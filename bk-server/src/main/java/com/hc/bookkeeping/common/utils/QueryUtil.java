package com.hc.bookkeeping.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.SortItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/29 10:18
 */
@Slf4j
public class QueryUtil {

    public static<T,R> Wrapper<R> bulid(T query){
        QueryWrapper<R> wrapper = new QueryWrapper<>();
        if(query == null){
            return wrapper;
        }

        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            List<SortItem> sortItems = new ArrayList<>();
            for (Field field: fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                //获取是否有排序
                Sort s = field.getAnnotation(Sort.class);
                if(s != null){
                    String column = StringUtils.isBlank(s.column()) ? field.getName() : s.column();
                    sortItems.add(new SortItem(s.sortOrder(),column,s.sort()));
                }
                //获取查询条件
                Object val = field.get(query);
                if(ObjectUtil.isEmpty(val)){
                    continue;
                }
                Query q = field.getAnnotation(Query.class);
                if(q != null){
                    String column = StringUtils.isBlank(q.column()) ? field.getName() : q.column();
                    Query.Type type = q.type();
                    switch (type){
                        case SQL:
                            String sql = q.sql();
                            if(StringUtils.isNotBlank(sql)){
                                wrapper.apply(sql,val);
                            }
                            break;
                        case MATCHING:
                            Query.Matching match = q.match();
                            setMatching(column,match,wrapper,val);
                            break;
                        default:
                            break;
                    }
                }
                field.setAccessible(accessible);
            }
            Collections.sort(sortItems);
            for (SortItem si: sortItems) {
                if(si.getType() == Sort.SortType.ASC){
                    wrapper.orderByAsc(si.getColumn());
                } else {
                    wrapper.orderByDesc(si.getColumn());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("构建查询条件错误:" + e.getMessage());
        }
        return wrapper;
    }

    private static void setMatching(String column, Query.Matching matching, QueryWrapper wrapper, Object val){
        switch (matching){
            case EQUAL:
                wrapper.eq(column,val);
                break;
            case IN:
                if (CollUtil.isNotEmpty((Collection)val)) {
                    wrapper.in(column,(Collection)val);
                }
                break;
            case NOT_EQUAL:
                wrapper.ne(column,val);
                break;
            case LEFT_LIKE:
                wrapper.likeLeft(column,val);
                break;
            case INNER_LIKE:
                wrapper.like(column,val);
                break;
            case RIGHT_LIKE:
                wrapper.likeRight(column,val);
                break;
            case GREATER_THAN:
                wrapper.gt(column,val);
                break;
            case LESS_THAN:
                wrapper.lt(column,val);
                break;
            case GREATER_EQ:
                wrapper.ge(column,val);
                break;
            case LESS_EQ:
                wrapper.le(column,val);
                break;
            case IS_NULL:
                wrapper.isNull(column);
                break;
            case NOT_NULL:
                wrapper.isNotNull(column);
                break;
            case BETWEEN:
                List<Object> between = new ArrayList<>((List<Object>)val);
                wrapper.between(column,between.get(0),between.get(1));
                break;
            case NOT_BETWEEN:
                List<Object> nbetween = new ArrayList<>((List<Object>)val);
                wrapper.notBetween(column,nbetween.get(0),nbetween.get(1));
                break;
            default:
                break;
        }
    }

    public static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }

    public static <T> void queryTreeRootCheck(T query){
        if(query == null){
            return;
        }

        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            boolean allEmpty = true;
            boolean hasPidField = false;
            Field pidField = null;
            for (Field field: fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Query q = field.getAnnotation(Query.class);
                if(q != null){
                    if(Constants.PARENT_FIELD_NAME.equals(field.getName())) {
                        hasPidField = true;
                        pidField = field;
                    }
                    //获取查询数据
                    Object val = field.get(query);
                    if(ObjectUtil.isNotEmpty(val)){
                        allEmpty = false;
                        field.setAccessible(accessible);
                        break;
                    }
                }
                field.setAccessible(accessible);
            }
            if(allEmpty && hasPidField){
                ReflectUtil.setFieldValue(query, pidField,-1);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("检查树形查询条件错误:" + e.getMessage());
        }
    }
}

package com.hc.bookkeeping.modules.bkeeping.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hc.bookkeeping.common.annotation.QueryHandler;
import com.hc.bookkeeping.modules.bkeeping.dto.ClassifyQueryItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类列表查询处理器
 * 支持复杂的多分类查询条件
 */
@Component
public class ClassifyListHandler implements QueryHandler {

    @Override
    public void apply(QueryWrapper<?> wrapper, Object value) {
        List<ClassifyQueryItem> list = (List<ClassifyQueryItem>) value;
        if (list == null || list.isEmpty()) {
            return;
        }
        wrapper.and(r -> {
            for (int i = 0; i < list.size(); i++) {
                ClassifyQueryItem item = list.get(i);
                if (item.getSubClassifyId() == null) {
                    r.apply("(main_classify = " + item.getMainClassifyId() + ")");
                } else {
                    r.apply("(main_classify = " + item.getMainClassifyId() + " AND sub_classify = " + item.getSubClassifyId() + ")");
                }
                if(i != list.size()-1){
                    r.or();
                }
            }
        });
    }
}
package com.hc.bookkeeping.modules.bkeeping.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.QueryUtil;
import com.hc.bookkeeping.modules.bkeeping.constants.Constants;
import com.hc.bookkeeping.modules.bkeeping.constants.ExpenseLimitShowType;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigDto;
import com.hc.bookkeeping.modules.bkeeping.dto.UserConfigQueryDto;
import com.hc.bookkeeping.modules.bkeeping.entity.UserConfig;
import com.hc.bookkeeping.modules.bkeeping.mapper.UserConfigMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.UserConfigMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.UserConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 用户配置表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Service
public class UserConfigServiceImpl extends BaseServiceImpl<UserConfigMapstruct, UserConfigDto, UserConfigMapper, UserConfig> implements UserConfigService {

    @Override
    public Page queryPage(UserConfigQueryDto queryDto, Page page) {
        return queryPage(page, QueryUtil.bulid(queryDto));
    }

    @Override
    public void setAdditionalExpenseLimit(Long userId, String type, String expenseLimit) {
        String configName = "";
        if(ExpenseLimitShowType.MONTHLY_SHOW.code.equals(type)){
            configName = Constants.EXPENSE_LIMIT_PREFIX + DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        } else if(ExpenseLimitShowType.YEARLY_SHOW.code.equals(type)){
            configName = Constants.EXPENSE_LIMIT_PREFIX + DateUtil.year(new Date());
        }
        if(StringUtils.isBlank(configName)){
            throw new BusinessException("支出限额类型错误");
        }
        UserConfig userConfig = getOne(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, userId).eq(UserConfig::getName, configName));
        if(userConfig == null){
            userConfig = new UserConfig();
            userConfig.setUserId(userId);
            userConfig.setName(configName);
            userConfig.setValue(expenseLimit);
            userConfig.setDescription("额外设置支出限额");
            save(userConfig);
        } else {
            userConfig.setValue(expenseLimit);
            updateById(userConfig);
        }
    }
}

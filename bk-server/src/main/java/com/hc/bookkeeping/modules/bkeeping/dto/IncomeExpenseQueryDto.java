package com.hc.bookkeeping.modules.bkeeping.dto;

import com.hc.bookkeeping.common.annotation.Query;
import com.hc.bookkeeping.common.annotation.Sort;
import com.hc.bookkeeping.common.model.BoolEnum;
import com.hc.bookkeeping.modules.bkeeping.handler.ClassifyListHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 收入支出表
 * </p>
 *
 * @author zjf
 * @since 2021-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="IncomeExpense对象", description="收入支出表")
public class IncomeExpenseQueryDto implements Serializable {

    @ApiModelProperty(value = "id")
    @Query
    private Long id;

    @ApiModelProperty(value = "用户id")
    @Query(column = "user_id")
    private Long userId;

    @ApiModelProperty(value = "账本id")
    @Query(column = "account_book_id")
    private Long accountBookId;

    @ApiModelProperty(value = "金额")
    @Query(match = Query.Matching.BETWEEN)
    private List<BigDecimal> amount;

    @ApiModelProperty(value = "产生时间")
    @Query(match = Query.Matching.BETWEEN)
    @Sort(sort = Sort.SortType.DESC, sortOrder = 0)
    private List<LocalDate> date;

    @ApiModelProperty(value = "备注")
    @Query(match = Query.Matching.INNER_LIKE, linkType = Query.LinkType.OR)
    private List<String> remark;

    @ApiModelProperty(value = "所属标签id列表")
    @Query(column = "tag_codes", linkType = Query.LinkType.OR, type = Query.Type.SQL, sql = "FIND_IN_SET({0}, #{column}) > 0")
    private List<String> tagCodes;

    @ApiModelProperty(value = "分类列表（包含主分类和子分类）")
    @Query(type = Query.Type.CUSTOM, customHandler = ClassifyListHandler.class)
    private List<ClassifyQueryItem> classifyList;

    @ApiModelProperty(value = "是否信用卡消费(0:否,1;是)")
    @Query(column = "is_credit_card")
    private BoolEnum isCreditCard;

    @ApiModelProperty(value = "创建时间")
    @Sort(column = "create_time", sort = Sort.SortType.DESC, sortOrder = 1)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "页码")
    private Integer pageNo;

    @ApiModelProperty(value = "每页记录数")
    private Integer pageSize;
}

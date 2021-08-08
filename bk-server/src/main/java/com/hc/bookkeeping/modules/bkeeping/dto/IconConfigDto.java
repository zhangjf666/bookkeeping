package com.hc.bookkeeping.modules.bkeeping.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hc.bookkeeping.modules.bkeeping.model.BillType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="IconConfig对象", description="图标配置表")
public class IconConfigDto {

    @ApiModelProperty(value = "配置id")
    private Long id;

    @ApiModelProperty(value = "图标代码")
    private String code;

    @ApiModelProperty(value = "图标类型(0:支出,1:收入)")
    private BillType type;
}

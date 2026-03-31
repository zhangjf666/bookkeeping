package com.hc.bookkeeping.modules.bkeeping.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AvatarUploadResult {

    @ApiModelProperty(value = "头像文件URL，用于显示")
    private String url;

    @ApiModelProperty(value = "头像文件名，保存用户资料时使用")
    private String fileName;
}

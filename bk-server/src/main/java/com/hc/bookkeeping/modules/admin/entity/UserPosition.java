package com.hc.bookkeeping.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 用户-职位表
 * </p>
 *
 * @author zjf
 * @since 2020-06-11
 */
@Data
@NoArgsConstructor
@TableName("sys_user_position")
@ApiModel(value="UserPosition对象", description="用户-职位表")
public class UserPosition {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "职位id")
    private Long positionId;

    public UserPosition(Long userId, Long positionId) {
        this.userId = userId;
        this.positionId = positionId;
    }
}

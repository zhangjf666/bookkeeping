package com.hc.bookkeeping.modules.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/9/29 10:55
 */
@ApiModel("服务器信息")
@Data
public class ServerInfoDto {
    @ApiModelProperty(value = "系统信息")
    private SysInfoDto sys;
    @ApiModelProperty(value = "cpu信息")
    private CpuInfoDto cpu;
    @ApiModelProperty(value = "内存信息")
    private MemoryInfoDto memory;
    @ApiModelProperty(value = "交换区信息")
    private SwapInfoDto swap;
    @ApiModelProperty(value = "磁盘信息")
    private DiskInfoDto disk;
    @ApiModelProperty(value = "当前时间")
    private String time;

    /**
     * 系统信息Dto
     */
    @ApiModel("系统信息")
    @Data
    public static class SysInfoDto{
        @ApiModelProperty(value = "系统运行时间")
        private String runTime;
        @ApiModelProperty(value = "系统类型")
        private String os;
        @ApiModelProperty(value = "ip地址")
        private String ip;
    }

    /**
     * CPU信息Dto
     */
    @ApiModel("CPU信息")
    @Data
    public static class CpuInfoDto{
        @ApiModelProperty(value = "处理器名称")
        private String name;
        @ApiModelProperty(value = "物理处理器个数")
        private int physicalPackage;
        @ApiModelProperty(value = "物理核心数")
        private int core;
        @ApiModelProperty(value = "逻辑处理器个数")
        private int logic;
        @ApiModelProperty(value = "使用率")
        private String used;
        @ApiModelProperty(value = "空闲率")
        private String idle;
    }

    /**
     * 内存信息Dto
     */
    @ApiModel("内存信息")
    @Data
    public static class MemoryInfoDto{
        @ApiModelProperty(value = "总容量")
        private String total;
        @ApiModelProperty(value = "可用容量")
        private String available;
        @ApiModelProperty(value = "已使用容量")
        private String used;
        @ApiModelProperty(value = "使用率")
        private String usageRate;
    }

    /**
     * 交换区信息Dto
     */
    @ApiModel("交换区信息")
    @Data
    public static class SwapInfoDto{
        @ApiModelProperty(value = "总容量")
        private String total;
        @ApiModelProperty(value = "可用容量")
        private String available;
        @ApiModelProperty(value = "已使用容量")
        private String used;
        @ApiModelProperty(value = "使用率")
        private String usageRate;
    }

    /**
     * 磁盘信息Dto
     */
    @ApiModel("磁盘信息")
    @Data
    public static class DiskInfoDto{
        @ApiModelProperty(value = "磁盘名称")
        private String name;
        @ApiModelProperty(value = "总容量")
        private String total;
        @ApiModelProperty(value = "可用容量")
        private String available;
        @ApiModelProperty(value = "已使用容量")
        private String used;
        @ApiModelProperty(value = "使用率")
        private String usageRate;
        @ApiModelProperty(value = "磁盘详细信息")
        private List<DiskInfoDto> disks;
    }
}

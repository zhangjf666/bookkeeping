package com.hc.bookkeeping.modules.admin.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.hc.bookkeeping.modules.admin.dto.ServerInfoDto;
import com.hc.bookkeeping.modules.admin.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/9/29 11:16
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MonitorServiceImpl implements MonitorService {

    private final Environment env;

    @Override
    public ServerInfoDto getServerInfo() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        HardwareAbstractionLayer hal = si.getHardware();
        ServerInfoDto serverInfoDto = new ServerInfoDto();
        serverInfoDto.setSys(getSysInfo(os));
        serverInfoDto.setCpu(getCpuInfo(hal.getProcessor()));
        serverInfoDto.setMemory(getMemoryInfo(hal.getMemory()));
        serverInfoDto.setSwap(getSwapInfo(hal.getMemory()));
        serverInfoDto.setDisk(getDiskInfo(os));
        serverInfoDto.setTime(DateUtil.formatTime(new Date()));
        return serverInfoDto;
    }

    @Override
    public String getSqlMonitorUrl() {
        return StrUtil.format("http://{}:{}/druid/", NetUtil.getLocalhostStr(), env.getProperty("server.port"));
    }

    /**
     * 获取系统信息
     *
     * @param os 系统
     * @return ServerInfoDto.SysInfoDto
     */
    private ServerInfoDto.SysInfoDto getSysInfo(OperatingSystem os){
        ServerInfoDto.SysInfoDto dto = new ServerInfoDto.SysInfoDto();
        // jvm 运行时间
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        Date date = new Date(time);
        // 计算项目运行时间
        String formatBetween = DateUtil.formatBetween(date, new Date(), BetweenFormatter.Level.SECOND);
        // 系统信息
        dto.setRunTime(formatBetween);
        dto.setIp(NetUtil.getLocalhostStr());
        dto.setOs(os.toString());
        return dto;
    }

    /**
     * 获取CPU信息
     *
     * @param processor 处理器
     * @return ServerInfoDto.CpuInfoDto
     */
    private ServerInfoDto.CpuInfoDto getCpuInfo(CentralProcessor processor){
        ServerInfoDto.CpuInfoDto dto = new ServerInfoDto.CpuInfoDto();
        dto.setName(processor.getProcessorIdentifier().getName());
        dto.setPhysicalPackage(processor.getPhysicalPackageCount());
        dto.setCore(processor.getPhysicalProcessorCount());
        dto.setLogic(processor.getLogicalProcessorCount());
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 等待1秒...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        dto.setUsed(NumberUtil.formatPercent(user / (double)totalCpu + sys / (double)totalCpu,2));
        dto.setIdle(NumberUtil.formatPercent(idle / (double)totalCpu, 2));
        return dto;
    }

    /**
     * 获取内存信息
     *
     * @param memory 存储
     * @return ServerInfoDto.MemoryInfoDto
     */
    private ServerInfoDto.MemoryInfoDto getMemoryInfo(GlobalMemory memory) {
        ServerInfoDto.MemoryInfoDto dto = new ServerInfoDto.MemoryInfoDto();
        dto.setTotal(FormatUtil.formatBytes(memory.getTotal()));
        dto.setAvailable(FormatUtil.formatBytes(memory.getAvailable()));
        dto.setUsed(FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable()));
        dto.setUsageRate(NumberUtil.formatPercent((memory.getTotal() - memory.getAvailable())/(double)memory.getTotal(),2));
        return dto;
    }

    /**
     * 获取交换区信息
     *
     * @param memory 交换区
     * @return ServerInfoDto.SwapInfoDto
     */
    private ServerInfoDto.SwapInfoDto getSwapInfo(GlobalMemory memory) {
        ServerInfoDto.SwapInfoDto dto = new ServerInfoDto.SwapInfoDto();
        dto.setTotal(FormatUtil.formatBytes(memory.getVirtualMemory().getSwapTotal()));
        dto.setUsed(FormatUtil.formatBytes(memory.getVirtualMemory().getSwapUsed()));
        dto.setAvailable(FormatUtil.formatBytes(memory.getVirtualMemory().getSwapTotal() - memory.getVirtualMemory().getSwapUsed()));
        dto.setUsageRate(NumberUtil.formatPercent(memory.getVirtualMemory().getSwapUsed()/(double)memory.getVirtualMemory().getSwapTotal(),2));
        return dto;
    }

    /**
     * 获取磁盘信息
     *
     * @param os 系统
     * @return ServerInfoDto.DiskInfoDto
     */
    private ServerInfoDto.DiskInfoDto getDiskInfo(OperatingSystem os) {
        ServerInfoDto.DiskInfoDto dto = new ServerInfoDto.DiskInfoDto();
        dto.setName("磁盘合计");
        List<ServerInfoDto.DiskInfoDto> disks = new ArrayList<>();
        long diskTotal = 0L;
        long availableTotal = 0L;
        long usedTotal = 0L;
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray){
            ServerInfoDto.DiskInfoDto disk = new ServerInfoDto.DiskInfoDto();
            disk.setName(fs.getName());
            long total = fs.getTotalSpace() > 0 ? fs.getTotalSpace() : 0;
            disk.setTotal(FileUtil.readableFileSize(total));
            long available = fs.getUsableSpace();
            disk.setAvailable(FileUtil.readableFileSize(available));
            long used = fs.getTotalSpace() - fs.getUsableSpace();
            disk.setUsed(FileUtil.readableFileSize(used));
            disk.setUsageRate(NumberUtil.formatPercent(used/(double)total,2));
            diskTotal += total;
            availableTotal += available;
            usedTotal += used;
            disks.add(disk);
        }
        dto.setTotal(FileUtil.readableFileSize(diskTotal));
        dto.setAvailable(FileUtil.readableFileSize(availableTotal));
        dto.setUsed(FileUtil.readableFileSize(usedTotal));
        dto.setUsageRate(NumberUtil.formatPercent(usedTotal/(double)diskTotal,2));
        dto.setDisks(disks);
        return dto;
    }
}

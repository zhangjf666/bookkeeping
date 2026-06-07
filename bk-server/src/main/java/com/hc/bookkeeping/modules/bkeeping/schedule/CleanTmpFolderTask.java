package com.hc.bookkeeping.modules.bkeeping.schedule;

import com.hc.bookkeeping.config.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 清理临时文件夹定时任务
 * 每天01:00执行，删除上传临时文件夹中的所有文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanTmpFolderTask {

    private final SystemProperties systemProperties;

    /**
     * 每天凌晨01:00执行清理任务
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanTempFolder() {
        String tempPath = systemProperties.getUploadTempPath();
        if (tempPath == null || tempPath.isEmpty()) {
            log.warn("临时文件夹路径未配置，跳过清理");
            return;
        }

        File tempDir = new File(tempPath);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            log.warn("临时文件夹不存在，跳过清理: {}", tempPath);
            return;
        }

        try {
            File[] files = tempDir.listFiles();
            if (files == null || files.length == 0) {
                log.info("临时文件夹为空，无需清理: {}", tempPath);
                return;
            }

            int deletedCount = 0;
            for (File file : files) {
                if (file.isFile()) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                } else if (file.isDirectory()) {
                    // 如果是子目录，递归删除
                    if (deleteDirectory(file)) {
                        deletedCount++;
                    }
                }
            }
            log.info("临时文件夹清理完成，共删除 {} 个文件/文件夹: {}", deletedCount, tempPath);
        } catch (Exception e) {
            log.error("清理临时文件夹失败: {}", tempPath, e);
        }
    }

    /**
     * 递归删除目录及其内容
     */
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }
}
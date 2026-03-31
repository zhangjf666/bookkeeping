package com.hc.bookkeeping.modules.bkeeping.service.impl;

import com.hc.bookkeeping.common.base.BaseServiceImpl;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import com.hc.bookkeeping.config.properties.SystemProperties;
import com.hc.bookkeeping.modules.bkeeping.constants.Constants;
import com.hc.bookkeeping.modules.bkeeping.dto.AvatarUploadResult;
import com.hc.bookkeeping.modules.bkeeping.dto.BookkeepingUserDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ChangePasswordDto;
import com.hc.bookkeeping.modules.bkeeping.entity.BookkeepingUser;
import com.hc.bookkeeping.modules.bkeeping.mapper.BookkeepingUserMapper;
import com.hc.bookkeeping.modules.bkeeping.mapstruct.BookkeepingUserMapstruct;
import com.hc.bookkeeping.modules.bkeeping.service.BookkeepingUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * <p>
 * 记账用户表 服务实现类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookkeepingUserServiceImpl extends BaseServiceImpl<BookkeepingUserMapstruct, BookkeepingUserDto, BookkeepingUserMapper, BookkeepingUser> implements BookkeepingUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemProperties systemProperties;

    private static final String UPLOAD_AVATAR_PATH = Constants.UPLOAD_PATH + "avatar/";

    @Override
    public BookkeepingUserDto getCurrentUser() {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        return queryById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookkeepingUserDto updateCurrentUser(BookkeepingUserDto userDto) throws IOException {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        
        // 获取数据库中现有数据
        BookkeepingUser existingUser = baseMapper.selectById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        String oldAvatar = existingUser.getAvatar();
        String newAvatar = userDto.getAvatar();

        // 如果头像有变化，处理头像文件
        if (newAvatar != null && !newAvatar.equals(oldAvatar)) {
            // 获取临时文件并移动到正式目录
            String tempFilePath = systemProperties.getUploadTempPath() + newAvatar;
            File tempFile = new File(tempFilePath);
            
            if (tempFile.exists()) {
                // 创建头像目录
                File avatarDir = new File(systemProperties.getUploadPath() + "avatar/");
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs();
                }

                // 生成正式文件名
                String fileExtension = getFileExtension(newAvatar);
                String finalFileName = "avatar_" + userId + "_" + System.currentTimeMillis() + fileExtension;
                String finalPath = systemProperties.getUploadPath() + "avatar/" + finalFileName;

                // 移动文件
                Path tempPath = Paths.get(tempFilePath);
                Path targetPath = Paths.get(finalPath);
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // 设置新头像路径
                userDto.setAvatar(UPLOAD_AVATAR_PATH + finalFileName);

                // 删除旧头像文件
                if (oldAvatar != null && !oldAvatar.isEmpty()) {
                    String oldFileName = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                    File oldFile = new File(systemProperties.getUploadPath() + "avatar/" + oldFileName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
            }
        } else {
            // 如果头像没有变化，保持原有头像
            userDto.setAvatar(oldAvatar);
        }

        // 更新用户信息
        userDto.setId(userId);
        userDto.setUsername(existingUser.getUsername());
        BookkeepingUser user = baseMapstruct.toEntity(userDto);
        // 不更新密码字段跟用户名字段
        user.setPassword(null);
        user.setUsername(null);
        baseMapper.updateById(user);
        return userDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDto changePasswordDto) {
        Long userId = SpringSecurityUtil.getCurrentUserId();
        BookkeepingUser user = baseMapper.selectById(userId);
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证当前密码
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码错误");
        }

        // 验证新密码和确认密码是否一致
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AvatarUploadResult uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new BusinessException("只支持JPG和PNG格式的图片");
        }

        // 验证文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过2MB");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        // 生成临时文件名
        String tempFileName = "tmp_" + System.currentTimeMillis() + fileExtension;

        // 创建临时目录
        File tempDir = new File(systemProperties.getUploadTempPath());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 保存临时文件
        File tempFile = new File(systemProperties.getUploadTempPath() + tempFileName);
        file.transferTo(tempFile);
        AvatarUploadResult result = new AvatarUploadResult();
        result.setFileName(tempFileName);
        result.setUrl(Constants.UPLOAD_TMP_PATH + tempFileName);
        return result;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return ".jpg";
        }
        return fileName.substring(lastDot);
    }
}

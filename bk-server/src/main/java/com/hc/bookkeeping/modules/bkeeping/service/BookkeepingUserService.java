package com.hc.bookkeeping.modules.bkeeping.service;

import com.hc.bookkeeping.common.base.BaseService;
import com.hc.bookkeeping.modules.bkeeping.dto.BookkeepingUserDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ChangePasswordDto;
import com.hc.bookkeeping.modules.bkeeping.entity.BookkeepingUser;
import com.hc.bookkeeping.modules.bkeeping.dto.AvatarUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 记账用户表 服务类
 * </p>
 *
 * @author zjf
 * @since 2021-08-08
 */
public interface BookkeepingUserService extends BaseService<BookkeepingUserDto, BookkeepingUser> {

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    BookkeepingUserDto getCurrentUser();

    /**
     * 更新当前用户信息
     * @param userDto 用户信息
     */
    BookkeepingUserDto updateCurrentUser(BookkeepingUserDto userDto) throws IOException;

    /**
     * 修改密码
     * @param changePasswordDto 修改密码参数
     */
    void changePassword(ChangePasswordDto changePasswordDto);

    /**
     * 上传头像
     * @param file 头像文件
     * @return 上传结果
     */
    AvatarUploadResult uploadAvatar(MultipartFile file) throws IOException;

}

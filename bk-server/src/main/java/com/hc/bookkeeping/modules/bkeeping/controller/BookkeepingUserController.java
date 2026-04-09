package com.hc.bookkeeping.modules.bkeeping.controller;

import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.modules.bkeeping.dto.AvatarUploadResult;
import com.hc.bookkeeping.modules.bkeeping.dto.BookkeepingUserDto;
import com.hc.bookkeeping.modules.bkeeping.dto.ChangePasswordDto;
import com.hc.bookkeeping.modules.bkeeping.service.BookkeepingUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户接口(只能操作自己)
 */
@Slf4j
@RestController
@RequestMapping("/bookkeepingUser")
@RequiredArgsConstructor
@Api(tags = "用户接口")
public class BookkeepingUserController {

    private final BookkeepingUserService userService;

    @Log("获取当前用户信息")
    @ApiOperation("获取当前用户信息")
    @GetMapping
    public BookkeepingUserDto getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Log("更新当前用户信息")
    @ApiOperation("更新当前用户信息")
    @PutMapping
    public BookkeepingUserDto updateCurrentUser(@RequestBody @Validated BookkeepingUserDto userDto) throws IOException {
        return userService.updateCurrentUser(userDto);
    }

    @Log("修改密码")
    @ApiOperation("修改密码")
    @PostMapping("/changePwd")
    public boolean changePassword(@RequestBody @Validated ChangePasswordDto changePasswordDto) {
        return userService.changePassword(changePasswordDto);
    }

    @Log("上传头像")
    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public AvatarUploadResult uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadAvatar(file);
    }
}

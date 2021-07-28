package com.hc.bookkeeping.modules.security.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import com.hc.bookkeeping.common.annotation.Anonymous;
import com.hc.bookkeeping.common.annotation.Log;
import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.model.LogType;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.support.valid.Insert;
import com.hc.bookkeeping.common.utils.RedisUtil;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import com.hc.bookkeeping.config.properties.SystemProperties;
import com.hc.bookkeeping.modules.admin.dto.UserDto;
import com.hc.bookkeeping.modules.admin.service.UserService;
import com.hc.bookkeeping.modules.security.config.JwtProperties;
import com.hc.bookkeeping.modules.security.dto.CacheUser;
import com.hc.bookkeeping.modules.security.dto.JwtUserDetails;
import com.hc.bookkeeping.modules.security.dto.LoginUserDto;
import com.hc.bookkeeping.modules.security.dto.RegisterUserDto;
import com.hc.bookkeeping.modules.security.service.JwtService;
import com.hc.bookkeeping.modules.security.service.OnlineUserService;
import com.wf.captcha.SpecCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hc.bookkeeping.common.constants.Constants.CAPTCHA_KEY;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 15:00
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "用户认证接口")
public class AuthenticationController {

    private final JwtProperties properties;
    private final OnlineUserService onlineUserService;
    private final JwtService jwtService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SystemProperties systemProperties;
    private final UserService userService;

    @Log(value = "用户登录",type = LogType.USER)
    @ApiOperation("登录授权")
    @Anonymous
    @PostMapping("/login")
    public Response login(@Validated @RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        //检查验证码
        checkCaptcha(loginUserDto);
        //用户校验
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌
        String token = jwtService.createToken(authentication);
        final JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        // 保存在线信息
        onlineUserService.save(jwtUserDetails, token, request);
        //单一用户登录,踢出之前用户
        if(systemProperties.getSingleLogin()){
            onlineUserService.checkLoginOnUser(jwtUserDetails.getUsername(),token);
        }
        // 返回 token 与 用户信息
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {{
            put("token", properties.getTokenStartWith() + token);
            put("user", BeanUtil.copyProperties(jwtUserDetails.getUser(), UserDto.class));
        }};
        return Response.ok(authInfo);
    }

    private void checkCaptcha(@RequestBody @Validated LoginUserDto loginUserDto) {
        if(!systemProperties.getEnableCaptcha()){
            return;
        }
        //获取验证码
        String captcha = (String) RedisUtil.get(CAPTCHA_KEY + loginUserDto.getUuid());
        if (StringUtils.isBlank(captcha)) {
            throw new BusinessException("验证码已过期");
        }
        //清除验证码
        RedisUtil.del(CAPTCHA_KEY + loginUserDto.getUuid());
        //校验验证码
        if (StringUtils.isBlank(loginUserDto.getCaptcha()) && captcha.equalsIgnoreCase(loginUserDto.getCaptcha())) {
            throw new BusinessException("验证码错误");
        }
    }

    @Anonymous
    @ApiOperation("获取验证码")
    @GetMapping("/captcha")
    public Response getCaptcha() {
        //生成验证码
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String captcha = specCaptcha.text().toLowerCase();
        String uuid = IdUtil.simpleUUID();
        //保存验证码
        RedisUtil.set(CAPTCHA_KEY + uuid, captcha);
        Map<String, Object> res = new HashMap<>(2);
        res.put("img", specCaptcha.toBase64());
        res.put("uuid", uuid);
        return Response.ok(res);
    }

    @ApiOperation("注册用户")
    @Anonymous
    @PostMapping("/register")
    public Response create(@Validated(Insert.class) @RequestBody RegisterUserDto dto){
        if(userService.checkExist(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        if(!dto.getPassword().equals(dto.getRepeatPassword())){
            throw new BusinessException("2次密码不相同");
        }
        userService.registerUser(dto);
        return Response.ok();
    }

    @Log("获取用户缓存信息")
    @ApiOperation("获取用户缓存信息")
    @PostMapping("/user-info")
    public Response getCacheUser() {
        CacheUser cacheUser = ((JwtUserDetails) SpringSecurityUtil.getCurrentUser()).getUser();
        List<String> userPermissions = cacheUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return Response.ok(Dict.create().set("user",BeanUtil.copyProperties(cacheUser, UserDto.class))
                .set("permission",userPermissions));
    }

    @Log("获取用户缓存信息")
    @ApiOperation("获取用户缓存信息")
    @PostMapping("/user-permission")
    public Response getUserPermission() {
        List<String> userPermissions = SpringSecurityUtil.getCurrentUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return Response.ok(userPermissions);
    }

    @Log(value = "退出登录",type = LogType.USER)
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Response logout(HttpServletRequest request) {
        onlineUserService.logout(jwtService.getToken(request));
        return Response.ok();
    }

    @Log("查询在线用户")
    @ApiOperation("查询在线用户")
    @GetMapping("/online")
    @PreAuthorize("@ph.check()")
    public Response getOnlineUser(String username, Pageable pageable){
        return Response.ok(onlineUserService.getAll(username, pageable));
    }

    @Log("踢出用户")
    @ApiOperation("踢出用户")
    @PostMapping("/online-kickout")
    @PreAuthorize("@ph.check()")
    public Response kickout(@RequestBody Set<String> userKeys){
        for (String key : userKeys) {
            onlineUserService.kickOut(key);
        }
        return Response.ok();
    }
}

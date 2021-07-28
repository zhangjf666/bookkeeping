package com.hc.bookkeeping.modules.security.service;

import com.hc.bookkeeping.common.model.Page;
import com.hc.bookkeeping.common.utils.RedisUtil;
import com.hc.bookkeeping.common.utils.WebUtil;
import com.hc.bookkeeping.modules.security.config.JwtProperties;
import com.hc.bookkeeping.modules.security.dto.JwtUserDetails;
import com.hc.bookkeeping.modules.security.dto.OnlineUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hc.bookkeeping.common.constants.Constants.CACHE_USER;
import static com.hc.bookkeeping.common.constants.Constants.ONLINE_USER_KEY;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/9 14:26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OnlineUserService {
    private final JwtProperties properties;

    /**
     * 保存在线用户信息
     * @param jwtUserDetails /
     * @param token /
     * @param request /
     */
    public void save(JwtUserDetails jwtUserDetails, String token, HttpServletRequest request){
        String ip = WebUtil.getIp(request);
        String browser = WebUtil.getBrowser(request);
        OnlineUserDto onlineUserDto = null;
        try {
            onlineUserDto = new OnlineUserDto(jwtUserDetails.getUser().getId(),jwtUserDetails.getUsername(), jwtUserDetails.getUser().getNickName(), browser , ip, token, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
        RedisUtil.set(ONLINE_USER_KEY + token, onlineUserDto, properties.getTokenValidity());
    }

    /**
     * 查询全部数据
     * @param pageable /
     * @return /
     */
    public Page getAll(String filterUsername, Pageable pageable){
        List<OnlineUserDto> onlineUserDtos = getAll(filterUsername);
        return Page.toPage(pageable.getPageNumber(),pageable.getPageSize(),onlineUserDtos);
    }

    /**
     * 查询全部数据，不分页
     * @return /
     */
    public List<OnlineUserDto> getAll(String filterUsername){
        List<OnlineUserDto> onlineUserDtos = new ArrayList<>();
        Set<String> keys = RedisUtil.getKeys(ONLINE_USER_KEY + "*");
        for (String key: keys) {
            OnlineUserDto onlineUserDto = (OnlineUserDto) RedisUtil.get(key);
            if(StringUtils.isNotBlank(filterUsername)){
                if(onlineUserDto.getUserName().equals(filterUsername)){
                    onlineUserDtos.add((OnlineUserDto) RedisUtil.get(key));
                }
            } else {
                onlineUserDtos.add(onlineUserDto);
            }
        }
        return onlineUserDtos;
    }

    /**
     * 踢出用户
     * @param token /
     */
    public void kickOut(String token){
        String key = ONLINE_USER_KEY + token;
        //先删除用户缓存
        OnlineUserDto onlineUserDto = (OnlineUserDto) RedisUtil.get(key);
        RedisUtil.hdel(CACHE_USER,onlineUserDto.getId().toString());
        RedisUtil.del(key);
    }

    /**
     * 退出登录
     * @param token /
     */
    public void logout(String token) {
        String key = ONLINE_USER_KEY + token;
        //先删除用户缓存
        OnlineUserDto onlineUserDto = (OnlineUserDto) RedisUtil.get(key);
        RedisUtil.hdel(CACHE_USER,onlineUserDto.getId().toString());
        RedisUtil.del(key);
    }

    /**
     * 查询用户
     * @param key /
     * @return /
     */
    public OnlineUserDto getOne(String key) {
        return (OnlineUserDto)RedisUtil.get(key);
    }

    /**
     * 检测用户是否在之前已经登录，已经登录踢下线
     * @param userName 用户名
     */
    public void checkLoginOnUser(String userName, String igoreToken){
        List<OnlineUserDto> onlineUserDtos = getAll(userName);
        if(onlineUserDtos ==null || onlineUserDtos.isEmpty()){
            return;
        }
        for(OnlineUserDto onlineUserDto : onlineUserDtos){
            if(onlineUserDto.getUserName().equals(userName)){
                try {
                    String token = onlineUserDto.getKey();
                    if(StringUtils.isNotBlank(igoreToken)&&!igoreToken.equals(token)){
                        RedisUtil.del(ONLINE_USER_KEY + token);
                    }else if(StringUtils.isBlank(igoreToken)){
                        RedisUtil.del(ONLINE_USER_KEY + token);
                    }
                } catch (Exception e) {
                    log.error("checkUser is error",e);
                }
            }
        }
    }
}

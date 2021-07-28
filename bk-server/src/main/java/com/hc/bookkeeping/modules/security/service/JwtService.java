package com.hc.bookkeeping.modules.security.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.hc.bookkeeping.common.utils.RedisUtil;
import com.hc.bookkeeping.modules.security.config.JwtProperties;
import com.hc.bookkeeping.modules.security.dto.CacheUser;
import com.hc.bookkeeping.modules.security.dto.JwtUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

import static com.hc.bookkeeping.common.constants.Constants.CACHE_USER;
import static com.hc.bookkeeping.common.constants.Constants.ONLINE_USER_KEY;


/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/8 15:30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService implements InitializingBean {
    private static String CLAIM_USER_ID = "id";
    private final JwtProperties jwtProperties;

    private Key key;

    public String createToken(Authentication authentication) {
        JwtUserDetails jwtUserDetails = (JwtUserDetails) authentication.getPrincipal();
        //缓存CacheUser
        RedisUtil.hset(CACHE_USER, jwtUserDetails.getUser().getId().toString(),jwtUserDetails.getUser());

        return Jwts.builder()
                .setSubject(jwtUserDetails.getUsername())
                .claim(CLAIM_USER_ID,jwtUserDetails.getUser().getId())
                .signWith(key, SignatureAlgorithm.HS512)
                // 加入ID确保生成的 Token 都不一致
                .setId(IdUtil.simpleUUID())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        //获取CacheUser
        CacheUser cacheUser = (CacheUser) RedisUtil.hget(CACHE_USER, claims.get(CLAIM_USER_ID).toString());
        JwtUserDetails jwtUserDetails = new JwtUserDetails(cacheUser,cacheUser.getAuthorities());
        return new UsernamePasswordAuthenticationToken(jwtUserDetails, token, cacheUser.getAuthorities());
    }

    /**
     * @param token 需要检查的token
     */
    public void checkRenewal(String token){
        // 判断是否续期token,计算token的过期时间
        long time = RedisUtil.ttl(ONLINE_USER_KEY + token);
        Date expireDate = DateUtil.offset(new Date(), DateField.MILLISECOND, (int) time);
        // 判断当前时间与过期时间的时间差
        long differ = expireDate.getTime() - System.currentTimeMillis();
        // 如果在续期检查的范围内，则续期
        if(differ <= jwtProperties.getDetect()){
            long renew = time + jwtProperties.getRenew();
            RedisUtil.expire(ONLINE_USER_KEY + token, renew);
        }
    }

    public String getToken(HttpServletRequest request){
        final String requestHeader = request.getHeader(jwtProperties.getHeader());
        if (requestHeader != null && requestHeader.startsWith(jwtProperties.getTokenStartWith())) {
            return requestHeader.substring(jwtProperties.getTokenStartWith().length());
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getBase64Secret());
        key = Keys.hmacShaKeyFor(keyBytes);
    }
}

package com.hc.bookkeeping.modules.security.config;

import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.model.ResponseCode;
import com.hc.bookkeeping.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: Created on 2019/3/15 10:52
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        Response response = new Response(ResponseCode.UNAUTHORIZED,e==null?"Unauthorized":e.getMessage());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String resStr = JsonUtil.toJsonString(response);
        log.error("Exception : "+ resStr);
        httpServletResponse.getOutputStream().write(resStr.getBytes(StandardCharsets.UTF_8));
    }
}

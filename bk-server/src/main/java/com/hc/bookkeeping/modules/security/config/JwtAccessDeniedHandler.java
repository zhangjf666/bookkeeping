package com.hc.bookkeeping.modules.security.config;

import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.model.ResponseCode;
import com.hc.bookkeeping.common.utils.JsonUtil;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
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
 * @Date: Created on 2019/3/11 17:11
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Response response = new Response(ResponseCode.FORBIDDEN,null);
        httpServletResponse.getOutputStream().write(JsonUtil.toJsonString(response).getBytes(StandardCharsets.UTF_8));
    }
}

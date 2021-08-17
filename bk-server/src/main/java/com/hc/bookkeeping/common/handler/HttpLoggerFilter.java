package com.hc.bookkeeping.common.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.swsc.microservices.common.constants.KeysConstant;
import com.swsc.microservices.common.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**用于记录出入参信息
 */
@Component
public class HttpLoggerFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger logger = LoggerFactory.getLogger(HttpLoggerFilter.class);

    private int order = Ordered.LOWEST_PRECEDENCE - 8;

    private static List<String> unFilterUrl;
    @Autowired
    private Environment env;

    static {
        unFilterUrl = Arrays.asList("/doc.html", "/swagger-resources", "/webjars", "/v2", "/swagger-ui.html", "/favicon", "/druid");
    }

    @Autowired
    private Gson gson;
    @Autowired
    private LogUtil logUtil;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String filterFunctionId = env.getProperty("logger.filter.function", "");
        String[] funtionIds = filterFunctionId.split(",");
        // 设置SessionId
        String requestId = UUID.randomUUID().toString().replace("-", "");
        ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        String uri = wrapperRequest.getRequestURI();//请求路径
        String sysId = wrapperRequest.getParameter(KeysConstant.SYS_ID);
        if (StringUtils.isBlank(sysId)) {
            sysId = wrapperRequest.getHeader(KeysConstant.SYS_ID);
        }
        // 不需要记录日志的功能号
        if (!Arrays.asList(funtionIds).contains(uri)) {
            if (urlCompare(uri)) {//请求过滤
                filterChain.doFilter(wrapperRequest, wrapperResponse);
            } else {
                String remoteAddr = getIpAddr(wrapperRequest);//ip
                Map<String, String[]> pramMap = wrapperRequest.getParameterMap();
                logger.info("调用开始>>{ sysId : " + sysId + " uri : "
                        + uri + " | " + "IP : " + remoteAddr + " |}  params : " + logUtil.logFilter(gson.toJson(pramMap)));
                filterChain.doFilter(wrapperRequest, wrapperResponse);
                String requestBody = getRequestBody(wrapperRequest);
                String requestBodyStr = "";
                try {
                    Map paramaterMap = buildParametersMap(requestBody);
                    requestBodyStr = logUtil.mapFilter(paramaterMap);
                } catch (Exception e) {
                    //do nothing
                }
                logger.info(" 调用结束[sysId : " + sysId + " uri : " + uri + "]>>耗时:" + (System.currentTimeMillis() - startTime) + "ms,请求body：[  " + requestBodyStr + "  ]  返回值: [  " + logUtil.logFilter(getResponseBody(wrapperResponse)) + " ]");
            }
        } else {
            filterChain.doFilter(wrapperRequest, wrapperResponse);
        }

        wrapperResponse.copyBodyToResponse();
    }

    private  Map buildParametersMap(String requestBody) {
        try {
            if (JSONUtil.isJsonObj(requestBody)) {
                return JSONUtil.parseObj(requestBody);
            }else if (JSONUtil.isJsonArray(requestBody)){
                JSONArray array = JSONUtil.parseArray(requestBody);
                Map<String, Object> result = new HashMap<>();
                for (int i = 0; i < array.size(); i++) {
                    result.put(i+"", array.get(i));
                }
                return result;
            }
        } catch (Exception e) {
        }

        Map<String, String> requestMap = new HashMap();
        String[] requestParamters = requestBody.split("&");
        for (int i = 0; i < requestParamters.length; i++) {
            String[] requestParamter = requestParamters[i].split("=");
            requestMap.put(requestParamter[0], requestParamter[1]);
        }
        return requestMap;
    }

    private boolean urlCompare(String uri) {
        for (String url : unFilterUrl) {
            if (uri.startsWith(url))
                return true;
        }
        return false;
    }


    /**
     * 打印请求参数
     * @param response
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            if (wrapper.getContentType() == null || wrapper.getContentType().contains("excel") || wrapper.getContentType().contains("word"))
                return "";
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "";
                }
                return payload;
            }
        }
        return "";
    }

    /**
     * 打印请求参数
     * @param request
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "";
                }
                return payload.replaceAll("\\n", "");
            }
        }
        return "";
    }


    //获取客户端IP
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
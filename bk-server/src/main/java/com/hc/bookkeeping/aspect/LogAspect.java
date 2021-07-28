package com.hc.bookkeeping.aspect;

import cn.hutool.json.JSONObject;
import com.hc.bookkeeping.common.utils.ExceptionUtil;
import com.hc.bookkeeping.common.utils.JsonUtil;
import com.hc.bookkeeping.common.utils.SpringSecurityUtil;
import com.hc.bookkeeping.common.utils.WebUtil;
import com.hc.bookkeeping.modules.admin.entity.Log;
import com.hc.bookkeeping.modules.admin.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/23 13:23
 */
@Component
@Slf4j
@Aspect
public class LogAspect {

    private final LogService logService;

    private ThreadLocal<Log> logThreadLocal = new ThreadLocal<>();

    private Set<Class> excluedParamClass;

    public LogAspect(LogService logService) {
        this.logService = logService;
        excluedParamClass = new HashSet<>();
        excluedParamClass.add(HttpServletRequest.class);
    }

    @Pointcut("@annotation(com.hc.bookkeeping.common.annotation.Log)")
    public void logPointcut(){}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        Log log = new Log();
        HttpServletRequest request = WebUtil.getHttpServletRequest();
        if(request != null){
            log.setBrowser(WebUtil.getBrowser(request));
            log.setIp(WebUtil.getIp(request));
            log.setUserAgent(WebUtil.getUserAgent(request));
            log.setRequestUri(request.getRequestURI());
        }
        fillLog(log,joinPoint);
        logThreadLocal.set(log);
        result = joinPoint.proceed();
        log = logThreadLocal.get();
        logThreadLocal.remove();
        log.setLevel("INFO");
        log.setExecuteTime(Duration.between(log.getRequestTime(), LocalDateTime.now()).toMillis());
        log.setResponse(JsonUtil.toJsonString(result));
        logService.asyncSave(log);
        return result;
    }

    @AfterThrowing(pointcut = "logPointcut()",throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Throwable e){
        Log log = logThreadLocal.get();
        logThreadLocal.remove();
        log.setLevel("ERROR");
        log.setExecuteTime(Duration.between(log.getRequestTime(), LocalDateTime.now()).toMillis());
        log.setException(ExceptionUtil.getStackTraceAsString(e));
        logService.asyncSave(log);
    }

    //填充log内容
    private void fillLog(Log log, ProceedingJoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.hc.bookkeeping.common.annotation.Log anoLog = method.getAnnotation(com.hc.bookkeeping.common.annotation.Log.class);
        if(anoLog != null){
            log.setTitle(anoLog.value());
            log.setType(anoLog.type().getType());
        }
        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName();
        log.setMethod(methodName);
        // 参数
        Map<String, Object> params = getParams(joinPoint);
        log.setParams(JsonUtil.toJsonString(params));
        //用户名
        String loginPath = "login";
        String username = "";
        if (loginPath.equals(signature.getName())) {
            try {
                username = new JSONObject(params.get("loginUserDto")).get("username").toString();
            } catch (Exception e) {
                LogAspect.log.error(e.getMessage(), e);
            }
        } else{
            try {
                username = SpringSecurityUtil.getCurrentUsername();
            } catch (Exception e) {
                username = "";
            }
        }
        log.setCreateBy(username);
        log.setRequestTime(LocalDateTime.now());
    }

    private Map<String, Object> getParams(ProceedingJoinPoint joinPoint){
        Map<String, Object> params = new HashMap<>();

        Object[] paramValues = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature)joinPoint.getSignature()).getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            Object paramValue = paramValues[i];
            for (Class clazz: excluedParamClass) {
                if(clazz.isInstance(paramValue)){
                    continue;
                }
                params.put(paramName, paramValue);
            }
        }

        return params;
    }
}

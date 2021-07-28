package com.hc.bookkeeping.common.handler;

import com.hc.bookkeeping.common.constants.Constants;
import com.hc.bookkeeping.common.exception.BaseException;
import com.hc.bookkeeping.common.model.Response;
import com.hc.bookkeeping.common.model.ResponseCode;
import com.hc.bookkeeping.common.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description: controller异常统一处理
 * @Date: 2020/6/8 11:16
 */
@Controller
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler implements ErrorController {

    @Override
    public String getErrorPath() {
        return null;
    }

    /**
     * 处理token检查异常
     */
    @RequestMapping(Constants.TOKEN_CHECK_ERROR)
    @ResponseBody
    public Response tokenError(HttpServletRequest request) {
        return Response.fail(ResponseCode.UNAUTHORIZED);
    }

    /**
     * 处理404异常
     */
    @RequestMapping("${server.error.path:${error.path:/error}}")
    @ResponseBody
    public Response error(HttpServletRequest request) {
        return Response.fail(ResponseCode.NOT_FOUND);
    }

    /**
     * 处理所有不可知的异常
     */
    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public Response handleException(Throwable e){
        // 打印堆栈信息
        log.error(ExceptionUtil.getStackTraceAsString(e));
        return Response.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }

    /**
     * 处理Get请求中 使用@Valid 验证路径中请求实体校验失败后抛出的异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Response bindExceptionHandler(BindException e) {
        // 打印堆栈信息
        log.error(ExceptionUtil.getStackTraceAsString(e));
        String message = e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","));
        return Response.fail(ResponseCode.BAD_REQUEST.getCode(),message);
    }

    /**
     * 处理请求参数格式错误 @RequestParam上validate失败后抛出的异常是javax.validation.ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public Response constraintViolationExceptionHandler(ConstraintViolationException e) {
        // 打印堆栈信息
        log.error(ExceptionUtil.getStackTraceAsString(e));
        String message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","));
        return Response.fail(ResponseCode.BAD_REQUEST.getCode(),message);
    }

    /**
     * 处理请求参数格式错误 @RequestBody上validate失败后抛出的异常是MethodArgumentNotValidException异常
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        // 打印堆栈信息
        log.error(ExceptionUtil.getStackTraceAsString(e));
        String message = e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","));
        return Response.fail(ResponseCode.BAD_REQUEST.getCode(),message);
    }

    /**
     * 拦截BaseException异常 BaseException.class
     */
    @ResponseBody
    @ExceptionHandler(value = BaseException.class)
    public Response handlerBaseException(BaseException e) {
        log.error(e.getMessage(),e);
        return Response.fail(e.getErrorCode(), e.getLocalizedMessage());
    }
}

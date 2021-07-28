package com.hc.bookkeeping.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description: 异常工具类
 * @Date: Created on 2017/12/27 16:49
 */
public class ExceptionUtil {
    /**
     * 将CheckedException转换为UncheckedException.
     */
    public static RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }

    /**
     * 将ErrorStack转化为String.
     */
    public static String getStackTraceAsString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * 判断异常是否由某些底层的异常引起.
     */
    @SafeVarargs
    public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            for (Class<? extends Exception> causeClass : causeExceptionClasses) {
                if (causeClass.isInstance(cause)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}

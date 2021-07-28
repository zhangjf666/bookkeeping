package com.hc.bookkeeping.common.utils;


import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.support.redis.RedisManager;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/4 15:08
 */
public class RedisUtil {
    private static RedisManager jedisManager;
    private static RedisManager redissonManager;

    public static void setJedisManager(RedisManager jedisManager) {
        RedisUtil.jedisManager = jedisManager;
    }

    public static void setRedissonManager(RedisManager redissonManager) {
        RedisUtil.redissonManager = redissonManager;
    }

    private static RedisManager getManager(){
        if(jedisManager!=null){
            return jedisManager;
        }
        if(redissonManager!= null){
            return redissonManager;
        }
        throw new BusinessException("redis manager is not exist");
    }

    public static Object get(final String key) {
        return getManager().get(key);
    }

    public static Set<String> getKeys(String pattern){
        return getManager().getKeys(pattern);
    }

    public static Set<Object> getAll(final String pattern) {
        return getManager().getAll(pattern);
    }


    public static void set(final String key, final Serializable value, long seconds) {
        getManager().set(key,value,seconds);
    }


    public static void set(final String key, final Serializable value) {
        getManager().set(key,value);
    }


    public static Boolean exists(final String key) {
        return getManager().exists(key);
    }


    public static void del(final String key) {
        getManager().del(key);
    }


    public static void delAll(final String pattern) {
        getManager().delAll(pattern);
    }


    public static String type(final String key) {
        return getManager().type(key);
    }

    /**
     * 在某段时间后失效
     *
     * @return
     */

    public static Boolean expire(final String key, final long seconds) {
        return getManager().expire(key,seconds);
    }

    /**
     * 在某个时间点失效
     *
     * @param key
     * @param unixTime
     * @return
     */

    public static Boolean expireAt(final String key, final long unixTime) {
        return getManager().expireAt(key,unixTime);
    }


    public static Long ttl(final String key) {
        return getManager().ttl(key);
    }


    public static Object getSet(final String key, final Serializable value) {
        return getManager().getSet(key,value);
    }


    public static boolean setnx(String key, Serializable value) {
        return getManager().setnx(key,value);
    }


    public static void lock(String key, long leaseTime, TimeUnit unit) throws InterruptedException {
        if(redissonManager == null){
            throw new BusinessException("not support.");
        }
        redissonManager.lock(key,leaseTime,unit);
    }


    public static void unlock(String key) {
        if(redissonManager == null){
            throw new BusinessException("not support.");
        }
        redissonManager.unlock(key);
    }


    public static boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        if(redissonManager == null){
            throw new BusinessException("not support.");
        }
        return redissonManager.tryLock(key,waitTime,leaseTime,unit);
    }


    public static void hset(String key, Serializable field, Serializable value) {
        getManager().hset(key,field,value);
    }


    public static Object hget(String key, Serializable field) {
        return getManager().hget(key,field);
    }


    public static void hdel(String key, Serializable field) {
        getManager().hdel(key,field);
    }


    public static void sadd(String key, Serializable value) {
        getManager().sadd(key,value);
    }


    public static Set<?> sall(String key) {
        return getManager().sall(key);
    }


    public static boolean sdel(String key, Serializable value) {
        return getManager().sdel(key,value);
    }
}

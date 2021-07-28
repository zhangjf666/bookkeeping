package com.hc.bookkeeping.common.support.redis;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/4 15:10
 */
public interface RedisManager {
    Object get(final String key);

    Set<String> getKeys(String pattern);

    Set<Object> getAll(final String pattern);

    void set(final String key, final Serializable value, long seconds);

    void set(final String key, final Serializable value);

    Boolean exists(final String key);

    void del(final String key);

    void delAll(final String pattern);

    String type(final String key);

    Boolean expire(final String key, final long seconds);

    Boolean expireAt(final String key, final long unixTime);

    Long ttl(final String key);

    Object getSet(final String key, final Serializable value);

    void lock(String key, long leaseTime, TimeUnit unit) throws InterruptedException;

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;

    void unlock(String key);

    void hset(String key, Serializable field, Serializable value);

    Object hget(String key, Serializable field);

    void hdel(String key, Serializable field);

    boolean setnx(String key, Serializable value);

    void sadd(String key, Serializable value);

    Set<?> sall(String key);

    boolean sdel(String key, Serializable value);
}

package com.hc.bookkeeping.common.support.redis;

import com.hc.bookkeeping.common.exception.BusinessException;
import com.hc.bookkeeping.common.utils.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/4 15:10
 */
public class JedisRedisManager implements RedisManager {
    private RedisTemplate<Serializable, Serializable> redisTemplate;
    private Long EXPIRE = 600L;

    public JedisRedisManager(RedisTemplate<Serializable, Serializable> redisTemplate, Long expire) {
        this.redisTemplate = redisTemplate;
        this.EXPIRE = expire;
        RedisUtil.setJedisManager(this);
    }

    public JedisRedisManager(RedisTemplate<Serializable, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
        RedisUtil.setJedisManager(this);
    }

    @Override
    public final Object get(final String key) {
        return redisTemplate.boundValueOps(key).get();
    }

    @Override
    public Set<String> getKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        for (Serializable s: redisTemplate.keys(pattern)) {
            keys.add(s.toString());
        }
        return keys;
    }

    @Override
    public final Set<Object> getAll(final String pattern) {
        Set<Object> values = new HashSet<>();
        Set<Serializable> keys = redisTemplate.keys(pattern);
        for (Serializable key : keys) {
            values.add(redisTemplate.opsForValue().get(key));
        }
        return values;
    }

    @Override
    public final void set(final String key, final Serializable value, long seconds) {
        redisTemplate.boundValueOps(key).set(value);
        expire(key, seconds);
    }

    @Override
    public final void set(final String key, final Serializable value) {
        Long ttl = ttl(key);
        redisTemplate.boundValueOps(key).set(value);
        expire(key, ttl != null && ttl > 0 ? ttl : EXPIRE);
    }

    @Override
    public final Boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public final void del(final String key) {
        redisTemplate.delete(key);
    }

    @Override
    public final void delAll(final String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    @Override
    public final String type(final String key) {
        return redisTemplate.type(key).getClass().getName();
    }

    /**
     * 在某段时间后失效
     *
     * @return
     */
    @Override
    public final Boolean expire(final String key, final long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 在某个时间点失效
     *
     * @param key
     * @param unixTime
     * @return
     */
    @Override
    public final Boolean expireAt(final String key, final long unixTime) {
        return redisTemplate.expireAt(key, new Date(unixTime));
    }

    @Override
    public final Long ttl(final String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public final void setrange(final String key, final long offset, final String value) {
        Long ttl = ttl(key);
        redisTemplate.boundValueOps(key).set(value, offset);
        expire(key, ttl != null && ttl > 0 ? ttl : EXPIRE);
    }

    public final String getrange(final String key, final long startOffset, final long endOffset) {
        return redisTemplate.boundValueOps(key).get(startOffset, endOffset);
    }

    @Override
    public final Object getSet(final String key, final Serializable value) {
        return redisTemplate.boundValueOps(key).getAndSet(value);
    }

    @Override
    public boolean setnx(String key, Serializable value) {
        return redisTemplate.boundValueOps(key).setIfAbsent(value);
    }

    @Override
    public void lock(String key, long leaseTime, TimeUnit unit) {
        throw new RuntimeException("not support.");
    }

    @Override
    public void unlock(String key) {
        throw new BusinessException("not support.");
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        throw new BusinessException("not support.");
    }

    @Override
    public void hset(String key, Serializable field, Serializable value) {
        redisTemplate.boundHashOps(key).put(field, value);
    }

    @Override
    public Object hget(String key, Serializable field) {
        return redisTemplate.boundHashOps(key).get(field);
    }

    @Override
    public void hdel(String key, Serializable field) {
        redisTemplate.boundHashOps(key).delete(field);
    }

    @Override
    public void sadd(String key, Serializable value) {
        redisTemplate.boundSetOps(key).add(value);
    }

    @Override
    public Set<?> sall(String key) {
        return redisTemplate.boundSetOps(key).members();
    }

    @Override
    public boolean sdel(String key, Serializable value) {
        return redisTemplate.boundSetOps(key).remove(value) == 1;
    }
}

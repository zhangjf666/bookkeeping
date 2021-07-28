package com.hc.bookkeeping.common.support.redis;

import com.hc.bookkeeping.common.utils.RedisUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RType;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zjf
 * @Email: junfeng1987@163.com
 * @Description:
 * @Date: 2020/6/4 15:11
 */
public class RedissonRedisManager implements RedisManager{
    private RedissonClient redissonClient;
    private Long EXPIRE = 600L;

    public RedissonRedisManager(RedissonClient client, Long expire){
        this.redissonClient = client;
        this.EXPIRE = expire;
        RedisUtil.setRedissonManager(this);
    }

    public RedissonRedisManager(RedissonClient client){
        this.redissonClient = client;
        RedisUtil.setRedissonManager(this);
    }

    public void setRedissonClient(RedissonClient client) {
        this.redissonClient = client;
        RedisUtil.setRedissonManager(this);
    }

    private RBucket<Object> getRedisBucket(String key) {
        return redissonClient.getBucket(key);
    }

    @Override
    public final Object get(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.get();
    }

    @Override
    public final void set(final String key, final Serializable value) {
        Long ttl = ttl(key);
        RBucket<Object> temp = getRedisBucket(key);
        temp.set(value);
        expire(temp, ttl != null && ttl >0 ? ttl : EXPIRE);
    }

    @Override
    public final void set(final String key, final Serializable value, long seconds) {
        RBucket<Object> temp = getRedisBucket(key);
        temp.set(value);
        expire(temp, seconds);
    }

    public final void multiSet(final Map<String, Object> temps) {
        redissonClient.getBuckets().set(temps);
    }

    @Override
    public final Boolean exists(final String key) {
        RBucket<Object> temp = getRedisBucket(key);
        return temp.isExists();
    }

    @Override
    public final void del(final String key) {
        redissonClient.getKeys().delete(key);
    }

    @Override
    public final void delAll(final String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    @Override
    public final String type(final String key) {
        RType type = redissonClient.getKeys().getType(key);
        if (type == null) {
            return null;
        }
        return type.getClass().getName();
    }

    /**
     * 在某段时间后失效
     *
     * @return
     */
    private final void expire(final RBucket<Object> bucket, final long seconds) {
        bucket.expire(seconds, TimeUnit.SECONDS);
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
        return redissonClient.getBucket(key).expireAt(new Date(unixTime));
    }


    @Override
    public final Long ttl(final String key) {
        RBucket<Object> rBucket = getRedisBucket(key);
        return rBucket.remainTimeToLive();
    }

    @Override
    public final Object getSet(final String key, final Serializable value) {
        RBucket<Object> rBucket = getRedisBucket(key);
        return rBucket.getAndSet(value);
    }

    @Override
    public Set<String> getKeys(String pattern) {
        Set<String> set = new HashSet<>();
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            set.add(key);
        }
        return set;
    }

    @Override
    public Set<Object> getAll(String pattern) {
        Set<Object> set = new HashSet<>();
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            set.add(getRedisBucket(key).get());
        }
        return set;
    }

    @Override
    public Boolean expire(String key, long seconds) {
        RBucket<Object> bucket = getRedisBucket(key);
        expire(bucket, seconds);
        return true;
    }

    @Override
    public void hset(String key, Serializable field, Serializable value) {
        redissonClient.getMap(key).put(field, value);
    }

    @Override
    public Object hget(String key, Serializable field) {
        return redissonClient.getMap(key).get(field);
    }

    @Override
    public void hdel(String key, Serializable field) {
        redissonClient.getMap(key).remove(field);
    }

    @Override
    public void sadd(String key, Serializable value) {
        redissonClient.getSet(key).add(value);
    }

    @Override
    public Set<Object> sall(String key) {
        return redissonClient.getSet(key).readAll();
    }

    @Override
    public boolean sdel(String key, Serializable value) {
        return redissonClient.getSet(key).remove(value);
    }

    @Override
    public void lock(String key, long leaseTime, TimeUnit unit) throws InterruptedException {
        redissonClient.getLock(key).lock(leaseTime, unit);
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return redissonClient.getLock(key).tryLock(waitTime,leaseTime,unit);
    }

    @Override
    public void unlock(String key) {
        redissonClient.getLock(key).unlock();
    }

    @Override
    public boolean setnx(String key, Serializable value) {
        RBucket<Object> bucket = getRedisBucket(key);
        return bucket.trySet(value);
    }
}

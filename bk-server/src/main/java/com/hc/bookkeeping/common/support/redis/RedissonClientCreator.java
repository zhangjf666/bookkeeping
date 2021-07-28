package com.hc.bookkeeping.common.support.redis;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: zhangjunfeng
 * @Email: junfeng1987@163.com
 * @Description: redis 连接配置
 * @Date: Created on 2017/11/7 15:05
 */
public class RedissonClientCreator {
    /**
     * Redis server address
     *
     */
    private String address;

    /**
     * Password for Redis authentication. Should be null if not needed
     */
    private String password;

    /**
     * Redis cluster node urls list
     */
    private Set<String> nodeAddresses = new HashSet<String>();

    /**
     * Redis master server address
     */
    private String masterAddress;

    /**
     * Redis slave servers addresses
     */
    private Set<String> slaveAddresses = new HashSet<String>();

    /**
     * Redis sentinel servers addresses
     */
    private Set<String> sentinelServerAddress = new HashSet<String>();

    private String masterName;

    private RedissonClient redissonClient;

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void init() {
        Config config = new Config();
        if (StringUtils.isNotBlank(address)) {
            SingleServerConfig serverConfig = config.useSingleServer().setAddress(address);
            if (StringUtils.isNotBlank(password)) {
                serverConfig.setPassword(password);
            }
        } else if (!nodeAddresses.isEmpty()) {
            ClusterServersConfig serverConfig = config.useClusterServers()
                    .addNodeAddress(nodeAddresses.toArray(new String[]{}));
            if (StringUtils.isNotBlank(password)) {
                serverConfig.setPassword(password);
            }
        } else if (masterAddress != null && !slaveAddresses.isEmpty()) {
            MasterSlaveServersConfig serverConfig = config.useMasterSlaveServers().setMasterAddress(masterAddress)
                    .addSlaveAddress(slaveAddresses.toArray(new String[]{}));
            if (StringUtils.isNotBlank(password)) {
                serverConfig.setPassword(password);
            }
        } else if (!sentinelServerAddress.isEmpty()) {
            SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(sentinelServerAddress.toArray(new String[]{})).setMasterName(masterName);
            if (StringUtils.isNotBlank(password)) {
                serverConfig.setPassword(password);
            }
        }

        this.redissonClient = Redisson.create(config);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNodeAddresses(String nodeAddresse) {
        if (nodeAddresse != null) {
            String[] nodeAddresses = nodeAddresse.split(",");
            for (int i = 0; i < nodeAddresses.length; i++) {
                if (StringUtils.isNotEmpty(nodeAddresses[i])) {
                    this.nodeAddresses.add(nodeAddresses[i]);
                }
            }
        }
    }



    public void setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
    }

    public void setSlaveAddresses(String slaveAddresse) {
        if (slaveAddresse != null) {
            String[] slaveAddresses = slaveAddresse.split(",");
            for (int i = 0; i < slaveAddresses.length; i++) {
                if (StringUtils.isNotEmpty(slaveAddresses[i])) {
                    this.slaveAddresses.add(slaveAddresses[i]);
                }
            }
        }
    }
    public void setSentinelServerAddress(String nodeAddresse) {
        String[] redisUris = nodeAddresse.split(",");
        for(String uri:redisUris)
        {
            sentinelServerAddress.add(String.format("redis://%s",uri));
        }
    }
    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

}

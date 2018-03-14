/*
 * Copyright (c) 2014-2015 ,fangzy (zyuanf@gmail.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.fangzy.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Map;

/**
 * Created on 2014/9/26.
 *
 * @author FZY
 */
@Component
public class JedisHolder implements InitializingBean, ApplicationContextAware {

    public static final String DEFAULT = "default";
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisHolder.class);
    private static final NamedThreadLocal<JedisResource<Jedis>> JEDIS_THREAD_LOCAL = new NamedThreadLocal<JedisResource<Jedis>>("jedis resource holder") {
        @Override
        protected JedisResource<Jedis> initialValue() {
            return new JedisResource<>(null);
        }
    };

    private static final NamedThreadLocal<JedisResource<ShardedJedis>> SHARDED_JEDIS_THREAD_LOCAL = new NamedThreadLocal<JedisResource<ShardedJedis>>("shardedJedis resource holder") {
        @Override
        protected JedisResource<ShardedJedis> initialValue() {
            return new JedisResource<>(null);
        }
    };

    private Map<String, JedisPool> jedisPoolMap;

    private ShardedJedisPool shardedJedisPool;

    @Autowired
    private RedisInterceptor redisInterceptor;

    public Jedis get() {
        JedisResource<Jedis> jedisResource = JEDIS_THREAD_LOCAL.get();
        if (jedisResource.hasJedis()) {
            return jedisResource.getJedis();
        }
        return jedisPoolMap.get(DEFAULT).getResource();
    }

    public ShardedJedis getShardedJedis() {
        JedisResource<ShardedJedis> jedisResource = SHARDED_JEDIS_THREAD_LOCAL.get();
        if (jedisResource.hasJedis()) {
            return jedisResource.getJedis();
        } else {
            throw new IllegalArgumentException("no sharded jedis connection.");
        }
    }

    public boolean hasJedis() {
        return JEDIS_THREAD_LOCAL.get().hasJedis();
    }

    public boolean hasShardJedis() {
        return SHARDED_JEDIS_THREAD_LOCAL.get().hasJedis();
    }

    public void createResource(String val) {
        Jedis jedis;
        JedisResource<Jedis> jedisResource = JEDIS_THREAD_LOCAL.get();
        if (jedisResource.hasJedis()) {
            jedisResource.incrementAndGet();
            return;
        }
        if (val.isEmpty()) {
            jedis = jedisPoolMap.get(DEFAULT).getResource();
        } else {
            jedis = jedisPoolMap.get(val).getResource();
        }
        jedisResource.setJedis(jedis);
    }

    public void createShardedResource() {
        JedisResource<ShardedJedis> jedisResource = SHARDED_JEDIS_THREAD_LOCAL.get();
        if (jedisResource.hasJedis()) {
            jedisResource.incrementAndGet();
            return;
        }
        ShardedJedis shardedJedis = shardedJedisPool.getResource();
        jedisResource.setJedis(shardedJedis);
    }

    public void release(Jedis jedis) {
        if (JEDIS_THREAD_LOCAL.get().hasJedis()) {
            return;
        }
        returnResource(jedis);
        JEDIS_THREAD_LOCAL.remove();
    }

    public void releaseForce() {
        JedisResource<Jedis> jedisResource = JEDIS_THREAD_LOCAL.get();
        if (!jedisResource.hasJedis()) {
            return;
        }
        int c = jedisResource.decrementAndGet();
        if (c == 0) {
            returnResource(jedisResource.getJedis());
            JEDIS_THREAD_LOCAL.remove();
        }
    }

    public void releaseShardedForce() {
        JedisResource<ShardedJedis> jedisResource = SHARDED_JEDIS_THREAD_LOCAL.get();
        if (!jedisResource.hasJedis()) {
            return;
        }
        int c = jedisResource.decrementAndGet();
        if (c > 0) {
            return;
        }
        ShardedJedis shardedJedis = jedisResource.getJedis();
        shardedJedis.close();
        SHARDED_JEDIS_THREAD_LOCAL.remove();
    }

    private void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public void setJedisPoolMap(Map<String, JedisPool> jedisPoolMap) {
        this.jedisPoolMap = jedisPoolMap;
    }

    public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
        this.shardedJedisPool = shardedJedisPool;
    }

    public int getConnectionNum(String connName) {
        return jedisPoolMap.getOrDefault(connName, jedisPoolMap.get(DEFAULT)).getNumActive();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        redisInterceptor.setJedisHolder(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Map<String, JedisPool> jedisPoolMap = applicationContext.getBeansOfType(JedisPool.class);
            setJedisPoolMap(jedisPoolMap);
            ShardedJedisPool shardedJedisPool = applicationContext.getBean(ShardedJedisPool.class);
            setShardedJedisPool(shardedJedisPool);
        } catch (BeansException e) {
            LOGGER.warn(e.getMessage());
        }
    }
}

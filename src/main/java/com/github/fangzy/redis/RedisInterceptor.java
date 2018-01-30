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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

/**
 * Created on 2014/9/27.
 *
 * @author FZY
 */
@Component
public class RedisInterceptor implements MethodInterceptor {

    private JedisHolder jedisHolder;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Redis redis = invocation.getMethod().getDeclaredAnnotation(Redis.class);
        if (redis == null) {
            throw new IllegalArgumentException("Can not find @Redis annotation.");
        }
        if (redis.shard()) {
            return shardedRedis(invocation);
        } else {
            return normalRedis(invocation, redis);
        }
    }

    private Object shardedRedis(MethodInvocation invocation) throws Throwable {
        try {
            jedisHolder.createShardedResource();
            return invocation.proceed();
        } finally {
            jedisHolder.releaseShardedForce();
        }
    }

    private Object normalRedis(MethodInvocation invocation, Redis redis) throws Throwable {
        String val = redis.value();
        try {
            jedisHolder.createResource(val);
            return invocation.proceed();
        } finally {
            jedisHolder.releaseForce();
        }
    }

    public void setJedisHolder(JedisHolder jedisHolder) {
        this.jedisHolder = jedisHolder;
    }
}

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

package org.reindeer.redis.shard;

import org.reindeer.redis.AbstractCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;

import java.lang.reflect.Method;

/**
 * ShardedJedis拦截器
 * Created by fzy on 2014/7/6.
 */
@Component
public class ShardedJedisCallback extends AbstractCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedJedisCallback.class);

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        ShardedJedis jedis;
        boolean status = jedisHolder.hasShardJedis();
        if (!status) {
            throw new UnsupportedOperationException("ShardedJedis proxy need use @Redis annotation.");
        }
        try {
            jedis = jedisHolder.getShardedJedis();
            return methodProxy.invoke(jedis, objects);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

}

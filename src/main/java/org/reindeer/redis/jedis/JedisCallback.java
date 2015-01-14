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

package org.reindeer.redis.jedis;

import org.reindeer.redis.AbstractCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;

/**
 * Jedis拦截器
 * Created by fzy on 2014/7/6.
 */
@Component
public class JedisCallback extends AbstractCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisCallback.class);

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Jedis jedis = null;
        boolean isBroken = false;
        filterMethod(method);
        try {
            jedis = jedisHolder.get();
            return methodProxy.invoke(jedis, objects);
        } catch (Exception e) {
            isBroken = true;
            LOGGER.error(e.getMessage());
            throw e;
        } finally {
            release(jedis, isBroken);
        }
    }

    private void release(Jedis jedis, boolean isBroken) {
        if (jedis == null) {
            return;
        }
        jedisHolder.release(jedis, isBroken);
    }

}

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

import org.reindeer.redis.JedisCallbackFilter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * Jedis代理
 * Created by fzy on 2014/7/6.
 */
@Component
public class JedisFactoryBean implements FactoryBean<Jedis> {

    private final CallbackFilter finalizeFilter = new JedisCallbackFilter();
    @Autowired
    private JedisCallback jedisCallback;
    private Jedis jedis;

    @Override
    public Jedis getObject() throws Exception {
        if (jedis != null) {
            return jedis;
        }
        Enhancer en = new Enhancer();
        en.setSuperclass(Jedis.class);
        en.setCallbackFilter(finalizeFilter);
        en.setCallbacks(new Callback[]{NoOp.INSTANCE, jedisCallback});
        jedis = (Jedis) en.create(new Class[]{String.class}, new Object[]{"JedisProxy"});
        return jedis;
    }

    @Override
    public Class getObjectType() {
        return Jedis.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

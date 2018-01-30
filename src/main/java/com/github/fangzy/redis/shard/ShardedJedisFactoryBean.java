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

package com.github.fangzy.redis.shard;

import com.github.fangzy.redis.JedisCallbackFilter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

import java.util.Arrays;
import java.util.List;

/**
 * ShardedJedis代理
 * Created by fzy on 2014/7/6.
 */
@Component
public class ShardedJedisFactoryBean implements FactoryBean<ShardedJedis> {

    private final CallbackFilter finalizeFilter = new JedisCallbackFilter();
    @Autowired
    private ShardedJedisCallback jedisCallback;
    private ShardedJedis jedis;

    @Override
    public ShardedJedis getObject() throws Exception {
        if (jedis != null) {
            return jedis;
        }
        Enhancer en = new Enhancer();
        en.setSuperclass(ShardedJedis.class);
        en.setCallbackFilter(finalizeFilter);
        en.setCallbacks(new Callback[]{NoOp.INSTANCE, jedisCallback});
        jedis = (ShardedJedis) en.create(new Class[]{List.class}, new Object[]{Arrays.asList(new JedisShardInfo("shardedJedisProxy"))});
        return jedis;
    }

    @Override
    public Class getObjectType() {
        return ShardedJedis.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

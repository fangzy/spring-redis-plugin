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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * Jedis代理
 * Created by fzy on 2014/7/6.
 */
@Component
public class JedisProxy implements ApplicationContextAware {

    private static volatile ApplicationContext ac;

    /**
     * 创建Jedis 代理
     *
     * @return
     */
    public static Jedis create() {
        return ac.getBean(Jedis.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (ac != null) {
            return;
        }
        synchronized (JedisProxy.class) {
            if (ac != null) {
                return;
            }
            ac = applicationContext;
        }
    }
}

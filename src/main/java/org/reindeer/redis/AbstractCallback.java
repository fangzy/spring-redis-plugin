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

package org.reindeer.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * Created on 2014/9/26.
 *
 * @author FZY
 */
public abstract class AbstractCallback implements MethodInterceptor {

    @Autowired
    protected JedisHolder jedisHolder;

    protected void filterMethod(Method method) {
        boolean status = jedisHolder.hasJedis();
        filterNoSupportMethod(method, "pipelined", status);
        filterNoSupportMethod(method, "watch", status);
        filterNoSupportMethod(method, "unwatch", status);
        filterNoSupportMethod(method, "multi", status);
    }

    protected void filterNoSupportMethod(Method method, String methodName, boolean status) {
        if (method.getName().equals(methodName) && !status) {
            throw new UnsupportedOperationException("Jedis proxy does not support " + methodName + " method.");
        }
    }
}

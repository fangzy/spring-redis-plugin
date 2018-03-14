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

import redis.clients.jedis.BinaryJedisCommands;

/**
 * Created on 2014/12/11.
 *
 * @author FZY
 */
public class JedisResource<T extends BinaryJedisCommands> {

    private T t;

    private int connectionNum = 0;

    public JedisResource(T t) {
        this.t = t;
    }

    public T getJedis() {
        return t;
    }

    public void setJedis(T t) {
        this.t = t;
        incrementAndGet();
    }

    public int incrementAndGet() {
        this.connectionNum++;
        return connectionNum;
    }

    public int decrementAndGet() {
        this.connectionNum--;
        return connectionNum;
    }

    public boolean hasJedis() {
        return t != null;
    }

}

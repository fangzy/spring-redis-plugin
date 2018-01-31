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
import redis.clients.jedis.Jedis;

/**
 * redis实现的分布式锁
 * Created by fzy on 2014/9/1.
 */
public final class JLock {

    public static final int EXPIRE = 1200;
    private static final String LOCK = "redis:lock:%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(JLock.class);

    private JLock() {

    }

    /**
     * 获得锁,未取得时一直阻塞,超时时间默认1200s;
     *
     * @param id
     */
    public static void getLock(String id) {
        getLock(id, 0);
    }

    /**
     * 获得锁,超时退出
     *
     * @param id
     * @param timeout 超时时间(ms)
     * @return
     */
    public static boolean getLock(String id, long timeout) {
        Jedis jedis = JedisProxy.create();
        long lock = 0;
        long start = System.currentTimeMillis();
        long pexpire = timeout > 0 ? timeout : EXPIRE * 1000;
        while (lock != 1) {
            long now = System.currentTimeMillis();
            //判断超时
            if (timeout > 0 && now > start + timeout) {
                return false;
            }
            long timestamp = now + EXPIRE + 1;
            String key = String.format(LOCK, id);
            lock = jedis.setnx(key, String.valueOf(timestamp));
            if (lock == 1) {
                jedis.pexpire(key, pexpire);
                LOGGER.trace("redis lock setnx");
            } else {
                sleep();
            }
        }
        return true;
    }

    /**
     * 检查锁是否存在,立刻返回
     *
     * @param id
     * @param timeout
     * @return
     */
    public static boolean checkLock(String id, long timeout) {
        Jedis jedis = JedisProxy.create();
        String key = String.format(LOCK, id);
        long exists = jedis.setnx(key, String.valueOf(System.currentTimeMillis()));
        if (exists == 0) {
            return false;
        }
        jedis.pexpire(key, timeout);
        return true;
    }

    private static void sleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOGGER.error("An unexpected error occurred.", e);
        }
    }

    /**
     * 释放锁
     *
     * @param id
     */
    public static void releaseLock(String id) {
        Jedis jedis = JedisProxy.create();
        String key = String.format(LOCK, id);
        String val = jedis.get(key);
        if (val == null) {
            return;
        }
        long lastLockTime = Long.parseLong(val);
        if (lastLockTime > System.currentTimeMillis() / 1000) {
            jedis.del(key);
        }
    }
}

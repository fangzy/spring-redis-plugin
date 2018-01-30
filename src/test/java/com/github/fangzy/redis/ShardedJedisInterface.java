package com.github.fangzy.redis;

/**
 * Created on 2014/10/30.
 *
 * @author FZY
 */
public interface ShardedJedisInterface {

    void incr(String key, int times);
}

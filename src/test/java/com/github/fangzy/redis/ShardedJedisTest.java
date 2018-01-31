package com.github.fangzy.redis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * Created on 2014/10/30.
 *
 * @author FZY
 */
public class ShardedJedisTest extends AbstractTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedJedisTest.class);

    @Autowired
    private ShardedJedisInterface shardedJedisDemo;

    @Autowired
    private JedisServiceDemo demo;

    private String key = "test:shardedJedis";

    private int times = 100000;

    @Autowired
    @Qualifier("jedisPool")
    private JedisPool jedisPool;

    @Autowired
    @Qualifier("jedisPool6380")
    private JedisPool jedisPool6380;

    @Test
    public void shardedPiplineTest() {
        StopWatch stopWatch = new StopWatch("jedis pipeline benchmark");

        stopWatch.start("jedis proxy pipeline");
        demo.incr(key, times);
        stopWatch.stop();

        stopWatch.start("shardedJedis proxy pipeline");
        shardedJedisDemo.incr(key, times);
        stopWatch.stop();

        LOGGER.info(stopWatch.prettyPrint());

        Jedis jedis1 = jedisPool.getResource();
        Set<String> set1 = jedis1.keys("test:shardedJedis*");
        LOGGER.info(String.valueOf(set1.size()));
        jedis1.del(set1.toArray(new String[set1.size()]));

        Jedis jedis2 = jedisPool6380.getResource();
        Set<String> set2 = jedis2.keys("test:shardedJedis*");
        LOGGER.info(String.valueOf(set2.size()));
        jedis2.del(set2.toArray(new String[set2.size()]));

        jedis1.close();
        jedis2.close();
    }

}

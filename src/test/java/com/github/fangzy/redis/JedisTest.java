package com.github.fangzy.redis;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Created on 2014/9/26.
 *
 * @author FZY
 */
public class JedisTest extends AbstractTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisTest.class);

    @Autowired
    private JedisServiceDemo demo;

    @Autowired
    @Qualifier("jedisPool")
    private JedisPool jedisPool;

    private int times = 10000;

    private String key = "test:jedis";

    @Test
    public void testPipelineBenchmark() throws Exception {
        StopWatch stopWatch = new StopWatch("jedis pipeline benchmark");
        demo.incr(key, times);
        jedisIncr(key, times);

        System.gc();
        Thread.sleep(5000);

        stopWatch.start("jedis normal pipeline");
        jedisIncr(key, times);
        stopWatch.stop();

        System.gc();
        Thread.sleep(5000);

        stopWatch.start("jedis proxy pipeline");
        demo.incr(key, times);
        stopWatch.stop();

        LOGGER.info(stopWatch.prettyPrint());
        LOGGER.info("jedis pool info:{}", jedisPool.getNumActive());
    }

    @Test
    public void testSetAndGetBenchmark() throws Exception {
        StopWatch stopWatch = new StopWatch("jedis setAndGet benchmark");
        demo.setAndGet(key, times / 100);
        demo.setAndGet2(key, times / 100);
        jedisSetAndGet(key, times / 100);

        System.gc();
        Thread.sleep(5000);

        stopWatch.start("jedis proxy setAndGet");
        demo.setAndGet(key, times / 100);
        stopWatch.stop();

        System.gc();
        Thread.sleep(5000);

        stopWatch.start("jedis normal setAndGet");
        jedisSetAndGet(key, times / 100);
        stopWatch.stop();

        System.gc();
        Thread.sleep(5000);

        stopWatch.start("jedis proxy setAndGet2");
        demo.setAndGet2(key, times / 100);
        stopWatch.stop();

        LOGGER.info(stopWatch.prettyPrint());
        LOGGER.info("jedis pool info:{}", jedisPool.getNumActive());
    }

    private void jedisIncr(String key, int times) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipeline.set(key, "1");
            for (int i = 0; i < times; i++) {
                pipeline.incr(key);
            }
            Response<String> response = pipeline.get(key);
            pipeline.sync();
            LOGGER.info(response.get());
            jedis.del(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
    }


    private void jedisSetAndGet(String key, int times) {
        Jedis jedis = null;
        boolean isBroken = false;
        try {
            jedis = jedisPool.getResource();
            for (int i = 0; i < times; i++) {
                jedis.set(key, "test1");
                jedis.get(key);
            }
            LOGGER.info(jedis.get(key));
            jedis.del(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
    }

    private void release(Jedis jedis, boolean isBroken) {
        if (jedis != null) {
            if (isBroken) {
                jedisPool.returnBrokenResource(jedis);
            } else {
                jedisPool.returnResource(jedis);
            }
        }
    }
}

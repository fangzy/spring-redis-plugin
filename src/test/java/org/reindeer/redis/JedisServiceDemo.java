package org.reindeer.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Created on 2014/9/26.
 *
 * @author FZY
 */
@Service
public class JedisServiceDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisServiceDemo.class);

    @Autowired
    private Jedis jedis;

    @Redis
    public void incr(String key, int times) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.set(key, "1");
        for (int i = 0; i < times; i++) {
            pipeline.incr(key + i);
        }
        Response<String> response = pipeline.get(key + 1);
        pipeline.sync();
        LOGGER.info(response.get());
        jedis.del(key);
    }

    public void setAndGet(String key, int times) {
        for (int i = 0; i < times; i++) {
            jedis.set(key, "test1");
            jedis.get(key);
        }
        LOGGER.info(jedis.get(key));
        jedis.del(key);
    }

    @Redis("test6380")
    public void setAndGet2(String key, int times) {
        for (int i = 0; i < times; i++) {
            jedis.set(key, "test1");
            jedis.get(key);
        }
        LOGGER.info(jedis.get(key));
        jedis.del(key);
    }
}

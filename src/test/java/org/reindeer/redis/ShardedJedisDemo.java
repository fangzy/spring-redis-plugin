package org.reindeer.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;

/**
 * Created on 2014/10/30.
 *
 * @author FZY
 */
@Service
public class ShardedJedisDemo implements ShardedJedisInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedJedisDemo.class);

    @Autowired
    private ShardedJedis shardedJedis;

    @Override
    @Redis(shard = true)
    public void incr(String key, int times) {
        ShardedJedisPipeline pipeline = shardedJedis.pipelined();
        for (int i = 0; i < times; i++) {
            pipeline.incr(key + i);
        }
        Response<String> response = pipeline.get(key + 1);
        pipeline.sync();
        LOGGER.info(response.get());
    }

}

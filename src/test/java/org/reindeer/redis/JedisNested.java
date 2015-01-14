package org.reindeer.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created on 2014/12/11.
 *
 * @author FZY
 */
@Service
public class JedisNested {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisNested.class);

    @Autowired
    private JedisServiceDemo demo;

    @Autowired
    private ShardedJedisDemo shardedDemo;

    @Redis
    public void incr(String key, int times) {
        for (int i = 0; i < 100; i++) {
            demo.incr(key, times);
            shardedDemo.incr(key, times);
        }
    }

}

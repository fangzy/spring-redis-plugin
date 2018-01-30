package com.github.fangzy.redis;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created on 2014/12/11.
 *
 * @author FZY
 */
public class JedisHolderTest extends AbstractTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisHolderTest.class);

    @Autowired
    private JedisNested jedisNested;

    @Autowired
    private JedisHolder jedisHolder;

    private int times = 5;

    private String key = "test:jedis";

    @Test
    public void testCreateAndRelease() {
        jedisNested.incr(key, times);
        LOGGER.info(String.valueOf(jedisHolder.getConnectionNum(null)));
        Assert.assertEquals(0, jedisHolder.getConnectionNum(null));
    }

}

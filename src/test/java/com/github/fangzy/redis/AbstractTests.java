package com.github.fangzy.redis;

import com.github.fangzy.redisconfig.TestRedisConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Created on 2014/9/27.
 *
 * @author FZY
 */
@Configuration
@TestPropertySource("classpath:redisPool.properties")
@ContextConfiguration(classes = TestRedisConfiguration.class)
public abstract class AbstractTests extends AbstractJUnit4SpringContextTests {

}

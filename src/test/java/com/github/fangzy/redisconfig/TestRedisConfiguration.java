package com.github.fangzy.redisconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Configuration
@ComponentScan("com.github.fangzy.redis")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TestRedisConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRedisConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public JedisPoolConfig defaultJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(env.getProperty("redis.maxTotal", Integer.class, 200));
        jedisPoolConfig.setMaxIdle(env.getProperty("redis.maxIdle", Integer.class, 20));
        jedisPoolConfig.setMinIdle(env.getProperty("redis.minIdle", Integer.class, 0));
        jedisPoolConfig.setMaxWaitMillis(env.getProperty("redis.maxWait", Integer.class, 3000));
        jedisPoolConfig.setMinEvictableIdleTimeMillis(env.getProperty("redis.minEvictableIdleTimeMillis", Long.class, 60000L));
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(env.getProperty("redis.timeBetweenEvictionRunsMillis", Long.class, 120000L));
        jedisPoolConfig.setNumTestsPerEvictionRun(env.getProperty("redis.numTestsPerEvictionRun", Integer.class, 1));
        jedisPoolConfig.setTestOnBorrow(env.getProperty("redis.testOnBorrow", Boolean.class, false));
        jedisPoolConfig.setTestOnReturn(env.getProperty("redis.testOnReturn", Boolean.class, false));
        jedisPoolConfig.setTestWhileIdle(env.getProperty("redis.testWhileIdle", Boolean.class, true));
        return jedisPoolConfig;
    }

    @Bean(name = "default", destroyMethod = "destroy")
    public JedisPool defaultJedisPool() {
        return new JedisPool(defaultJedisPoolConfig(),
                env.getProperty("redis.host", "127.0.0.1"),
                env.getProperty("redis.port", Integer.class, 6379),
                Protocol.DEFAULT_TIMEOUT,
                StringUtils.isEmpty(env.getProperty("redis.password")) ? null : env.getProperty("redis.password"));
    }

    @Bean(name = "test", destroyMethod = "destroy")
    public JedisPool testJedisPool() {
        return new JedisPool(defaultJedisPoolConfig(),
                env.getProperty("redis.host2", "127.0.0.1"),
                env.getProperty("redis.port2", Integer.class, 6379),
                Protocol.DEFAULT_TIMEOUT,
                StringUtils.isEmpty(env.getProperty("redis.password2")) ? null : env.getProperty("redis.password2"));
    }

    @Bean(destroyMethod = "destroy")
    public ShardedJedisPool defaultShardedJedisPool() {
        String hoststr = env.getProperty("redis.sharded.hosts", "127.0.0.1");
        String portstr = env.getProperty("redis.sharded.ports", "6379");
        List<String> hosts = Arrays.asList(hoststr.split(","));
        List<Integer> ports = Stream.of(portstr.split(",")).map(Integer::new).collect(Collectors.toList());

        List<JedisShardInfo> list = IntStream.range(0, hosts.size()).mapToObj(i -> new JedisShardInfo(hosts.get(i), ports.get(i))).collect(Collectors.toList());
        return new ShardedJedisPool(defaultJedisPoolConfig(), list);
    }

}

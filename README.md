spring-redis-plugin  [![Build Status](https://travis-ci.org/fangzy/spring-redis-plugin.svg?branch=master)](https://travis-ci.org/fangzy/spring-redis-plugin) [![Release](https://jitpack.io/v/fangzy/spring-redis-plugin.svg)](https://jitpack.io/#fangzy/spring-redis-plugin)
===========
本项目是[Jedis](https://github.com/xetorthio/jedis)基于spring的增强版,实现了自动获取连接和关闭连接,提供基于redis的分布式锁实现.

## 1 Maven POM 配置

使用[Jitpack](https://jitpack.io/#fangzy/spring-redis-plugin)仓库

### 1.1 添加仓库地址

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### 1.2 添加依赖

```xml
<dependency>
    <groupId>com.github.fangzy</groupId>
    <artifactId>spring-redis-plugin</artifactId>
    <version>1.5.1</version>
</dependency>
```

### 1.3 依赖`spring-context`项目

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>4.X.X.RELEASE</version>
    <exclusions>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 2 Jedis使用方法

### 2.1 Spring Configuration配置文件

> 直接使用默认配置

```java
@Configuration
@PropertySource("classpath:redisPool.properties")
@Import(DefaultRedisConfiguration.class)
public class RedisConfig {
    
}

```

> 自定义配置参见[`DefaultRedisConfiguration`](/src/main/java/com/github/fangzy/redisconfig/DefaultRedisConfiguration.java)、[`DefaultShardedRedisConfiguration`](/src/main/java/com/github/fangzy/redisconfig/DefaultShardedRedisConfiguration.java)

### 2.2 Spring XML配置文件

```xml
<context:property-placeholder location="classpath:your-redis-settings.properties" file-encoding="UTF-8"/>

<!--redis pool-->
<bean id="jedisPoolFactoryConfig" class="redis.clients.jedis.JedisPoolConfig">
    <property name="maxTotal" value="${redis.maxTotal}"/>
    <property name="maxIdle" value="${redis.maxIdle}"/>
    <property name="maxWaitMillis" value="${redis.maxWait}"/>
    <property name="minIdle" value="${redis.minIdle}"/>
    <property name="numTestsPerEvictionRun" value="${redis.numTestsPerEvictionRun}"/>
    <property name="testOnBorrow" value="${redis.testOnBorrow}"/>
    <property name="testOnReturn" value="${redis.testOnReturn}"/>
    <property name="testWhileIdle" value="${redis.testWhileIdle}"/>
</bean>

<bean id="default" class="redis.clients.jedis.JedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="${redis.port}" name="port" type="int"/>
</bean>
<bean id="jedisPool6380" class="redis.clients.jedis.JedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="6380" name="port" type="int"/>
</bean>

<!-- shard redis -->
<bean id="jedisShardInfo1" class="redis.clients.jedis.JedisShardInfo">
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="${redis.port}" name="port" type="int"/>
</bean>

<bean id="jedisShardInfo2" class="redis.clients.jedis.JedisShardInfo">
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="6380" name="port" type="int"/>
</bean>

<bean id="shardedJedisPool" class="redis.clients.jedis.ShardedJedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg name="shards">
        <list>
            <ref bean="jedisShardInfo1"/>
            <ref bean="jedisShardInfo2"/>
        </list>
    </constructor-arg>
</bean>

<context:component-scan base-package="org.reindeer.reids" />

<!--使用@Redis注解时必须加上-->
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

> 请按照需要选择配置

### 2.3 依赖注入

```java
@Service
public class JedisDemo {

    @Autowired
    private Jedis jedis;

    public void setAndGet(String key,int times) {
        for (int i=0;i<times;i++) {
            jedis.set(key, "test1");
            jedis.get(key);
        }
        jedis.del(key);
    }
}
```

> 如果需要使用pipelined,watch,unwatch,multi 方法必须开启注解,详见2.4

### 2.4 使用注解

> jedisPool的default为默认数据源，必须存在

```java
@Service
public class JedisDemo {

    @Autowired
    private Jedis jedis;

    @Redis
    public void incr(String key,int times) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.set(key, "1");
        for (int i = 0; i < times; i++) {
            pipeline.incr(key);
        }
        Response<String> response = pipeline.get(key);
        pipeline.sync();
        jedis.del(key);
    }
}
```

### 2.5 使用JedisProxy

```java
public class JedisDemo {

    public static void setAndGet(String key,int times) {
        Jedis jedis = JedisProxy.create();
        for (int i=0;i<times;i++) {
            jedis.set(key, "test1");
            jedis.get(key);
        }
        jedis.del(key);
    }
}
```

> JedisProxy 通常用于静态方法,但是需要在spring容器全部加载完毕后使用
  如果需要使用pipelined,watch,unwatch,multi 方法必须开启注解,详见2.4

### 2.6 使用多数据源

```java
@Service
public class JedisDemo {

    @Autowired
    private Jedis jedis;

    @Redis("anotherRedis")
    public void incr(String key,int times) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.set(key, "1");
        for (int i = 0; i < times; i++) {
            pipeline.incr(key);
        }
        Response<String> response = pipeline.get(key);
        pipeline.sync();
        jedis.del(key);
    }
}
```

> java config 使用`@Bean(name="anotherRedis")`来定义数据源名称

```java
@Configuration
public class RedisConfiguration {
    @Bean(name = "default", destroyMethod = "destroy")
    public JedisPool defaultJedisPool() {
            return new JedisPool(defaultJedisPoolConfig(),
                    env.getProperty("redis.host", "127.0.0.1"),
                    env.getProperty("redis.port", Integer.class, 6379),
                    Protocol.DEFAULT_TIMEOUT,
                    StringUtils.isEmpty(env.getProperty("redis.password")) ? null : env.getProperty("redis.password"));
    }
    
    @Bean(name = "anotherRedis", destroyMethod = "destroy")
    public JedisPool testJedisPool() {
        return new JedisPool(defaultJedisPoolConfig(),
                env.getProperty("redis.host2", "127.0.0.1"),
                env.getProperty("redis.port2", Integer.class, 6379),
                Protocol.DEFAULT_TIMEOUT,
                StringUtils.isEmpty(env.getProperty("redis.password2")) ? null : env.getProperty("redis.password2"));
    }
}

```

> xml配置

```xml
<bean id="default" class="redis.clients.jedis.JedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="${redis.port}" name="port" type="int"/>
</bean>
<bean id="anotherRedis" class="redis.clients.jedis.JedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="6380" name="port" type="int"/>
</bean>
```

> jedisPool的default为默认数据源，必须存在

### 2.7 使用shard

```java
@Service
public class ShardedJedisDemo {

    @Autowired
    private ShardedJedis shardedJedis;

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
```

> java config配置

```java
@Configuration
public class RedisConfiguration {
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
```

> xml配置

```xml
<bean id="jedisShardInfo1" class="redis.clients.jedis.JedisShardInfo">
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="${redis.port}" name="port" type="int"/>
</bean>

<bean id="jedisShardInfo2" class="redis.clients.jedis.JedisShardInfo">
    <constructor-arg value="${redis.ip}" name="host" type="java.lang.String"/>
    <constructor-arg value="6380" name="port" type="int"/>
</bean>

<bean id="shardedJedisPool" class="redis.clients.jedis.ShardedJedisPool" destroy-method="destroy">
    <constructor-arg ref="jedisPoolFactoryConfig" name="poolConfig"/>
    <constructor-arg name="shards">
        <list>
            <ref bean="jedisShardInfo1"/>
            <ref bean="jedisShardInfo2"/>
        </list>
    </constructor-arg>
</bean>
```

## 3 JLock分布式锁

- void waitLock(String key) 一直等待解锁
- boolean getLock(String key) 获得锁,超时退出
- boolean checkLock(String key) 检查锁是否存在,立即返回
- void releaseLock(String key) 手动释放锁
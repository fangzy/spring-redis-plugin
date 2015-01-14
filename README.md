spring-redis-plugin  [![Build Status](https://travis-ci.org/fangzy/spring-redis-plugin.svg?branch=master)](https://travis-ci.org/fangzy/spring-redis-plugin)
===========
本项目是[Jedis](https://github.com/xetorthio/jedis)基于spring的增强版,实现了自动获取连接和关闭连接,提供基于redis的分布式锁实现.

## 1 Maven POM 配置

### 1.1 SNAPSHOT

暂未上传中央仓库

### 1.2 正式版

暂无

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

## 2 使用方法

### 2.1 spring配置文件

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

<bean id="jedisPool" class="redis.clients.jedis.JedisPool" destroy-method="destroy">
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
    
<bean id="jedisHolder" class="org.reindeer.redis.JedisHolder" >
    <property name="jedisPoolMap">
        <map>
            <entry key="default" value-ref="jedisPool"/>
            <entry key="test6380" value-ref="jedisPool6380"/>
        </map>
    </property>
    <!--如果使用shard redis-->
    <property name="shardedJedisPool" ref="shardedJedisPool"/>
</bean>

<context:component-scan base-package="org.reindeer.reids" />

<!--使用@Redis注解时必须加上-->
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

> 请按照需要选择配置

### 2.2 依赖注入

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

> 如果需要使用pipelined,watch,unwatch,multi 方法必须开启注解,详见2.3

### 2.3 使用注解

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

### 2.4 使用JedisProxy

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
  如果需要使用pipelined,watch,unwatch,multi 方法必须开启注解,详见2.3

### 2.5 使用多数据源

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

```xml
<bean id="jedisHolder" class="org.reindeer.redis.JedisHolder" >
    <property name="jedisPoolMap">
        <map>
            <entry key="default" value-ref="jedisPool"/>
            <entry key="anotherRedis" value-ref="jedisPoolAnother"/>
        </map>
    </property>
</bean>
```

> jedisPoolMap中的default为默认数据源

### 2.6 使用shard

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

```xml
<bean id="jedisHolder" class="org.reindeer.redis.JedisHolder" >
    <property name="shardedJedisPool" ref="shardedJedisPool"/>
</bean>
```
# Vert.x Beans

Vert.x Beans makes it easy for developers to use Vert.x 3.0 (and above) with Spring.

It does this in two ways:

- Provides useful beans you can add to your application context and inject into your beans. They are `Vertx`, `EventBus`, `FileSystem`, and `SharedData`.
- Allows you to specify your own `MetricsOptions` or `ClusterManager`, simply by having them in your application context.

If you're feeling impatient, just head over to the [example](https://github.com/rworsnop/vertx-beans-example). It demonstrates how to use this library with Spring Boot.

## Getting the beans into your application context

The Vert.x beans provided by this library are in a Spring Java Configuration class. There are a number of ways to
include such a configuration into your application context. One of them is to use the `@ComponentScan` annotation:
```
@ComponentScan("io.vertx.spring")
```
or, if you're using RxJava:
```
@ComponentScan("io.vertx.rxjava.spring")
```

## Using the beans

Once they're in your application context, you can inject the Vert.x beans just like any others:

```
@Resource
private Vertx vertx;

@Resource
private EventBus eventBus;

@Resource
private FileSystem fileSystem;

@Resource
SharedData sharedData;
```

## Configuring Vert.x Beans

Vert.x Beans doesn't require configuration, but there are a number of properties that can be supplied to it via Spring's
property placeholder mechanism. (For example, they'd live in `application.properties` in a basic Spring Boot application.)

Here are the properties:

**`vertx.eventLoopPoolSize`** 
See `VertxOptions.setEventLoopPoolSize`

**`vertx.maxEventLoopExecutionTime`** 
See `VertxOptions.setMaxEventLoopExecutionTime`

**`vertx.warningExceptionTime`** 
See `VertxOptions.setWarningExceptionTime`

**`vertx.blockedThreadCheckInterval`** 
See `VertxOptions.setBlockedThreadCheckInterval`

**`vertx.workerPoolSize`** 
See `VertxOptions.setWorkerPoolSize`

**`vertx.maxWorkerExecutionTime`** 
See `VertxOptions.setMaxWorkerExecutionTime`

**`vertx.internalBlockingPoolSize`** 
See `VertxOptions.setInternalBlockingPoolSize`

**`vertx.haEnabled`** 
See `VertxOptions.setHaEnabled`

**`vertx.haGroup`** 
See `VertxOptions.setHaGroup`

**`vertx.quorumSize`** 
See `VertxOptions.setQuorumSize`

**`vertx.clustered`** 
See `VertxOptions.setClustered`

**`vertx.clusterHost`** 
See `VertxOptions.setClusterHost`

**`vertx.clusterPort`** 
See `VertxOptions.setClusterPort`

**`vertx.clusterPingInterval`** 
See `VertxOptions.setClusterPingInterval`

**`vertx.clusterPingReplyInterval`** 
See `VertxOptions.setClusterPingReplyInterval`

## Providing your own ClusterManager and/or MetricsOptions

To provide your own `ClusterManager` or `MetricsOptions`, you just have to declare the appropriate bean in your 
application context.

For example, if you're using XML:

```
<bean id="clusterManager" class="com.acme.MyClusterManager"/>
```
```
<bean id="metricsOptions" class="io.vertx.ext.dropwizard.DropwizardMetricsOptions">
  <property name="enabled" value="${metricsEnabled:false}"/>
</bean>   
```

## Spring is now the framework

Vert.x 3.0 allows developers to use it as either a framework (with verticles) or a library (in  more of a Node.js style).
Vert.x Beans is best-suited to the latter approach. Do note that this means that there is no longer a concept of "instances", so you
are responsible for making sure you are using a number of event loops commensurate with the number of cores on the machine.
In practice this means calling `Vertx.createHttpServer`, `EventBus.consumer`, etc. multiple times.
# Vert.x Beans

Vert.x Beans makes it easy for developers to use Vert.x 3.0 (and above) with Spring.

It does this in two ways:

- Provides useful beans you can add to your application context and inject into your beans. They are `Vertx`, `EventBus`, `FileSystem`, and `SharedData`.
- Allows you to specify your own `MetricsOptions` or `ClusterManager`, simply by having them in your application context.

If you're feeling impatient, just head over to the [example](https://github.com/rworsnop/vertx-beans-example). It demonstrates how to use this library with Spring Boot.

**Important note:** If you're looking for a library that allows you to inject your application beans into a verticle, you need something
like [spring-vert-ext](https://github.com/amoAHCP/spring-vertx-ext). Vert.x Beans flips this around, taking the opposite approach. See 
[below](spring-is-now-the-framework) for more details on this.

## Getting the beans into your application context

The Vert.x beans provided by this library are in a Spring Java Configuration class. There are a number of ways to
include such a configuration into your application context. One of them is to use the `@Import` annotation:
```
@Import(io.vertxbeans.VertxBeans.class)
```
or, if you're using RxJava:
```
@Import(io.vertxbeans.rxjava.VertxBeans.class)
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

- **`vertx.event-loop-pool-size`** &mdash; See `VertxOptions.setEventLoopPoolSize`

- **`vertx.max-event-loop-execution-time`** &mdash; See `VertxOptions.setMaxEventLoopExecutionTime`

- **`vertx.warning-exception-time`** &mdash; See `VertxOptions.setWarningExceptionTime`

- **`vertx.blocked-thread-check-interval`** &mdash; See `VertxOptions.setBlockedThreadCheckInterval`

- **`vertx.worker-pool-size`** &mdash; See `VertxOptions.setWorkerPoolSize`

- **`vertx.max-worker-execution-time`** &mdash; See `VertxOptions.setMaxWorkerExecutionTime`

- **`vertx.internal-blocking-pool-size`** &mdash; See `VertxOptions.setInternalBlockingPoolSize`

- **`vertx.ha-enabled`** &mdash; See `VertxOptions.setHaEnabled`

- **`vertx.ha-group`** &mdash; See `VertxOptions.setHaGroup`

- **`vertx.quorum-size`** &mdash; See `VertxOptions.setQuorumSize`

- **`vertx.clustered`** &mdash; See `VertxOptions.setClustered`

- **`vertx.cluster-host`** &mdash; See `VertxOptions.setClusterHost`

- **`vertx.cluster-port`** &mdash; See `VertxOptions.setClusterPort`

- **`vertx.cluster-ping-interval`** &mdash; See `VertxOptions.setClusterPingInterval`

- **`vertx.cluster-ping-reply-interval`** &mdash; See `VertxOptions.setClusterPingReplyInterval`

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
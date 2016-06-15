See the [api docs](http://rworsnop.github.io/vertx-beans/apidocs/)

# Vert.x Beans

Vert.x Beans makes it easy for developers to use Vert.x 3.0 (and above) with Spring.

It does this in two ways:

- Provides useful beans you can add to your application context and inject into your beans. They are `Vertx`, `EventBus`, `FileSystem`, and `SharedData`.
- Allows you to specify your own `MetricsOptions` or `ClusterManager`, simply by having them in your application context.

If you're feeling impatient, just head over to the [example](https://github.com/rworsnop/vertx-beans-example). It demonstrates how to use this library with Spring Boot.

**Important note:** If you're looking for a library that allows you to inject your application beans into a verticle, you need something
like [spring-vert-ext](https://github.com/amoAHCP/spring-vertx-ext). Vert.x Beans flips this around, taking the opposite approach. 

## Getting the library 

Either grab the latest from the [releases page](https://github.com/rworsnop/vertx-beans/releases) or add a dependency.

Maven:
```
<dependency>
    <groupId>com.github.rworsnop</groupId>
    <artifactId>vertx-beans</artifactId>
    <version>1.3.0</version>
</dependency>
```

Gradle: 
```
compile 'com.github.rworsnop:vertx-beans:1.3.0'
```


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

Vert.x Beans doesn't *require* configuration, but there are a number of optional properties that can be supplied to it via Spring's
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
<bean id="clusterManager" class="io.vertx.spi.cluster.hazelcast.HazelcastClusterManager">
  <property name="config" ref="cacheConfig"/>
</bean>
```
```
<bean id="metricsOptions" class="io.vertx.ext.dropwizard.DropwizardMetricsOptions">
  <property name="enabled" value="${metricsEnabled:false}"/>
</bean>   
```


## Creating multiple "instances"

Vert.x 3.0 allows developers to use it as either a framework (with verticles) or a library (in  more of a Node.js style).
Vert.x Beans is best-suited to the latter approach. Do note that this means that there is no longer a concept of "instances", so you
are responsible for making sure you are using a number of event loops commensurate with the number of cores on the machine.
In practice this means calling `Vertx.createHttpServer`, `EventBus.consumer`, etc. multiple times.

Vertx Beans provides `ContextRunner` for doing this. It executes a callback a specified number of times, using a new event
loop each time (this is useful if, for example, you want multiple HTTP servers to share event loops).

To use `ContextRunner`, simply inject it:
```
@Resource
private ContextRunner contextRunner;
```


In these examples, we create two HTTP servers.

Here, we'll do it synchronously, waiting for one minute to perform the work:

```
try {
    List<HttpServer> servers =  contextRunner.executeBlocking(2,
        (Handler<AsyncResult<HttpServer>> handler) ->
            vertx.createHttpServer().requestHandler(someHandler).listen(8080, handler),
            1, TimeUnit.MINUTES);
} catch (InterruptedException | ExecutionException | TimeoutException e) {
    e.printStackTrace();
}            
```

If everything goes well, the above will return two `HTTPServer` instances. In the event of an error, or if there are
no results within one minute, the method will throw an exception.

The asynchronous version would look like this:

```
contextRunner.execute(2,
        (Handler<AsyncResult<HttpServer>> handler) ->
            vertx.createHttpServer().requestHandler(someHandler).listen(8080, handler),
            result-> {
                if (result.succeeded()){
                   List<HttpServer> servers = result.result();
                } else{
                    result.cause().printStackTrace();
                }
            });
```

Notice that, instead of returning a `List`, this version allows us to pass a handler, which will receive the collated results when it 
completes.

This isn't pretty code. But if you're using RxJava, you're in luck. `org.vertxbeans.rxjava.ContextRunner` allows you to write much
neater code.

Synchronous RxJava version:

```
try {
    List<HttpServer> servers =  contextRunner.executeBlocking(2, 
        () -> vertx.createHttpServer().requestHandler(someHandler).listenObservable(8080), 1, MINUTES);
} catch (InterruptedException | ExecutionException | TimeoutException e) {
    e.printStackTrace();
}         
```

Instead of consuming a handler, the user's code must now supply an `Observable`.

Finally, here's the asynchronous RxJava version:

```
contextRunner.execute(2, ()->vertx.createHttpServer().requestHandler(someHandler).listenObservable(8080))
        .timeout(1, MINUTES)
        .subscribe(httpServers -> {}
        , Throwable::printStackTrace);
```

## Sharing Vert.x client objects across contexts

The "Spring way" of using things like `HttpClient` is to have a `FactoryBean`, or a `@Bean` method, that creates
a single instance injected into beans across your application.

The problem with this is that Vert.x does not expect these things to be used across multiple threads. You are likely
to run into deadlock situations or have other problems if you do this.

Fortunately, Spring has a solution to this problem: custom scopes. Specifically, `SimpleThreadScope`. Using this scope
will ensure that each thread gets its own instance of the bean.

For example, you would create an `HttpClient` like this:
```
@Bean
@Scope(scopeName = "thread", proxyMode = ScopedProxyMode.INTERFACES)
public HttpClient httpClient(Vertx vertx){
  return vertx.createHttpClient();
}
```

The above works because `io.vertx.core.http.HttpClient` is an interface. But the RxJava version is a class, so you need
a different proxy mode for that:
```
@Bean
@Scope(scopeName = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
// This is the RxJava version
public HttpClient httpClient(Vertx vertx){
  return vertx.createHttpClient();
}
```

Note that the "thread" scope isn't registered by default. Please refer to the Spring documentation to see how to
register custom scopes.

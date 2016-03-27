package io.vertxbeans;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * When using Vertx Beans instead of verticles, there is no concept of instances.
 * These utilities offer an easy way to replicate instances by executing user-supplied callbacks
 * on new event loops.
 * The results of these asynchronous calls are collated and made available to the client.
 *
 * Created by Rob Worsnop on 3/15/16.
 */
public interface ContextRunner {
    /**
     * Execute user-supplied code and provide the collated results asynchronously.
     *
     * @param instances the number of times to execute the code (and the number of event loops to use)
     * @param consumer consumes a {@code Handler} on which the result of a call is sent
     * @param resultHandler a {@code Handler} on which collated results can be received
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     */
    <T> void execute(int instances, Consumer<Handler<AsyncResult<T>>> consumer, Handler<AsyncResult<List<T>>> resultHandler);
    /**
     * Execute user-supplied code and provide the collated results synchronously.
     *
     * @param instances the number of times to execute the code (and the number of event loops to use)
     * @param consumer consumes a {@code Handler} on which the result of a call is sent
     * @param timeout how long to wait for a result
     * @param unit unit for the timeout
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     * @return a collated {@code List} of items
     * @throws InterruptedException
     * @throws ExecutionException if your code raises an error
     * @throws TimeoutException if all of your instances don't provide a result within the timeout
     */
    <T> List<T> executeBlocking(int instances, Consumer<Handler<AsyncResult<T>>> consumer, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Create a proxy that guarantees that objects aren't shared across Vert.x contexts.
     * This is useful for things like {@code HttpClient} which were designed to be used on a singe context.
     * Each time a method is called on the proxy, a cache is checked to see if an object already exists
     * for the current context. If there isn't one, the {@code creator} function is used to create one. Then then
     * method is routed on to the real object.
     *
     * This method can be called from any thread. However, the methods on the proxy itself must be called from
     * within a Vert.x context - for example as part of an {@code HttpServer} request handler.
     *
     * @param creator creates the object to be tied to a context
     * @param clazz the interface to be proxied; e.g., {@code HttpClient}
     * @param <R> the type to be proxied; same as {@code clazz}
     * @return the proxy
     */
    <R> R createProxy(Function<Vertx,R> creator, Class<R> clazz);
}

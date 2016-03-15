package io.vertxbeans;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

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
}

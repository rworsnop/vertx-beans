package io.vertxbeans;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * When using Vertx Beans instead of verticles, there is no concept of instances.
 * These utilities offer an easy way to replicate instances by repeatedly calling,
 * for example, createHttpServer.
 * The results of these asynchronous calls are collated and made available to the client.
 *
 * Created by Rob Worsnop on 2/2/16.
 */
public final class InstanceRunner {
    /**
     * Execute user-supplied code and provide the collated results asynchronously.
     *
     * @param instances the number of times to execute the code
     * @param consumer consumes a {@code Handler} on which the result of a call is sent
     * @param resultHandler a {@code Handler} on which collated results can be received
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     */
    public static <T> void execute(int instances, Consumer<Handler<AsyncResult<T>>> consumer, Handler<AsyncResult<List<T>>> resultHandler) {
        ResultCollector collector = new ResultCollector(instances, resultHandler);
        for (int i = 0; i < instances; i++){
            consumer.accept(result -> {
                if (result.succeeded()){
                    collector.pushResult(result.result());
                } else {
                    resultHandler.handle(Future.failedFuture(result.cause()));
                }
            });
        }
    }
    /**
     * Execute user-supplied code and provide the collated results synchronously.
     *
     * @param instances the number of times to execute the code
     * @param consumer consumes a {@code Handler} on which the result of a call is sent
     * @param timeout how long to wait for a result
     * @param unit unit for the timeout
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     * @return a collated {@code List} of items
     * @throws InterruptedException
     * @throws ExecutionException if your code raises an error
     * @throws TimeoutException if all of your instances don't provide a result within the timeout
     */
    public static <T> List<T> executeBlocking(int instances, Consumer<Handler<AsyncResult<T>>> consumer, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        execute(instances, consumer, result -> {
            if (result.succeeded()){
                future.complete(result.result());
            } else{
                future.completeExceptionally(result.cause());
            }
        });
        return future.get(timeout, unit);
    }

    private static class ResultCollector<T>{
        private final int count;
        private final Handler<AsyncResult<Collection<T>>> resultHandler;
        private final Collection<T> results = new ArrayList<>();

        private ResultCollector(int count, Handler<AsyncResult<Collection<T>>> resultHandler) {
            this.count = count;
            this.resultHandler = resultHandler;
        }

        private void pushResult(T result){
            results.add(result);
            if (results.size() == count){
                resultHandler.handle(Future.succeededFuture(results));
            }
        }
    }
}

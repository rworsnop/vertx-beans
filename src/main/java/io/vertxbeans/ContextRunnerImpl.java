package io.vertxbeans;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Created by Rob Worsnop on 3/15/16.
 */
public class ContextRunnerImpl implements ContextRunner{
    private final static Logger log = LoggerFactory.getLogger(ContextRunnerImpl.class);

    private final Vertx vertx;

    public ContextRunnerImpl(Vertx vertx) {
        this.vertx = vertx;
    }
    @Override
    public <T> void execute(int instances, Consumer<Handler<AsyncResult<T>>> consumer, Handler<AsyncResult<List<T>>> resultHandler) {
        if (Vertx.currentContext() != null){
            throw new IllegalStateException("Already on a Vert.x thread!");
        }
        ResultCollector<T> collector = new ResultCollector<>(instances, resultHandler);
        for (int i = 0; i < instances; i++){
            wrap(consumer).accept(result -> {
                if (result.succeeded()){
                    collector.pushResult(result.result());
                } else {
                    resultHandler.handle(Future.failedFuture(result.cause()));
                }
            });
        }
    }
    @Override
    public <T> List<T> executeBlocking(int instances, Consumer<Handler<AsyncResult<T>>> consumer, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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

    private <T> Consumer<Handler<AsyncResult<T>>>  wrap(Consumer<Handler<AsyncResult<T>>> consumer){
        Context context = ((VertxInternal) vertx).createEventLoopContext();

        return resultHandler -> context.runOnContext(
                v->consumer.accept(result -> context.runOnContext(v1 ->resultHandler.handle(result))));
    }

    private static class ResultCollector<T>{
        private final int count;
        private final Handler<AsyncResult<List<T>>> resultHandler;
        private final List<T> results = new ArrayList<>();

        private ResultCollector(int count, Handler<AsyncResult<List<T>>> resultHandler) {
            this.count = count;
            this.resultHandler = resultHandler;
        }

        private synchronized void pushResult(T result){
            if (results.size() == count){
                log.warn("Your callback must supply one result, and only one result, to ContextRunner. (Trying to add {0}.)", result);
            } else{
                results.add(result);
                if (results.size() == count){
                    resultHandler.handle(Future.succeededFuture(results));
                }
            }
        }
    }
}

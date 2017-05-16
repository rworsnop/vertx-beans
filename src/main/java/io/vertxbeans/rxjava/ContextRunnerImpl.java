package io.vertxbeans.rxjava;

import io.vertx.core.Future;
import rx.Single;
import rx.SingleSubscriber;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Created by Rob Worsnop on 3/15/16.
 */
public class ContextRunnerImpl implements ContextRunner {

    private final io.vertxbeans.ContextRunner delegate;

    public ContextRunnerImpl(io.vertxbeans.ContextRunner delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Single<List<T>> execute(int instances, Supplier<Single<T>> supplier) {
        return Single.create(subscriber -> doExecute(subscriber, instances, supplier));
    }

    @Override
    public <T> List<T> executeBlocking(int instances, Supplier<Single<T>> supplier, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return execute(instances, supplier).toBlocking().toFuture().get(timeout, unit);
    }

    private <T> void doExecute(SingleSubscriber<? super List<T>> subscriber, int instances, Supplier<Single<T>> supplier){
        delegate.<T>execute(instances,
                resultHandler -> supplier.get().subscribe(
                        result->resultHandler.handle(Future.succeededFuture(result)),
                        throwable -> resultHandler.handle(Future.failedFuture(throwable))),

                result -> {
                    if (result.succeeded()){
                        subscriber.onSuccess(result.result());
                    } else{
                        subscriber.onError(result.cause());
                    }
                });
    }
}

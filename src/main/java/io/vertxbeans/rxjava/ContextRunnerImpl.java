package io.vertxbeans.rxjava;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import rx.Observable;
import rx.Subscriber;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
    public <T> Observable<List<T>> execute(int instances, Supplier<Observable<T>> supplier) {
        return Observable.create(subscriber -> doExecute(subscriber, instances, supplier));
    }

    @Override
    public <T> List<T> executeBlocking(int instances, Supplier<Observable<T>> supplier, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return execute(instances, supplier).toBlocking().toFuture().get(timeout, unit);
    }

    @Override
    public <R> R createProxy(Function<Vertx, R> creator, Class<R> clazz) {
        return delegate.createProxy(creator, clazz);
    }

    private <T> void doExecute(Subscriber<? super List<T>> subscriber, int instances, Supplier<Observable<T>> supplier){
        delegate.<T>execute(instances,
                resultHandler -> supplier.get().subscribe(
                        result->resultHandler.handle(Future.succeededFuture(result)),
                        throwable -> resultHandler.handle(Future.failedFuture(throwable))),

                result -> {
                    if (result.succeeded()){
                        subscriber.onNext(result.result());
                        subscriber.onCompleted();
                    } else{
                        subscriber.onError(result.cause());
                    }
                });
    }
}

package io.vertxbeans.rxjava;

import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * When using Vertx Beans instead of verticles, there is no concept of instances.
 * These utilities offer an easy way to replicate instances by repeatedly calling,
 * for example, createHttpServer.
 * The results of these asynchronous calls are collated and made available to the client.
 *
 * Created by Rob Worsnop on 2/2/16.
 */
public class InstanceRunner {
    /**
     * Execute user-supplied code and provide the collated results asynchronously.
     *
     * @param instances the number of times to execute the code
     * @param supplier supplies an {@code Observable} after executing the code; e.g., {@code Observable<HttpServer>}
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     * @return an {@code Observable} that either emits a collated {@code List} of items, or an error.
     */
    public static <T> Observable<List<T>> execute(int instances, Supplier<Observable<T>> supplier) {
        List<Observable<T>> observables = new ArrayList<>();
        for (int i = 0; i < instances; i++){
            observables.add(supplier.get());
        }
        return Observable.merge(observables).buffer(instances);
    }

    /**
     * Execute user-supplied code and provide the collated results synchronously.
     *
     * @param instances the number of times to execute the code
     * @param supplier supplies an {@code Observable} after executing the code; e.g., {@code Observable<HttpServer>}
     * @param timeout how long to wait for a result
     * @param unit unit for the timeout
     * @param <T> the type of object we are creating; e.g., {@code HttpServer}
     * @return a collated {@code List} of items
     * @throws InterruptedException
     * @throws ExecutionException if your code raises an error
     * @throws TimeoutException if all of your instances don't provide a result within the timeout
     */
    public static <T> List<T> executeBlocking(int instances, Supplier<Observable<T>> supplier, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return execute(instances, supplier).toBlocking().toFuture().get(timeout, unit);
    }
}

package io.vertxbeans;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Rob Worsnop on 3/15/16.
 */
public class ContextRunnerImpl implements ContextRunner{
    private final static Logger log = LoggerFactory.getLogger(ContextRunnerImpl.class);

    private static final ThreadLocal<Map<Integer, Object>> proxiedObjects = ThreadLocal.withInitial(HashMap::new);

    private final Vertx vertx;

    public ContextRunnerImpl(Vertx vertx) {
        this.vertx = vertx;
    }
    @Override
    public <T> void execute(int instances, Consumer<Handler<AsyncResult<T>>> consumer, Handler<AsyncResult<List<T>>> resultHandler) {
        if (Thread.currentThread().getClass().getName().startsWith("io.vertx")){
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

    @Override
    public <R> R createProxy(Function<Vertx, R> creator, Class<R> clazz) {
        return (R) Proxy.newProxyInstance(Vertx.class.getClassLoader(), new Class[]{clazz},
                (proxy, method, args) -> invokeProxiedMethod(creator, proxy, method, args));
    }

    private <R> Object invokeProxiedMethod(Function<Vertx, R> creator, Object proxy, Method method, Object[] args) throws Throwable {
        if (!Thread.currentThread().getClass().getName().startsWith("io.vertx")){
            if (!method.getReturnType().equals(void.class)){
                log.warn("Calling %s but not able to return anything because %s is not a Vert.x thread!",
                        method.toString(), Thread.currentThread().getName());
            }
            return null;
        }

        int proxyId = System.identityHashCode(proxy);
        Object proxied = proxiedObjects.get().get(proxyId);
        if (proxied == null){
            proxied = creator.apply(vertx);
            proxiedObjects.get().put(proxyId, proxied);
        }

        try {
            return method.invoke(proxied, args);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e); // should be impossible
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private <T> Consumer<Handler<AsyncResult<T>>>  wrap(Consumer<Handler<AsyncResult<T>>> consumer){
        Context context = vertx.getOrCreateContext();
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

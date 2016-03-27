package io.vertxbeans;

import io.vertx.FixtureThread;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by Rob Worsnop on 3/15/16.
 */
public class ContextRunnerTest {

    private ContextRunner contextRunner;

    @Before
    public void setup(){
        contextRunner = new ContextRunnerImpl(Vertx.vertx());
    }

    @Test
    public void executeBlocking() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> results = contextRunner.executeBlocking(2, handler -> {
            if (Thread.currentThread().getClass().getName().startsWith("io.vertx")){
                handler.handle(Future.succeededFuture("OK"));
            } else{
                handler.handle(Future.failedFuture("Not on event loop!"));
            }
        }, 10, MILLISECONDS);
        assertThat(String.join(".", results), equalTo("OK.OK"));
    }

    @Test(expected = ExecutionException.class)
    public void executeBlockingFailure() throws InterruptedException, ExecutionException, TimeoutException {
        contextRunner.executeBlocking(2, handler ->
                handler.handle(Future.failedFuture("Something bad happened")), 10, MILLISECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void executeBlockingTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        contextRunner.executeBlocking(1, handler -> {}, 1, MILLISECONDS);
    }

    @Test
    public void createProxy() throws InterruptedException, ExecutionException, TimeoutException {
        ThreadName threadName = contextRunner.createProxy(vertx->new ThreadNameImpl(), ThreadName.class);

        CompletableFuture<Boolean> future1 = new CompletableFuture<>();
        Thread thread1 = worker(threadName, future1, "test_thread_1");
        CompletableFuture<Boolean> future2 = new CompletableFuture<>();
        Thread thread2 = worker(threadName, future2, "test_thread_2");

        thread1.start();
        thread2.start();
        assertThat(future1.get(1, SECONDS), equalTo(Boolean.TRUE));
        assertThat(future2.get(1, SECONDS), equalTo(Boolean.TRUE));
    }

    @Test(expected = IllegalStateException.class)
    public void createProxyCallOutsideContext(){
        contextRunner.createProxy(vertx->new ThreadNameImpl(), ThreadName.class).get();
    }

    @Test
    public void createProxyCallOutsideContextVoidMethod(){
        contextRunner.createProxy(vertx->new ThreadNameImpl(), ThreadName.class).throwException(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createProxyThrowException() throws Throwable {
        ThreadName threadName = contextRunner.createProxy(vertx->new ThreadNameImpl(), ThreadName.class);
        CompletableFuture<Void> future = new CompletableFuture<>();
        new FixtureThread(()->{
            try{
                threadName.throwException(new IllegalArgumentException());
            } catch (IllegalArgumentException e){
                future.completeExceptionally(e);
            }
        }, "test_thread").start();
        try{
            future.get(1, MILLISECONDS);
        } catch (ExecutionException e){
            throw e.getCause();
        }
    }

    // simulates context
    private static Thread worker(ThreadName threadName, CompletableFuture<Boolean> future, String tname){
        return new FixtureThread(()->{
            for (int i = 0; i < 1000; i++){
                ThreadName delegate = threadName.getThis();
                if (delegate == threadName){
                    future.completeExceptionally(new AssertionError("delegate must not be same as proxy"));
                }
                if (!(threadName.get().equals(tname) && threadName.getThis() == delegate)){
                    future.completeExceptionally(new AssertionError());
                }
            }
            future.complete(true);
        }, tname);
    }

}

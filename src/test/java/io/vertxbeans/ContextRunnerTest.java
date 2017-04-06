package io.vertxbeans;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
    public void success() throws InterruptedException, ExecutionException, TimeoutException {
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
    public void failure() throws InterruptedException, ExecutionException, TimeoutException {
        contextRunner.executeBlocking(2, handler ->
                handler.handle(Future.failedFuture("Something bad happened")), 100, MILLISECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void timeout() throws InterruptedException, ExecutionException, TimeoutException {
        contextRunner.executeBlocking(1, handler -> {}, 1, MILLISECONDS);
    }
}

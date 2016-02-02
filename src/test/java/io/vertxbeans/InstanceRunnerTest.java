package io.vertxbeans;

import io.vertx.core.Future;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by Rob Worsnop on 2/2/16.
 */
public class InstanceRunnerTest {

    @Test
    public void success() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> results = InstanceRunner.executeBlocking(2, handler ->
            handler.handle(Future.succeededFuture("OK")), 1, MINUTES);
        assertThat(String.join(".", results), equalTo("OK.OK"));
    }

    @Test(expected = ExecutionException.class)
    public void failure() throws InterruptedException, ExecutionException, TimeoutException {
        InstanceRunner.executeBlocking(1, handler ->
            handler.handle(Future.failedFuture(new RuntimeException())), 1, MINUTES);
    }

    @Test(expected = TimeoutException.class)
    public void timeout() throws InterruptedException, ExecutionException, TimeoutException {
        InstanceRunner.executeBlocking(1, handler -> {}, 1, MILLISECONDS);
    }

}

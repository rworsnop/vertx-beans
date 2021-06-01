package io.vertxbeans;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by Rob Worsnop on 3/15/16.
 */
public class ContextRunnerTest {

    private ContextRunner contextRunner;

    @BeforeEach
    public void setup() {
        contextRunner = new ContextRunnerImpl(Vertx.vertx());
    }

    @Test
    public void success() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> results = contextRunner.executeBlocking(2, handler -> {
            if (Thread.currentThread().getClass().getName().startsWith("io.vertx")) {
                handler.handle(Future.succeededFuture("OK"));
            } else {
                handler.handle(Future.failedFuture("Not on event loop!"));
            }
        }, 10, MILLISECONDS);
        assertThat(String.join(".", results)).isEqualTo("OK.OK");
    }

    @Test
    public void failure() {
        assertThatThrownBy(() -> contextRunner.executeBlocking(2, handler ->
                handler.handle(Future.failedFuture("Something bad happened")), 100, MILLISECONDS))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    public void timeout() {
        assertThatThrownBy(() -> contextRunner.executeBlocking(1, handler -> {}, 1, MILLISECONDS))
                .isInstanceOf(TimeoutException.class);


    }
}

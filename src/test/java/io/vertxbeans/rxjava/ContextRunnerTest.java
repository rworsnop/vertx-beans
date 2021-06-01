package io.vertxbeans.rxjava;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rx.Observable;

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
    public void setup(){
        contextRunner = new ContextRunnerImpl(new io.vertxbeans.ContextRunnerImpl(Vertx.vertx()));
    }

    @Test
    public void success() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> results = contextRunner.executeBlocking(2, () -> {
            if (Thread.currentThread().getClass().getName().startsWith("io.vertx")){
                return Observable.just("OK");
            } else{
                return Observable.error(new RuntimeException("Not on event loop!"));
            }
        }, 10, MILLISECONDS);
        assertThat(String.join(".", results)).isEqualTo("OK.OK");
    }

    @Test
    public void failure() {
        assertThatThrownBy(() ->  contextRunner.executeBlocking(2, () ->
                Observable.error(new RuntimeException()), 10, MILLISECONDS)).isInstanceOf(ExecutionException.class);

    }

    @Test
    public void timeout() {
        assertThatThrownBy(() -> contextRunner.executeBlocking(1,
                () -> Observable.create(subscriber -> {}), 10, MILLISECONDS)).isInstanceOf(TimeoutException.class);
    }

}

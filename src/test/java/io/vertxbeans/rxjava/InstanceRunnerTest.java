package io.vertxbeans.rxjava;

import org.junit.Test;
import rx.Observable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by Rob Worsnop on 2/2/16.
 */

public class InstanceRunnerTest {

    @Test
    public void success() throws InterruptedException, ExecutionException, TimeoutException {
        List<String> results = InstanceRunner.executeBlocking(2, () -> Observable.just("OK"), 1, MILLISECONDS);
        assertThat(String.join(".", results), equalTo("OK.OK"));
    }

    @Test(expected = ExecutionException.class)
    public void failure() throws InterruptedException, ExecutionException, TimeoutException {
        InstanceRunner.executeBlocking(2, () -> Observable.error(new RuntimeException()), 1, MILLISECONDS);
    }

    @Test(expected = TimeoutException.class)
    public void timeout() throws InterruptedException, ExecutionException, TimeoutException {
        InstanceRunner.executeBlocking(1, () -> Observable.create(subscriber -> {}), 1, MILLISECONDS);
    }

}
package io.vertx;

/**
 * Created by Rob Worsnop on 3/27/16.
 */
public class FixtureThread extends Thread {
    public FixtureThread(Runnable target, String name) {
        super(target, name);
    }
}

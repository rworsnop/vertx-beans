package io.vertxbeans;

/**
 * Created by Rob Worsnop on 3/27/16.
 */
public class ThreadNameImpl implements ThreadName{
    private final String threadName;

    public ThreadNameImpl() {
        threadName = Thread.currentThread().getName();
    }

    @Override
    public String get() {
        return threadName;
    }

    @Override
    public ThreadName getThis() {
        return this;
    }

    @Override
    public void throwException(RuntimeException e) {
        throw e;
    }
}

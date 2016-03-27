package io.vertxbeans;

/**
 * Created by Rob Worsnop on 3/27/16.
 */
public interface ThreadName {
    String get();
    ThreadName getThis();
    void throwException(RuntimeException e);
}

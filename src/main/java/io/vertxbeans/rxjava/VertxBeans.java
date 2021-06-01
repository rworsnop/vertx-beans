package io.vertxbeans.rxjava;


import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.file.FileSystem;
import io.vertx.rxjava.core.shareddata.SharedData;
import io.vertxbeans.VertxBeansBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Rob Worsnop on 9/5/15.
 */
@Configuration
public class VertxBeans extends VertxBeansBase {

    @Bean
    public Vertx vertx(VertxOptions options) throws Throwable {
        if (options.getClusterManager() != null) {
            return clusteredVertx(options);
        } else {
            return Vertx.vertx(options);
        }
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        return vertx.eventBus();
    }

    @Bean
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }

    @Bean
    public SharedData sharedData(Vertx vertx) {
        return vertx.sharedData();
    }

    @Bean
    public ContextRunner contextRunner(Vertx vertx) {
        return new ContextRunnerImpl(new io.vertxbeans.ContextRunnerImpl(vertx.getDelegate()));
    }

    private Vertx clusteredVertx(VertxOptions options) throws Throwable {
        return clusteredVertx(handler -> Vertx.clusteredVertx(options, handler));
    }

}
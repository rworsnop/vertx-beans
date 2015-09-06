package io.vertx.rxjava.spring;


import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.file.FileSystem;
import io.vertx.rxjava.core.shareddata.SharedData;
import io.vertx.spring.VertxBeansBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Rob Worsnop on 9/5/15.
 */
@Configuration
public class VertxBeans extends VertxBeansBase{

    @Bean
    public Vertx vertx(VertxOptions options) throws Throwable {
        if (options.isClustered()) {
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
    public FileSystem fileSystem(Vertx vertx){
        return vertx.fileSystem();
    }

    @Bean
    public SharedData sharedData(Vertx vertx){
        return vertx.sharedData();
    }

    private Vertx clusteredVertx(VertxOptions options) throws Throwable {
        return clusteredVertx(handler -> Vertx.clusteredVertx(options, handler));
    }

}
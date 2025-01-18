package io.vertxbeans;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Rob Worsnop on 9/5/15.
 */
@Configuration
public class VertxBeans extends VertxBeansBase {

    @Bean
    public Vertx vertx(VertxOptions options) throws Exception {
       try {
           if (options.getClusterManager() != null) {
               return clusteredVertx(options);
           } else {
               return Vertx.vertx(options);
           }
       } catch (Throwable throwable) {
           throw new Exception(throwable);
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
        return new ContextRunnerImpl(vertx);
    }

    private Vertx clusteredVertx(VertxOptions options) throws Throwable {
        return clusteredVertx(handler -> Vertx.clusteredVertx(options, handler));
    }


}

package io.vertxbeans;

import io.vertx.core.AsyncResultHandler;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.Collections.list;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by Rob Worsnop on 9/5/15.
 */
public class VertxBeansBase {

    private static final Logger log = LoggerFactory.getLogger(VertxBeansBase.class);

    @Autowired(required = false)
    private ClusterManager clusterManager;

    @Autowired(required = false)
    private MetricsOptions metricsOptions;

    @Autowired
    private Environment env;

    @Bean
    protected VertxOptions vertxOptions() {
        VertxOptions options = new VertxOptions();

        setParameter(env.getProperty("vertx.warning-exception-time", Long.class), options::setWarningExceptionTime);
        setParameter(env.getProperty("vertx.event-loop-pool-size", Integer.class), options::setEventLoopPoolSize);
        setParameter(env.getProperty("vertx.max-event-loop-execution-time", Long.class), options::setMaxEventLoopExecuteTime);
        setParameter(env.getProperty("vertx.worker-pool-size", Integer.class), options::setWorkerPoolSize);
        setParameter(env.getProperty("vertx.max-worker-execution-time", Long.class), options::setMaxWorkerExecuteTime);
        setParameter(env.getProperty("vertx.blocked-thread-check-interval", Long.class), options::setBlockedThreadCheckInterval);
        setParameter(env.getProperty("vertx.internal-blocking-pool-size", Integer.class), options::setInternalBlockingPoolSize);
        options.setHAEnabled(env.getProperty("vertx.ha-enabled", Boolean.class, false));
        setParameter(env.getProperty("vertx.ha-group", ""), options::setHAGroup);
        setParameter(env.getProperty("vertx.quorum-size", Integer.class), options::setQuorumSize);
        options.setClustered(env.getProperty("vertx.clustered", Boolean.class, false));
        options.setClusterHost(env.getProperty("vertx.cluster-host", getDefaultAddress()));
        setParameter(env.getProperty("vertx.cluster-port", Integer.class), options::setClusterPort);
        setParameter(env.getProperty("vertx.cluster-ping-interval", Long.class), options::setClusterPingInterval);
        setParameter(env.getProperty("vertx.cluster-ping-reply-interval", Long.class), options::setClusterPingReplyInterval);
        setParameter(env.getProperty("vertx.cluster.public.host", String.class), options::setClusterPublicHost);
        setParameter(env.getProperty("vertx.cluster.public.port", Integer.class), options::setClusterPublicPort);
        setParameter(clusterManager, options::setClusterManager);
        setParameter(metricsOptions, options::setMetricsOptions);

        return options;
    }

    protected <T> T clusteredVertx(Consumer<AsyncResultHandler<T>> consumer) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<T> future = new CompletableFuture<>();
        clusteredVertx(consumer, ar -> {
            if (ar.succeeded()) {
                future.complete(ar.result());
            } else {
                future.completeExceptionally(ar.cause());
            }
        });
        return future.get(2, MINUTES);
    }

    private <T> void clusteredVertx(Consumer<AsyncResultHandler<T>> consumer, AsyncResultHandler<T> handler) {
        consumer.accept(handler);
    }

    private <T> void setParameter(T param, Consumer<T> setter) {
        if (param != null) {
            setter.accept(param);
        }
    }

    private String getDefaultAddress() {
       try {
            return list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(ni -> list(ni.getInetAddresses()).stream())
                    .filter(address -> !address.isAnyLocalAddress())
                    .filter(address -> !address.isMulticastAddress())
                    .filter(address -> !address.isLoopbackAddress())
                    .filter(address ->!(address instanceof Inet6Address))
                    .map(InetAddress::getHostAddress)
                    .findFirst().orElse("0.0.0.0");
        } catch (SocketException e) {
            log.warn("Unable to determine network interfaces. Using \"localhost\" as host address.", e);
            return "0.0.0.0";
       }
    }
}

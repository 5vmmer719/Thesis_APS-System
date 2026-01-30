package com.aps.config;

import com.aps.grpc.proto.ApsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 客户端配置
 */
@Slf4j
@Configuration
public class GrpcConfig {

    @Value("${aps.schedule-engine.grpc-host:localhost}")
    private String grpcHost;

    @Value("${aps.schedule-engine.grpc-port:50051}")
    private int grpcPort;

    @Value("${aps.schedule-engine.timeout-seconds:300}")
    private long timeoutSeconds;

    private ManagedChannel channel;

    @Bean
    public ManagedChannel managedChannel() {
        log.info("初始化 gRPC Channel: {}:{}", grpcHost, grpcPort);

        channel = ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(100 * 1024 * 1024) // 100MB
                .build();

        return channel;
    }

    @Bean
    public ApsServiceGrpc.ApsServiceBlockingStub apsServiceBlockingStub(ManagedChannel channel) {
        return ApsServiceGrpc.newBlockingStub(channel)
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS);
    }

    @Bean
    public ApsServiceGrpc.ApsServiceStub apsServiceStub(ManagedChannel channel) {
        return ApsServiceGrpc.newStub(channel)
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("关闭 gRPC Channel");
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("关闭 gRPC Channel 被中断", e);
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

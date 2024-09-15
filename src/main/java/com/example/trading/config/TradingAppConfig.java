package com.example.trading.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class TradingAppConfig {


    @Bean(name = "fillServerExecutor")
    public Executor fillServerExecutor(@Value("${fill.executor.corePoolSize:3}") int corePoolSize,
                                      @Value("${fill.executor.maximumPoolSize:5}") int maximumPoolSize,
                                      @Value("${fill.executor.keepAliveTime:60}") int keepAliveTime,
                                      @Value("${fill.executor.queueCapacity:15}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maximumPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setThreadNamePrefix("FillServer-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "aumServerExecutor")
    public Executor aumServerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("AUMServer-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "allocationServerExecutor")
    public Executor allocationServerExecutor(@Value("${allocation.executor.corePoolSize:3}") int corePoolSize,
                                             @Value("${allocation.executor.maximumPoolSize:10}") int maximumPoolSize,
                                             @Value("${allocation.executor.keepAliveTime:60}") int keepAliveTime,
                                             @Value("${allocation.executor.queueCapacity:50}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maximumPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setThreadNamePrefix("AllocationServer-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "positionServerExecutor")
    public Executor positionServerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("PositionServer-");
        executor.initialize();
        return executor;
    }

}

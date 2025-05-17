package com.example.checkin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration

public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);    // Số thread tối thiểu
        executor.setMaxPoolSize(10);    // Số thread tối đa
        executor.setQueueCapacity(100); // Độ dài hàng đợi
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}
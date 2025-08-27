package me.critiq.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class AsyncConfig {
    // 不写这个bean也有默认线程池实现
    @Bean
    public Executor mailThreadPool() {
        return Executors.newFixedThreadPool(10);
    }
}

//package com.berliz.JWT;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean
//    public AsyncTaskExecutor asyncTaskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5); // Set the number of core threads
//        executor.setMaxPoolSize(10); // Set the maximum number of threads
//        executor.setQueueCapacity(25); // Set the capacity of the queue
//        executor.setThreadNamePrefix("Async-"); // Set thread names
//        executor.initialize();
//        return executor;
//    }
//}

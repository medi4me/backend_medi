package com.mediforme.mediforme.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 검색 어댑터 병렬 호출용 Executor
 */
@Configuration
public class SearchExecutorConfig {

    @Bean(name = "searchExecutor", destroyMethod = "shutdown")
    public ExecutorService searchExecutor() {
        return Executors.newFixedThreadPool(6, r -> {
            Thread t = new Thread(r, "search-adapter");
            t.setDaemon(true);
            return t;
        });
    }
}

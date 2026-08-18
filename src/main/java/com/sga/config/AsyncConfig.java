package com.sga.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync // Habilita métodos assíncronos

public class AsyncConfig {

	@Bean(name = "faturamentoExecutor")
	public Executor faturamentoExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(100);
		executor.setQueueCapacity(2000);
		executor.setThreadNamePrefix("Faturamento-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "pdfExecutor")
	public Executor pdfExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("PDF-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "rmExecutor")
	public Executor rmExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(20);
		executor.setThreadNamePrefix("RM-");
		executor.initialize();
		return executor;
	}
	
	@Bean(name = "simulacaoExecutor")
    public Executor simulacaoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("Simulacao-");
        executor.initialize();
        return executor;
    }

}
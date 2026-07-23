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
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(100);
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

}
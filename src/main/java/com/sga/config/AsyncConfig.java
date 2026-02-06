package com.sga.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAsync // Habilita métodos assíncronos
@EnableTransactionManagement
@EnableAspectJAutoProxy // Habilita AOP
public class AsyncConfig {
    
    // Configuração padrão é suficiente
	
}
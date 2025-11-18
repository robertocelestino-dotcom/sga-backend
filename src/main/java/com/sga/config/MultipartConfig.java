package com.sga.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Tamanho máximo do arquivo
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        
        // Tamanho máximo total da requisição
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        // Tamanho mínimo para escrever em disco
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        // Localização para arquivos temporários
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }
}
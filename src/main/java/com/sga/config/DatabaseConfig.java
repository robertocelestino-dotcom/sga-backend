// src/main/java/com/sga/config/DatabaseConfig.java
package com.sga.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.sga.repository",
        entityManagerFactoryRef = "sgaEntityManager",
        transactionManagerRef = "sgaTransactionManager"
)
public class DatabaseConfig {

    // ========== DATASOURCE PRINCIPAL (PostgreSQL) - CONFIGURAÇÃO MANUAL ==========
    @Primary
    @Bean(name = "sgaDataSource")
    public DataSource sgaDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://10.109.1.4:5432/cdlfor");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

    @Primary
    @Bean(name = "sgaEntityManager")
    public LocalContainerEntityManagerFactoryBean sgaEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sgaDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "update");
        
        return builder
                .dataSource(dataSource)
                .packages("com.sga.model")
                .persistenceUnit("sga")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "sgaTransactionManager")
    public PlatformTransactionManager sgaTransactionManager(
            @Qualifier("sgaEntityManager") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

    // ========== DATASOURCE MS-SQL ==========
    @Bean(name = "notificacaoDataSource")
    public DataSource notificacaoDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://10.109.1.44:1433;databaseName=Notificacao;encrypt=false;trustServerCertificate=true");
        dataSource.setUsername("sa");
        dataSource.setPassword("cdl!#%adm2022");
        return dataSource;
    }

    @Bean(name = "notificacaoEntityManager")
    public LocalContainerEntityManagerFactoryBean notificacaoEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("notificacaoDataSource") DataSource dataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.jdbc.batch_size", "50");
        
        return builder
                .dataSource(dataSource)
                .packages("com.sga.model.notificacao")
                .persistenceUnit("notificacao")
                .properties(properties)
                .build();
    }

    @Bean(name = "notificacaoTransactionManager")
    public PlatformTransactionManager notificacaoTransactionManager(
            @Qualifier("notificacaoEntityManager") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
    
}
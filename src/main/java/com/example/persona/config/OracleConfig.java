package com.example.persona.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Profile("!local")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        // Target ONLY the Oracle folder
        basePackages = "com.example.persona.migration.repository.oracle",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager")
public class OracleConfig {

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-oracle")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oracleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("oracleDataSource") DataSource dataSource,
            @Value("${spring.jpa.oracle.properties.hibernate.default_schema:MWING}") String oracleDefaultSchema) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.persona.migration.model.oracle");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.default_schema", oracleDefaultSchema);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "oracleTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

package com.example.persona.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

/**
 * Local-profile replacement for {@link OracleConfig}.
 *
 * <p>Wires an H2 in-memory database (Oracle compatibility mode) as the source
 * datasource so that Oracle-reading migration handlers can run locally without
 * a real Oracle instance. H2 tables are created automatically from entity
 * definitions ({@code hbm2ddl.auto = create-drop}) and populated by
 * {@link com.example.persona.migration.seeder.LocalOracleDataSeeder}.
 *
 * <p>Bean names ({@code oracleDataSource}, {@code oracleEntityManagerFactory},
 * {@code oracleTransactionManager}) are identical to {@link OracleConfig} so
 * all Oracle repositories and migration handlers work without code changes.
 */
@Slf4j
@Profile("local")
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.persona.migration.repository.oracle",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager")
public class LocalOracleConfig {

    @Bean(name = "oracleDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-oracle")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oracleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("oracleDataSource") DataSource dataSource) {

        log.info("[H2-Oracle EMF] Initialising H2 in-memory datasource for Oracle source tables");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.persona.migration.model.oracle");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        // Auto-create tables from entity classes; dropped when context closes
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        // Keep column names exactly as declared in @Column(name = "...") — Oracle entities use UPPER_CASE
        properties.put(
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "oracleTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

package com.example.persona.config;

import com.example.persona.config.properties.PersonaApplicationAsyncProperties;
import com.example.persona.config.properties.PersonaMigrationAsyncProperties;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties({PersonaMigrationAsyncProperties.class, PersonaApplicationAsyncProperties.class})
public class AsyncConfig {

    @Bean(name = "migrationTaskExecutor")
    public Executor migrationTaskExecutor(
            ContextPropagatingTaskDecorator contextPropagatingTaskDecorator,
            PersonaMigrationAsyncProperties personaMigrationAsyncProperties) {
        return createExecutor(
                "MigrationAsync-",
                personaMigrationAsyncProperties.getCorePoolSize(),
                personaMigrationAsyncProperties.getMaxPoolSize(),
                personaMigrationAsyncProperties.getQueueCapacity(),
                contextPropagatingTaskDecorator);
    }

    /**
     * Default {@code @Async} executor (unnamed {@code @Async} methods). Not used for migration.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor(
            ContextPropagatingTaskDecorator contextPropagatingTaskDecorator,
            PersonaApplicationAsyncProperties personaApplicationAsyncProperties) {
        return createExecutor(
                "AppAsync-",
                personaApplicationAsyncProperties.getCorePoolSize(),
                personaApplicationAsyncProperties.getMaxPoolSize(),
                personaApplicationAsyncProperties.getQueueCapacity(),
                contextPropagatingTaskDecorator);
    }

    @Bean
    public AsyncConfigurer asyncConfigurer(@Qualifier("taskExecutor") Executor taskExecutor) {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return taskExecutor;
            }
        };
    }

    private static Executor createExecutor(
            String threadNamePrefix,
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            ContextPropagatingTaskDecorator decorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setTaskDecorator(decorator);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

package com.assetcompass.portfolio;

import com.assetcompass.portfolio.config.AsyncSyncConfiguration;
import com.assetcompass.portfolio.config.DatabaseTestcontainer;
import com.assetcompass.portfolio.config.JacksonConfiguration;
import com.assetcompass.portfolio.config.RedisTestContainer;
import com.assetcompass.portfolio.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        PortfolioServiceApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        TestSecurityConfiguration.class,
        com.assetcompass.portfolio.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers({ DatabaseTestcontainer.class, RedisTestContainer.class })
public @interface IntegrationTest {}

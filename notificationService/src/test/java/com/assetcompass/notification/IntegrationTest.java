package com.assetcompass.notification;

import com.assetcompass.notification.config.AsyncSyncConfiguration;
import com.assetcompass.notification.config.DatabaseTestcontainer;
import com.assetcompass.notification.config.JacksonConfiguration;
import com.assetcompass.notification.config.RedisTestContainer;
import com.assetcompass.notification.config.TestSecurityConfiguration;
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
        NotificationServiceApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        TestSecurityConfiguration.class,
        com.assetcompass.notification.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers({ DatabaseTestcontainer.class, RedisTestContainer.class })
public @interface IntegrationTest {}

package com.will.compairator;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import java.net.URI;

/** Runs before Hibernate: integration tests delete data and are limited to disposable test databases. */
public class TestDatabaseGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String url = context.getEnvironment().getRequiredProperty("spring.datasource.url");
        if (url.startsWith("jdbc:h2:mem:compairator-test")) return;
        if (url.startsWith("jdbc:mysql:")) {
            URI database = URI.create(url.substring("jdbc:".length()));
            if ("/compairator_kpi_test".equals(database.getPath())
                    && ("localhost".equals(database.getHost()) || "127.0.0.1".equals(database.getHost()))) return;
        }
        throw new IllegalStateException("Tests refusés : utiliser H2 en mémoire ou la base locale compairator_kpi_test.");
    }
}

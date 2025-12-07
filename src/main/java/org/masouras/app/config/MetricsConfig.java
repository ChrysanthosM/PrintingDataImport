package org.masouras.app.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        String appName = environment.getProperty("spring.application.name", "unknown");
        String activeProfile = environment.getProperty("spring.profiles.active", "default");

        return registry -> registry.config().commonTags(
                "application", appName,
                "environment", activeProfile
        );
    }
}

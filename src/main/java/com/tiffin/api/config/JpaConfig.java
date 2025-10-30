package com.tiffin.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled through application.yml
    // This class enables JPA Auditing for creation and modification timestamps
    // and transaction management
}
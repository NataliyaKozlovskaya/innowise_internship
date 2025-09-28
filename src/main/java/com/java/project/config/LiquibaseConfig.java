package com.java.project.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Liquibase
 */
@Configuration
public class LiquibaseConfig {

  @Bean
  public SpringLiquibase liquibase(ApplicationContext context) {
    DataSource dataSource = context.getBean(DataSource.class);
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
    return liquibase;
  }
}
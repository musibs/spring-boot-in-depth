package com.quickpay.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Somnath Musib
 * Date: 14/09/2025
 */
@Configuration
public class HikariDataSourceConfig {

    @Bean
    public DataSource quickPayDataSource() {
        return new HikariDataSource();
    }
}

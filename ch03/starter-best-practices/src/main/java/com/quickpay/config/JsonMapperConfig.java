package com.quickpay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Somnath Musib
 * Date: 14/09/2025
 */
@Configuration
public class JsonMapperConfig {

    //@Bean
     JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .findAndAddModules() // Automatically discover and register all available modules
                .build();
        }
}

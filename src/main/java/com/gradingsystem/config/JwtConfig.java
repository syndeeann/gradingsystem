package com.gradingsystem.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {

    private String secret;

    /** Token lifetime in milliseconds (default 24 h). */
    private long expiration = 86_400_000L;
}

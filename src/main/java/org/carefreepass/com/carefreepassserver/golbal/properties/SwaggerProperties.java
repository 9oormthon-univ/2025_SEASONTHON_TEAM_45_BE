package org.carefreepass.com.carefreepassserver.golbal.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swagger")
public record SwaggerProperties(
        String version,
        String url
) {
}

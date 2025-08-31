package org.carefreepass.com.carefreepassserver.golbal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.carefreepass.com.carefreepassserver.golbal.properties.SwaggerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String SWAGGER_TITLE = "CareFreePass API";
    private static final String SWAGGER_DESCRIPTION = "CareFreePass API Documentation";

    private static final String BEARER_SCHEME_NAME = "BearerAuth";
    private static final String BEARER_TYPE = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    private static final String LOCAL_IDENTIFIER = "localhost";
    private static final String LOCAL_SERVER = "Local Server";
    private static final String DEV_SERVER = "Dev Server";

    private final SwaggerProperties swaggerProperties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(server()))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                                BEARER_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(BEARER_TYPE)
                                        .bearerFormat(BEARER_FORMAT)
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title(SWAGGER_TITLE)
                .description(SWAGGER_DESCRIPTION)
                .version(swaggerProperties.version());
    }

    private Server server() {
        String url = swaggerProperties.url();
        String description = url.contains(LOCAL_IDENTIFIER) ? LOCAL_SERVER : DEV_SERVER;
        return new Server().url(url).description(description);
    }
}

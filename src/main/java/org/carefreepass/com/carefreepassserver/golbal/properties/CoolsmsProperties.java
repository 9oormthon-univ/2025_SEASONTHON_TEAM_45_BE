package org.carefreepass.com.carefreepassserver.golbal.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coolsms")
public record CoolsmsProperties(
        String apiKey,
        String apiSecret,
        String apiUrl,
        String fromPhoneNumber,
        int verificationCodeLength,
        int verificationCodeTtl,
        String messageTemplate
) {
}
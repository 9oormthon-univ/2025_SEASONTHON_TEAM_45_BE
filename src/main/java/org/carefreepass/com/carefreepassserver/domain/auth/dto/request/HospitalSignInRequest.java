package org.carefreepass.com.carefreepassserver.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HospitalSignInRequest(
        String adminEmail,
        @NotBlank String adminPassword
) {

}

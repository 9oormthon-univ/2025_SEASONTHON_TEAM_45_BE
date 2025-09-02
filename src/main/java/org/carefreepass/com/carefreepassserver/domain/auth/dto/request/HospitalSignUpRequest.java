package org.carefreepass.com.carefreepassserver.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HospitalSignUpRequest(
        @NotBlank String adminName,
        String adminEmail,
        @Size(min=8, max=64) String adminPassword,
        @NotBlank String hospitalName,
        String hospitalAddress
) {

}

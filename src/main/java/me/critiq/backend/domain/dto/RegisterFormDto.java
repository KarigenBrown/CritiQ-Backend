package me.critiq.backend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterFormDto {
    // todo 变更为phone
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String password;
}

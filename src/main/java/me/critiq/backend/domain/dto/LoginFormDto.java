package me.critiq.backend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginFormDto {
    // todo 变更为phone
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

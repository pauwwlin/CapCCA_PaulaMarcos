package com.capgemini.test.code.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank
    @Size(max = 6)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 15)
    private String dni;

    @NotBlank
    @Size(max = 15)
    private String phone;

    @Pattern(regexp = "admin|superadmin", message = "El rol debe ser admin o superadmin")
    private String role;

}

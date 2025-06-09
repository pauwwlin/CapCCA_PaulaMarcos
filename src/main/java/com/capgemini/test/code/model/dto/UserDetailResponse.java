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
public class UserDetailResponse {

    private Long id;

    @NotBlank
    @Size(max = 6)
    public String name;

    @NotBlank
    @Size(max = 150)
    @Email
    public String email;

    @NotBlank
    @Size(max = 15)
    public String dni;

    @NotBlank
    @Size(max = 15)
    public String phone;

    @Pattern(regexp = "admin|superadmin", message = "El rol debe ser admin o superadmin")
    public String role;

}

package com.piixl.auth_service.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank
    private String password;

    private Boolean termsAccepted;

    private Boolean enabled = false;

    private Role role;

    private LocalDate dateOfBirth;

    private LocalDate createdAt;

}

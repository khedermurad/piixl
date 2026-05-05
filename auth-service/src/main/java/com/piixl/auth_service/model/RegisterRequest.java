package com.piixl.auth_service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.AssertTrue;
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
public class RegisterRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    @Pattern(regexp = "^[A-Za-z]{5}.*", message = "Username must start with 5 letters")
    private String username;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 20, message = "Profile name must be between 1 and 20 characters")
    private String profileName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
    message = "The password must be at least 8 characters long and contain uppercase letters, lowercase letters, numbers, and special characters.")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirm;

    @NotNull(message = "Date of birth is required")
    @Past(message = "must be a past date")
    private LocalDate dateOfBirth;

    @NotNull(message = "Terms must be accepted")
    @AssertTrue(message = "Terms must be accepted")
    private Boolean termsAccepted;
}

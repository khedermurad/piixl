package com.piixl.auth_service.model;


import java.io.Serializable;
import java.time.LocalDate;

public record UserEvent(Long id, String profileName, String username, String email, LocalDate dateOfBirth) implements Serializable {
}

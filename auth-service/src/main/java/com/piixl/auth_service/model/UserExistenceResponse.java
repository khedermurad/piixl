package com.piixl.auth_service.model;

public record UserExistenceResponse(Boolean usernameExists, Boolean emailExists) {
}

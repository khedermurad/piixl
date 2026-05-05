package com.piixl.auth_service.integration;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{
    @GetMapping("/api/test/protected")
    public String protectedEndpoint(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG: Principal: " + auth.getPrincipal());
        System.out.println("DEBUG: Authorities: " + auth.getAuthorities());
        System.out.println("DEBUG: IsAuthenticated: " + auth.isAuthenticated());
        return "Access granted";
    }
}
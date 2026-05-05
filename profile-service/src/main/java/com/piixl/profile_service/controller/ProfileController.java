package com.piixl.profile_service.controller;

import com.piixl.profile_service.model.Profile;
import com.piixl.profile_service.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private ProfileService profileService;



    public ProfileController(ProfileService profileService){
        this.profileService = profileService;
    }


    @GetMapping("/{id}")
    public Profile getProfile(@PathVariable Long id){
        return this.profileService.getProfile(id);
    }

}

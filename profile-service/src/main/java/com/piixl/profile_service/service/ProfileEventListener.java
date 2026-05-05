package com.piixl.profile_service.service;

import com.piixl.profile_service.model.Profile;
import com.piixl.profile_service.model.UserEvent;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProfileEventListener {

    private ProfileService profileService;

    public ProfileEventListener(ProfileService profileService){
        this.profileService = profileService;
    }

    @RabbitListener(queuesToDeclare = @Queue("user.profile.create"))
    public void handleUserCreated(UserEvent userEvent){
        Profile profile = new Profile();
        profile.setId(userEvent.id());
        profile.setName(userEvent.profileName());
        profile.setUsername(userEvent.username());
        profile.setEmail(userEvent.email());
        profile.setDateOfBirth(userEvent.dateOfBirth());
        profile.setPrivateProfile(false);

        profileService.createProfile(profile);
    }


}

package com.piixl.profile_service.service;

import com.piixl.profile_service.model.Profile;
import com.piixl.profile_service.exception.ProfileNotFoundException;
import com.piixl.profile_service.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private ProfileRepository profileRepository;

    // TODO use this for update for better performance
    private MongoTemplate mongoTemplate;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, MongoTemplate mongoTemplate){
        this.profileRepository = profileRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Profile getProfile(Long id){
        return profileRepository.findById(id).orElseThrow(
                () -> new ProfileNotFoundException("Can not find Profile with id: " + id));
    }

    public ResponseEntity<Profile> createProfile(Profile profile){
        Profile savedProfile = profileRepository.save(profile);
        return new ResponseEntity<>(savedProfile, HttpStatus.CREATED);
    }

    public void deleteProfile(Long id){
        profileRepository.deleteById(id);
    }

}

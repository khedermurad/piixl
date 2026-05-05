package com.piixl.profile_service.repository;

import com.piixl.profile_service.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, Long> {
}

package com.piixl.profile_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "profiles")
@NoArgsConstructor
@Getter
@Setter
public class Profile {
    @Id
    private Long id;
    private String username;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private String bio;
    private boolean privateProfile;

    private List<Long> follower;
    private List<Long> follows;

}

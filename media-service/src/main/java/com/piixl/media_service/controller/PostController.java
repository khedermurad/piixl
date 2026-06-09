package com.piixl.media_service.controller;

import com.piixl.media_service.repository.PostRepository;
import com.piixl.media_service.service.PostService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/media/posts")
@Validated
public class PostController {

    private final PostService postService;

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    public PostController(PostService postService){
        this.postService = postService;
    }


    @PostMapping
    public ResponseEntity<Document> createPost(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String username,
            @RequestParam("description") @NotBlank @Size(max = 500) String description,
            @RequestParam("file") @NotNull MultipartFile file){

        return postService.savePost(userId, username, description, file);
    }




}

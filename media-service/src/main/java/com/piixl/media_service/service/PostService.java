package com.piixl.media_service.service;

import com.piixl.media_service.repository.PostRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final FileStorageService fileStorageService;


    public PostService(PostRepository postRepository,
                       FileStorageService fileStorageService){
        this.postRepository = postRepository;
        this.fileStorageService = fileStorageService;
    }

    public ResponseEntity<Document> savePost(String userId, String username,
                                             String description, MultipartFile imageFile) {
        String fileName = fileStorageService.saveImageToStorage(imageFile);
        Document savedPost = postRepository.savePost(userId, username, description, fileName);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }


}

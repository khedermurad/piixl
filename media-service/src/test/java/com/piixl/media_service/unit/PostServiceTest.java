package com.piixl.media_service.unit;

import com.piixl.media_service.repository.PostRepository;
import com.piixl.media_service.service.FileStorageService;
import com.piixl.media_service.service.PostService;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PostService postService;


    @Test
    void shouldReturnSavedPostWithCreatedStatus(){
        Post post = defaultPost();
        Document savedPost = new Document()
                .append("userId", post.userId())
                .append("username", post.username())
                .append("description", post.description())
                .append("fileName", post.fileName());
        when(fileStorageService.saveImageToStorage(any()))
                .thenReturn(post.fileName());
        when(postRepository.savePost(post.userId(), post.username(),
                post.description(), post.fileName())).thenReturn(savedPost);

        ResponseEntity<Document> response =
                postService.savePost(post.userId(), post.username(),
                        post.description(), new MockMultipartFile("test", "Hello, World!".getBytes()));

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MockMultipartFile> mockMultipartCaptor =
                ArgumentCaptor.forClass(MockMultipartFile.class);


        verify(postRepository, times(1))
                .savePost(stringCaptor.capture(), any(), any(), any());
        verify(fileStorageService, times(1))
                .saveImageToStorage(mockMultipartCaptor.capture());

        Assertions.assertEquals(post.userId(), stringCaptor.getValue());
        Assertions.assertEquals("test", mockMultipartCaptor.getValue().getName());

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(post.userId(), response.getBody().get("userId"));
        Assertions.assertEquals(post.username(), response.getBody().get("username"));
        Assertions.assertEquals(post.description(), response.getBody().get("description"));
        Assertions.assertEquals(post.fileName(), response.getBody().get("fileName"));
    }

    @Test
    void shouldNotSavePostWhenImageCannotBeSaved(){
        Post post = defaultPost();

        when(fileStorageService.saveImageToStorage(any()))
                .thenThrow(new RuntimeException());

        Assertions.assertThrows(RuntimeException.class,
                () -> postService.savePost(post.userId(), post.username(),
                        post.description(), null));

        verify(postRepository, times(0))
                .savePost(any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenDatabaseFails() {
        Post post = defaultPost();

        when(fileStorageService.saveImageToStorage(any())).thenReturn(post.fileName());

        when(postRepository.savePost(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB Error"));

        Assertions.assertThrows(RuntimeException.class,
                () -> postService.savePost(post.userId(), post.username(), post.description(), null));
    }


    private static Post defaultPost(){
        return new Post("id12345", "username", "description", "url12345");
    }

}

record Post(String userId, String username, String description, String fileName){}

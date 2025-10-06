package com.celonis.challenge.services;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class FileServiceTest {

    public static final String TASK_ID = "test-id";
    public static final String STORAGE_LOCATION = "test.zip";
    @Mock
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;

    @InjectMocks
    private FileService fileService;


    @Test
    public void storeResult() throws IOException {
        ProjectGenerationTask task = new ProjectGenerationTask();
        task.setId(TASK_ID);
        task.setStorageLocation(STORAGE_LOCATION);
        URL url = Thread.currentThread().getContextClassLoader().getResource(STORAGE_LOCATION);
        fileService.storeResult(task, url);
        assertTrue(new File(task.getStorageLocation()).exists());
    }

    @Test
    public void getTaskResult() throws IOException {
        ProjectGenerationTask task = new ProjectGenerationTask();
        task.setId(TASK_ID);
        task.setStorageLocation(STORAGE_LOCATION);

        URL url = Thread.currentThread().getContextClassLoader().getResource(STORAGE_LOCATION);
        fileService.storeResult(task, url);
        ResponseEntity<FileSystemResource> response = fileService.getTaskResult(task);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertEquals("form-data; name=\"attachment\"; filename=\"challenge.zip\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }
}
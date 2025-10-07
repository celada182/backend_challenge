package com.celonis.challenge.services;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.repository.ProjectGenerationTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectGenerationTaskServiceTest {

    @Mock
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProjectGenerationTaskService projectGenerationTaskService;

    @Test
    void listTasks() {
        List<ProjectGenerationTask> expectedTasks = List.of(
                new ProjectGenerationTask(),
                new ProjectGenerationTask()
        );

        when(projectGenerationTaskRepository.findAll()).thenReturn(expectedTasks);

        List<ProjectGenerationTask> tasks = projectGenerationTaskService.listTasks();
        assertEquals(expectedTasks.size(), tasks.size());
    }

    @Test
    void createTask() {
        Date inputDate = new Date();
        String inputId = "Input ID";
        ProjectGenerationTask task = new ProjectGenerationTask();
        task.setId(inputId);
        task.setName("Test Task");
        task.setCreationDate(inputDate);
        when(projectGenerationTaskRepository.save(task)).thenReturn(task);

        ProjectGenerationTask createdTask = projectGenerationTaskService.createTask(task);

        assertEquals(task, createdTask);
        assertNotEquals(inputId, createdTask.getId());
        assertNotEquals(inputDate, createdTask.getCreationDate());
        assertEquals(task.getName(), createdTask.getName());
    }

    @Test
    void getTask() {
        ProjectGenerationTask task = new ProjectGenerationTask();
        String inputId = "Input Id";
        task.setId(inputId);
        task.setName("Test Task");
        task.setCreationDate(new Date());
        when(projectGenerationTaskRepository.findById(inputId)).thenReturn(Optional.of(task));

        ProjectGenerationTask foundTask = projectGenerationTaskService.getTask(inputId);

        assertEquals(task, foundTask);
    }

    @Test
    void update() {
        ProjectGenerationTask existingTask = new ProjectGenerationTask();
        String inputId = "Input Id";
        existingTask.setId(inputId);
        existingTask.setName("Test Task");
        existingTask.setCreationDate(new Date());
        when(projectGenerationTaskRepository.findById(inputId)).thenReturn(Optional.of(existingTask));
        ProjectGenerationTask expectedTask = new ProjectGenerationTask();
        expectedTask.setId(inputId);
        expectedTask.setName("Updated Name");
        when(projectGenerationTaskRepository.save(existingTask)).thenReturn(expectedTask);

        ProjectGenerationTask update = new ProjectGenerationTask();
        update.setName("Updated Name");
        ProjectGenerationTask updatedTask = projectGenerationTaskService.update(inputId, update);

        assertEquals(existingTask.getId(), updatedTask.getId());
        assertEquals(expectedTask.getName(), updatedTask.getName());
    }

    @Test
    void delete() {
        String inputId = "Input Id";
        projectGenerationTaskService.delete(inputId);
        verify(projectGenerationTaskRepository, times(1)).deleteById(inputId);
    }

    @Test
    void executeTask() throws IOException {
        String inputId = "Input Id";
        ProjectGenerationTask task = new ProjectGenerationTask();
        task.setId(inputId);
        task.setName("Test Task");
        task.setCreationDate(new Date());
        when(projectGenerationTaskRepository.findById(inputId)).thenReturn(Optional.of(task));
        projectGenerationTaskService.executeTask(inputId);
        verify(fileService, times(1)).storeResult(eq(task), any());
    }
}
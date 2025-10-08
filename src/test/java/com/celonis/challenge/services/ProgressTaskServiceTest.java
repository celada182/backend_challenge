package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.exceptions.TaskCompletedException;
import com.celonis.challenge.model.ProgressTask;
import com.celonis.challenge.repository.ProgressTaskRepository;
import com.celonis.challenge.scheduler.ProgressTaskScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressTaskServiceTest {

    @Mock
    private ProgressTaskRepository progressTaskRepository;

    @Mock
    private ProgressTaskScheduler progressTaskScheduler;

    @InjectMocks
    private ProgressTaskService progressTaskService;

    private ProgressTask testTask;

    @BeforeEach
    void setUp() {
        testTask = new ProgressTask();
        testTask.setId("test-task-id");
        testTask.setStart(0);
        testTask.setEnd(10);
        testTask.setProgress(0);
        testTask.setCompleted(false);
        testTask.setCreationDate(new Date());
    }

    @Test
    void createTask_shouldSetInitialValuesAndSave() {
        // Given
        ProgressTask newTask = new ProgressTask();
        newTask.setStart(0);
        newTask.setEnd(10);

        when(progressTaskRepository.save(any(ProgressTask.class))).thenAnswer(invocation -> {
            ProgressTask savedTask = invocation.getArgument(0);
            savedTask.setId("new-task-id");
            return savedTask;
        });

        // When
        ProgressTask createdTask = progressTaskService.createTask(newTask);

        // Then
        assertNotNull(createdTask.getId());
        assertEquals(0, createdTask.getStart());
        assertEquals(10, createdTask.getEnd());
        assertEquals(0, createdTask.getProgress());
        assertFalse(createdTask.isCompleted());
        assertNotNull(createdTask.getCreationDate());
        verify(progressTaskRepository).save(any(ProgressTask.class));
    }

    @Test
    void getTask_shouldReturnTaskWhenFound() {
        // Given
        when(progressTaskRepository.findById("test-task-id")).thenReturn(Optional.of(testTask));

        // When
        ProgressTask foundTask = progressTaskService.getTask("test-task-id");

        // Then
        assertNotNull(foundTask);
        assertEquals("test-task-id", foundTask.getId());
    }

    @Test
    void getTask_shouldThrowNotFoundException() {
        // Given
        when(progressTaskRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> progressTaskService.getTask("non-existent-id"));
    }

    @Test
    void executeTask_shouldScheduleTask() {
        // When
        progressTaskService.executeTask("test-task-id");

        // Then
        verify(progressTaskScheduler).scheduleAtFixedRate(any(Runnable.class), eq(Duration.ofSeconds(1)), eq("test-task-id"));
    }

    @Test
    void cancelTask_shouldCancelScheduledTask() {
        // When
        progressTaskService.cancelTask("test-task-id");

        // Then
        verify(progressTaskScheduler).cancelScheduledTask("test-task-id");
    }

    @Test
    void updateTask_shouldIncrementProgressAndCompleteTask() {
        // Given
        testTask.setStart(0);
        testTask.setEnd(2);
        testTask.setProgress(0);
        testTask.setCompleted(false);
        
        when(progressTaskRepository.findById("test-task-id")).thenReturn(Optional.of(testTask));
        when(progressTaskRepository.save(any(ProgressTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - First update
        progressTaskService.updateTask("test-task-id");

        // Then - After first update
        assertEquals(1, testTask.getProgress());
        assertFalse(testTask.isCompleted());

        // When - Second update (should complete the task)
        progressTaskService.updateTask("test-task-id");

        // Then - After second update
        assertEquals(2, testTask.getProgress());
        assertTrue(testTask.isCompleted());
        
        // Verify the task was saved twice (once for each update)
        verify(progressTaskRepository, times(2)).save(any(ProgressTask.class));
    }
    
    @Test
    void updateTask_shouldThrowWhenTaskNotFound() {
        // Given
        when(progressTaskRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> progressTaskService.updateTask("non-existent-id"));
        verify(progressTaskRepository, never()).save(any(ProgressTask.class));
    }
    
    @Test
    void updateTask_shouldThrowWhenTaskAlreadyCompleted() {
        // Given
        testTask.setCompleted(true);
        when(progressTaskRepository.findById("test-task-id")).thenReturn(Optional.of(testTask));

        // When & Then
        assertThrows(TaskCompletedException.class, () -> progressTaskService.updateTask("test-task-id"));
        verify(progressTaskRepository, never()).save(any(ProgressTask.class));
    }

    @Test
    void cleanupTasks_shouldDeleteOldTasks() {
        // Given
        LocalDate oldDate = LocalDate.now().minusWeeks(2);
        LocalDate recentDate = LocalDate.now().minusDays(3);

        ProgressTask oldTask = new ProgressTask();
        oldTask.setId("old-task");
        oldTask.setCreationDate(Date.from(oldDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ProgressTask recentTask = new ProgressTask();
        recentTask.setId("recent-task");
        recentTask.setCreationDate(Date.from(recentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        when(progressTaskRepository.findAll()).thenReturn(List.of(oldTask, recentTask));

        // When
        progressTaskService.cleanupTasks();

        // Then
        verify(progressTaskScheduler).cancelScheduledTask("old-task");
        verify(progressTaskScheduler, never()).cancelScheduledTask("recent-task");
        verify(progressTaskRepository).delete(oldTask);
        verify(progressTaskRepository, never()).delete(recentTask);
    }
}

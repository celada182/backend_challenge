package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProgressTask;
import com.celonis.challenge.services.ProgressTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/tasks/progress")
public class ProgressTaskController {

    private final ProgressTaskService progressTaskService;

    public ProgressTaskController(ProgressTaskService progressTaskService) {
        this.progressTaskService = progressTaskService;
    }

    @PostMapping("/")
    public ProgressTask createTask(@RequestBody @Valid ProgressTask progressTask) {
        return progressTaskService.createTask(progressTask);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
        ProgressTask task = progressTaskService.getTask(taskId);
        if (task.isCompleted()) {
            throw new IllegalArgumentException("Task is already completed");
        }
        progressTaskService.executeTask(taskId);
    }

    @PostMapping("/{taskId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTask(@PathVariable String taskId) {
        progressTaskService.cancelTask(taskId);
    }

    @GetMapping("/{taskId}")
    public ProgressTask getTask(@PathVariable String taskId) {
        return progressTaskService.getTask(taskId);
    }

}

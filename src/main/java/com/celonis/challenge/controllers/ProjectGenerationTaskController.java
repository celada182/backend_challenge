package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.ProjectGenerationTaskService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/project-generation")
public class ProjectGenerationTaskController {

    private final ProjectGenerationTaskService projectGenerationTaskService;

    private final FileService fileService;

    public ProjectGenerationTaskController(ProjectGenerationTaskService projectGenerationTaskService,
                                           FileService fileService) {
        this.projectGenerationTaskService = projectGenerationTaskService;
        this.fileService = fileService;
    }

    @GetMapping("/")
    public List<ProjectGenerationTask> listTasks() {
        return projectGenerationTaskService.listTasks();
    }

    @PostMapping("/")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return projectGenerationTaskService.createTask(projectGenerationTask);
    }

    @GetMapping("/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return projectGenerationTaskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        // TODO Avoid null fields
        return projectGenerationTaskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        projectGenerationTaskService.delete(taskId);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
        projectGenerationTaskService.executeTask(taskId);
    }

    @GetMapping("/{taskId}/result")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        ProjectGenerationTask task = projectGenerationTaskService.getTask(taskId);
        return fileService.getTaskResult(task);
    }

}

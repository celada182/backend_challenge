package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.repository.ProjectGenerationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectGenerationTaskService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProjectGenerationTaskRepository projectGenerationTaskRepository;

    private final FileService fileService;

    public ProjectGenerationTaskService(ProjectGenerationTaskRepository projectGenerationTaskRepository,
                                        FileService fileService) {
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
        this.fileService = fileService;
    }

    public List<ProjectGenerationTask> listTasks() {
        return projectGenerationTaskRepository.findAll();
    }

    public ProjectGenerationTask createTask(ProjectGenerationTask projectGenerationTask) {
        projectGenerationTask.setId(null);
        projectGenerationTask.setCreationDate(new Date());
        return projectGenerationTaskRepository.save(projectGenerationTask);
    }

    public ProjectGenerationTask getTask(String taskId) {
        return get(taskId);
    }

    public ProjectGenerationTask update(String taskId, ProjectGenerationTask projectGenerationTask) {
        ProjectGenerationTask existing = get(taskId);
        existing.setCreationDate(projectGenerationTask.getCreationDate());
        existing.setName(projectGenerationTask.getName());
        return projectGenerationTaskRepository.save(existing);
    }

    public void delete(String taskId) {
        projectGenerationTaskRepository.deleteById(taskId);
    }

    public void executeTask(String taskId) {
        // TODO automatically create zip file and resource folder
        URL url = Thread.currentThread().getContextClassLoader().getResource("challenge.zip");
        ProjectGenerationTask task = getTask(taskId);
        if (url == null) {
            throw new InternalException("Zip file not found");
        }
        try {
            fileService.storeResult(task, url);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    private ProjectGenerationTask get(String taskId) {
        Optional<ProjectGenerationTask> projectGenerationTask = projectGenerationTaskRepository.findById(taskId);
        return projectGenerationTask.orElseThrow(NotFoundException::new);
    }

    @Scheduled(cron = "0 0 * * * ?") // Every day at 00:00
    void cleanupTasks() {
        logger.info("Cleaning up project generation tasks");
        LocalDate localDate = LocalDate.now().minusWeeks(1); // One week old
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        projectGenerationTaskRepository.findAll().forEach(task -> {
            if (task.getCreationDate().compareTo(date) < 0) {
                logger.info("Task {} is older than one week", task.getId());
                projectGenerationTaskRepository.delete(task);
            }
        });
    }
}

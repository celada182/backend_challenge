package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProgressTask;
import com.celonis.challenge.repository.ProgressTaskRepository;
import com.celonis.challenge.scheduler.ProgressTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ProgressTaskService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ProgressTaskRepository progressTaskRepository;

    private final ProgressTaskScheduler progressTaskScheduler;

    public ProgressTaskService(ProgressTaskRepository progressTaskRepository,
                               ProgressTaskScheduler progressTaskScheduler) {
        this.progressTaskRepository = progressTaskRepository;
        this.progressTaskScheduler = progressTaskScheduler;
    }

    public ProgressTask createTask(ProgressTask progressTask) {
        progressTask.setId(null);
        progressTask.setProgress(progressTask.getStart());
        progressTask.setCompleted(false);
        return progressTaskRepository.save(progressTask);
    }

    public ProgressTask getTask(String taskId) {
        Optional<ProgressTask> progressTask = progressTaskRepository.findById(taskId);
        return progressTask.orElseThrow(NotFoundException::new);
    }

    public void executeTask(String taskId) {
        logger.info("Executing progress task: {}", taskId);
        ProgressTask task = getTask(taskId);
        if (task.isCompleted()) {
            throw new IllegalArgumentException("Task is already completed");
        }
        Runnable runnable = taskRunnable(task);
        progressTaskScheduler.scheduleAtFixedRate(runnable, Duration.ofSeconds(1), taskId);
    }

    public void cancelTask(String taskId) {
        logger.info("Cancel progress task: {}", taskId);
        progressTaskScheduler.cancelScheduledTask(taskId);
    }

    private Runnable taskRunnable(ProgressTask task) {
        return () -> {
            if (!task.isCompleted()) {
                logger.info("Running task {}", task.getId());
                task.setProgress(task.getProgress() + 1);
                logger.info("Task {} progress: {}", task.getId(), task.getProgress());
                if (task.getProgress() >= task.getEnd()) {
                    logger.info("Task {} completed", task.getId());
                    task.setCompleted(true);
                }
            }
            progressTaskRepository.save(task);
            logger.info("Task {} saved", task.getId());
            if (task.isCompleted()) {
                progressTaskScheduler.cancelScheduledTask(task.getId());
                logger.info("Task {} cancelled", task.getId());
            }
        };
    }
}

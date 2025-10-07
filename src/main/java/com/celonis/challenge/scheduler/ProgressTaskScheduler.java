package com.celonis.challenge.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
public class ProgressTaskScheduler extends ThreadPoolTaskScheduler {

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    public void scheduleAtFixedRate(Runnable task, Duration period, String id) {
        ScheduledFuture<?> future = super.scheduleAtFixedRate(task, period);
        scheduledTasks.put(id, future);
    }

    public void cancelScheduledTask(String id) {
        ScheduledFuture<?> future = scheduledTasks.get(id);
        if (null != future) {
            future.cancel(true);
            scheduledTasks.remove(id);
        }
    }
}

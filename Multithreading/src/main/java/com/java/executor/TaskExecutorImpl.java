package com.java.executor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskExecutorImpl implements TaskExecutor {

    private final BlockingQueue<Task<?>> taskQueue;
    private final ExecutorService executorService;
    private final Map<UUID, Object> groupLocks = new ConcurrentHashMap<>();

    public TaskExecutorImpl(int concurrencyLevel) {
        this.executorService = Executors.newFixedThreadPool(concurrencyLevel);
        this.taskQueue = new LinkedBlockingQueue<>();
    }
    
    @Override
    public <T> Future<T> submitTask(Task<T> task) {
        taskQueue.offer(task);
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.submit(() -> executeTask(task, future));

        return future;
    }

    private <T> void executeTask(Task<T> task, CompletableFuture<T> future) {
        Object groupLock = groupLocks.computeIfAbsent(task.taskGroup().groupUUID(), v -> new Object());

        synchronized (groupLock) {
            try {
                T result = task.taskAction().call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                groupLocks.remove(task.taskGroup().groupUUID());
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}


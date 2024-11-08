package com.java.executor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) {

        TaskExecutorImpl taskExecutor = new TaskExecutorImpl(3);

        TaskGroup group1 = new TaskGroup(UUID.randomUUID());
        TaskGroup group2 = new TaskGroup(UUID.randomUUID());

        Task<String> task1 = new Task<>(UUID.randomUUID(), group1, TaskType.READ, () -> {
            Thread.sleep(1000);
            return "Task 1 completed";
        });

        Task<String> task2 = new Task<>(UUID.randomUUID(), group1, TaskType.WRITE, () -> {
            Thread.sleep(800);
            return "Task 2 completed";
        });

        Task<String> task3 = new Task<>(UUID.randomUUID(), group2, TaskType.READ, () -> {
            Thread.sleep(700);
            return "Task 3 completed";
        });

        Task<String> task4 = new Task<>(UUID.randomUUID(), group2, TaskType.WRITE, () -> {
            Thread.sleep(600);
            return "Task 4 completed";
        });

        Future<String> future1 = taskExecutor.submitTask(task1);
        Future<String> future2 = taskExecutor.submitTask(task2);
        Future<String> future3 = taskExecutor.submitTask(task3);
        Future<String> future4 = taskExecutor.submitTask(task4);

        try {
            System.out.println(future1.get());
            System.out.println(future2.get());
            System.out.println(future3.get());
            System.out.println(future4.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            taskExecutor.shutdown();
        }
    }
}

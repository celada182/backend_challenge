package com.celonis.challenge.exceptions;

public class TaskCompletedException extends RuntimeException {
    public TaskCompletedException(String message) {
        super(message);
    }
}

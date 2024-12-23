package com.app.task_manager;

public class Task {
    private String userId;
    private String title;
    private String description;
    private String priority;
    private String dueDate;
    private String reminderDateTime; // Field to store combined date and time for reminders
    private String taskId;

    // Default constructor required for Firestore
    public Task() {}

    // Constructor
    public Task(String title, String description, String priority, String dueDate, String reminderDateTime, String userId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.reminderDateTime = reminderDateTime;
        this.userId = userId; // Set userId when creating a task
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReminderDateTime() { return reminderDateTime; }
    public void setReminderDateTime(String reminderDateTime) { this.reminderDateTime = reminderDateTime; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

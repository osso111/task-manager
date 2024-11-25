package com.app.task_manager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TaskActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private Button addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        recyclerView = findViewById(R.id.recyclerView);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        // Load tasks from Firestore
        getTasks();

        // Set up add task button
        addTaskButton.setOnClickListener(view -> openCreateTaskDialog());
    }

    private void getTasks() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view your tasks.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("tasks")
                .whereEqualTo("userId", userId) // Query for tasks with the current user's ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Task task = documentSnapshot.toObject(Task.class);
                        task.setTaskId(documentSnapshot.getId());

                        String reminderDateTime = task.getReminderDateTime();
                        if (TextUtils.isEmpty(reminderDateTime)) {
                            Log.e("TaskActivity", "ReminderDateTime is null or empty for task: " + task.getTitle());
                            continue; // Skip tasks with invalid or empty reminderDateTime
                        }

                        taskList.add(task);
                    }
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskActivity.this, "Error fetching tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openCreateTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_task, null);
        builder.setView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        Spinner inputPriority = dialogView.findViewById(R.id.inputPriority);
        TextView dueDateText = dialogView.findViewById(R.id.dueDateText);
        TextView timeText = dialogView.findViewById(R.id.timeText);

        dueDateText.setOnClickListener(v -> showDateTimePickerDialog(dueDateText, timeText));

        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String priority = inputPriority.getSelectedItem().toString().trim();
            String dueDate = dueDateText.getText().toString().trim();
            String dueTime = timeText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priority) ||
                    TextUtils.isEmpty(dueDate) || TextUtils.isEmpty(dueTime)) {
                Toast.makeText(TaskActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            createTask(title, description, priority, dueDate, dueTime);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showDateTimePickerDialog(TextView dueDateText, TextView timeText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String dueDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    dueDateText.setText(dueDate);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (view1, hourOfDay, minute) -> {
                                String dueTime = String.format("%02d:%02d", hourOfDay, minute);
                                timeText.setText(dueTime);
                            },
                            12, 0, true
                    );
                    timePickerDialog.show();
                },
                2024, 0, 1
        );
        datePickerDialog.show();
    }

    private void createTask(String taskName, String taskDescription, String taskPriority, String dueDate, String dueTime) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to create a task.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String reminderDateTime = dueDate + " " + dueTime;

        Task newTask = new Task(taskName, taskDescription, taskPriority, dueDate, reminderDateTime, userId);

        db.collection("tasks")
                .add(newTask)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TaskActivity.this, "Task created successfully", Toast.LENGTH_SHORT).show();
                    getTasks(); // Refresh tasks
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskActivity.this, "Error creating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteTask(String taskId) {
        db.collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    getTasks();  // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskActivity.this, "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditTask(Task task) {
        openEditTaskDialog(task);
    }

    private void openEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_task, null);
        builder.setView(dialogView);

        // Initialize the dialog components
        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        Spinner inputPriority = dialogView.findViewById(R.id.inputPriority);
        TextView dueDateText = dialogView.findViewById(R.id.dueDateText);
        TextView timeText = dialogView.findViewById(R.id.timeText);

        // Pre-fill the dialog with the task's current data
        inputTitle.setText(task.getTitle());
        inputDescription.setText(task.getDescription());
        dueDateText.setText(task.getDueDate());

        // Extract the time from reminderDateTime (e.g., "2024-11-30 14:30")
        if (!TextUtils.isEmpty(task.getReminderDateTime())) {
            String[] dateTimeParts = task.getReminderDateTime().split(" ");
            if (dateTimeParts.length == 2) {
                dueDateText.setText(dateTimeParts[0]);
                timeText.setText(dateTimeParts[1]);
            }
        }

        // Set the spinner to the current priority
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.task_priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPriority.setAdapter(adapter);

        if (task.getPriority() != null) {
            int priorityPosition = adapter.getPosition(task.getPriority());
            inputPriority.setSelection(priorityPosition);
        }

        // Date and time picker dialog setup
        dueDateText.setOnClickListener(v -> showDateTimePickerDialog(dueDateText, timeText));

        // Handle the dialog buttons
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newDescription = inputDescription.getText().toString().trim();
            String newPriority = inputPriority.getSelectedItem().toString().trim();
            String newDueDate = dueDateText.getText().toString().trim();
            String newDueTime = timeText.getText().toString().trim();

            // Validate input fields
            if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newDescription) || TextUtils.isEmpty(newPriority) ||
                    TextUtils.isEmpty(newDueDate) || TextUtils.isEmpty(newDueTime)) {
                Toast.makeText(TaskActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the task
            updateTask(task.getTaskId(), newTitle, newDescription, newPriority, newDueDate, newDueTime);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void updateTask(String taskId, String newTitle, String newDescription, String newPriority, String newDueDate, String newDueTime) {
        String reminderDateTime = newDueDate + " " + newDueTime;

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("description", newDescription);
        updates.put("priority", newPriority);
        updates.put("dueDate", newDueDate);
        updates.put("reminderDateTime", reminderDateTime);

        db.collection("tasks").document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    getTasks();  // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskActivity.this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

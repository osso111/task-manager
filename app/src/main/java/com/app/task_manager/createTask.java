/*package com.app.task_manager;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

private FirebaseFirestore db = FirebaseFirestore.getInstance();

public void createTask(String title, String description, String priority, String dueDate) {
    Task task = new Task(title, description, priority, dueDate);

    db.collection("tasks")
            .add(task)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(TaskActivity.this, "Task Created", Toast.LENGTH_SHORT).show();
                // Optionally, update the UI or navigate to a different screen
            })
            .addOnFailureListener(e -> {
                Toast.makeText(TaskActivity.this, "Error creating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}
*/
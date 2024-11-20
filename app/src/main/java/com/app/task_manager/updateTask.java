/*package com.app.task_manager;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

private FirebaseFirestore db = FirebaseFirestore.getInstance();

public void updateTask(String taskId, String newTitle, String newDescription) {
    db.collection("tasks").document(taskId)
            .update("title", newTitle, "description", newDescription)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(TaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(TaskActivity.this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}
*/
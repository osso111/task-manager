/*package com.app.task_manager;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

private FirebaseFirestore db = FirebaseFirestore.getInstance();

public void deleteTask(String taskId) {
    db.collection("tasks").document(taskId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(TaskActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(TaskActivity.this, "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}
*/
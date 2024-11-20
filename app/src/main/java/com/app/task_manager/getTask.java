/*package com.app.task_manager;

import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

private FirebaseFirestore db = FirebaseFirestore.getInstance();

public void getTasks() {
    db.collection("tasks")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Task> taskList = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Task task = documentSnapshot.toObject(Task.class);
                    task.setTaskId(documentSnapshot.getId());  // Store the document ID
                    taskList.add(task);
                }
                // Update RecyclerView adapter with taskList
            })
            .addOnFailureListener(e -> {
                Toast.makeText(TaskActivity.this, "Error fetching tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}
*/
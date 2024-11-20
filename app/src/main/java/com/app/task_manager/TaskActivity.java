package com.app.task_manager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

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
        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    taskList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Task task = documentSnapshot.toObject(Task.class);
                        task.setTaskId(documentSnapshot.getId());
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

        // Create a layout for the input fields
        final EditText inputTitle = new EditText(this);
        inputTitle.setHint("Task Title");
        final EditText inputDescription = new EditText(this);
        inputDescription.setHint("Task Description");

        // Priority Spinner
        final Spinner inputPriority = new Spinner(this);
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Low", "Medium", "High"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPriority.setAdapter(priorityAdapter);

        final TextView dueDateText = new TextView(this);
        dueDateText.setText("Select Due Date");
        dueDateText.setTextSize( 20 );

        // Date Picker for Due Date
        dueDateText.setOnClickListener(v -> showDatePickerDialog(dueDateText));

        // Arrange inputs vertically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputTitle);
        layout.addView(inputDescription);
        layout.addView(inputPriority);
        layout.addView(dueDateText);

        builder.setView(layout);

        // Add buttons for dialog
        builder.setPositiveButton("Create", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String description = inputDescription.getText().toString().trim();
            String priority = inputPriority.getSelectedItem().toString().trim();
            String dueDate = dueDateText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priority) || TextUtils.isEmpty(dueDate)) {
                Toast.makeText(TaskActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            createTask(title, description, priority, dueDate);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showDatePickerDialog(TextView dueDateText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedMonth++; // Months are 0-based
                    String selectedDate = selectedYear + "-" + selectedMonth + "-" + selectedDay;
                    dueDateText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void createTask(String taskName, String taskDescription, String taskPriority, String dueDate) {
        Log.d("TaskActivity", "Creating task: " + taskName + " with priority: " + taskPriority + " and due date: " + dueDate);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to create a task.", Toast.LENGTH_SHORT).show();
            return;
        }

        Task newTask = new Task(taskName, taskDescription, taskPriority, dueDate);

        db.collection("tasks")
                .add(newTask)
                .addOnSuccessListener(documentReference -> {
                    Log.d("TaskActivity", "Task created successfully with ID: " + documentReference.getId());
                    Toast.makeText(TaskActivity.this, "Task created successfully", Toast.LENGTH_SHORT).show();
                    getTasks(); // Refresh tasks
                })
                .addOnFailureListener(e -> {
                    Log.e("TaskActivity", "Error creating task: " + e.getMessage(), e);
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

        // Create input fields pre-filled with task data
        final EditText inputTitle = new EditText(this);
        inputTitle.setText(task.getTitle());
        inputTitle.setHint("Task Title");

        final EditText inputDescription = new EditText(this);
        inputDescription.setText(task.getDescription());
        inputDescription.setHint("Task Description");

        // Priority Spinner
        final Spinner inputPriority = new Spinner(this);
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Low", "Medium", "High"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputPriority.setAdapter(priorityAdapter);

        // Set the spinner's current selection
        String currentPriority = task.getPriority();
        int priorityPosition = priorityAdapter.getPosition(currentPriority);
        if (priorityPosition != -1) {
            inputPriority.setSelection(priorityPosition);
        }

        // Due Date with DatePicker
        final TextView dueDateText = new TextView(this);
        dueDateText.setText(task.getDueDate());
        dueDateText.setHint("Select Due Date");
        dueDateText.setTextSize(20);

        // Add DatePicker functionality
        dueDateText.setOnClickListener(v -> showDatePickerDialog(dueDateText));

        // Arrange inputs vertically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputTitle);
        layout.addView(inputDescription);
        layout.addView(inputPriority);
        layout.addView(dueDateText);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newTitle = inputTitle.getText().toString().trim();
            String newDescription = inputDescription.getText().toString().trim();
            String newPriority = inputPriority.getSelectedItem().toString().trim();
            String newDueDate = dueDateText.getText().toString().trim();

            if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newDescription) || TextUtils.isEmpty(newPriority) || TextUtils.isEmpty(newDueDate)) {
                Toast.makeText(TaskActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            updateTask(task.getTaskId(), newTitle, newDescription, newPriority, newDueDate);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void updateTask(String taskId, String newTitle, String newDescription, String newPriority, String newDueDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("description", newDescription);
        updates.put("priority", newPriority);
        updates.put("dueDate", newDueDate);

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

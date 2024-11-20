package com.app.task_manager;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.*;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskActionListener onTaskActionListener;

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.onTaskActionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskdate.setText(task.getDueDate());
        holder.deleteButton.setOnClickListener(v -> onTaskActionListener.onDeleteTask(task.getTaskId()));
        holder.editButton.setOnClickListener(v -> onTaskActionListener.onEditTask(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskdate;
        Button deleteButton, editButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskdate = itemView.findViewById(R.id.taskDueDate);
            deleteButton = itemView.findViewById(R.id.deleteTaskButton);
            editButton = itemView.findViewById(R.id.editTaskButton);
        }
    }

    public interface OnTaskActionListener {
        void onDeleteTask(String taskId);
        void onEditTask(Task task);
    }
}

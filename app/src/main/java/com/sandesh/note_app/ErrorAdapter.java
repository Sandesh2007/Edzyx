package com.sandesh.note_app;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder> {

    private List<ErrorItem> errorList;
    private Context context;

    public ErrorAdapter(List<ErrorItem> errorList, Context context) {
        this.errorList = errorList;
        this.context = context;
    }

    @NonNull
    @Override
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.error_item, parent, false);
        return new ErrorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder holder, int position) {
        // Bind data to the views
        ErrorItem errorItem = errorList.get(position);
        holder.titleTextView.setText(errorItem.getTitle());
        holder.fixTextView.setText(errorItem.getFix());
    }

    @Override
    public int getItemCount() {
        return errorList.size();
    }

    public static class ErrorViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView fixTextView;

        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the TextViews here
            titleTextView = itemView.findViewById(R.id.error_title);
            fixTextView = itemView.findViewById(R.id.error_fix);
        }
    }
}

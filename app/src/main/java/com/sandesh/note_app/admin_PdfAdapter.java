package com.sandesh.note_app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class admin_PdfAdapter extends RecyclerView.Adapter<admin_PdfAdapter.PdfViewHolder> {

    private Context context;
    private List<uploadPdf> uploadPDFS;
    private OnDeleteClickListener onDeleteClickListener;

    public admin_PdfAdapter(Context context, List<uploadPdf> uploadPDFS, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.uploadPDFS = uploadPDFS;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_list_item, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        uploadPdf currentPdf = uploadPDFS.get(position);
        String fileName = currentPdf.getName();
        String fileSize = android.text.format.Formatter.formatShortFileSize(context, currentPdf.getSize());
        String uploadedDate = android.text.format.DateFormat.format("dd/MM/yyyy", currentPdf.getUploadedDate()).toString();

        holder.textFilename.setText(fileName);
        holder.textSize.setText("Size: " + fileSize);

        holder.itemView.setOnClickListener(v -> {
            String fileUrl = currentPdf.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                context.startActivity(browserIntent);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(currentPdf, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploadPDFS.size();
    }

    public static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView textFilename;
        TextView textSize;
        Button deleteButton;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            textFilename = itemView.findViewById(R.id.text_filename);
            textSize = itemView.findViewById(R.id.text_size);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(uploadPdf pdf, int position);
    }
}

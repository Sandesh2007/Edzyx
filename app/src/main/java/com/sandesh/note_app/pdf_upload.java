package com.sandesh.note_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class pdf_upload extends AppCompatActivity {
    ImageView imageView;
    Button upload_btn;
    StorageReference storageReference;
    DatabaseReference databaseReference;

    Spinner referenceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_upload);

        imageView = findViewById(R.id.imageView);
        upload_btn = findViewById(R.id.upload_btn);
        referenceSpinner = findViewById(R.id.reference_spinner);  // Initialize Spinner

        // References in Firebase
        String[] references = getResources().getStringArray(R.array.pdf_references);

        // Set Spinner with references
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, references);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        referenceSpinner.setAdapter(spinnerAdapter);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        upload_btn.setOnClickListener(view -> select_pdf());
    }

    private void select_pdf() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
                "application/pdf",
                "application/vnd.ms-powerpoint",  // MIME type for .ppt
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // MIME type for .pptx
                "application/vnd.google-apps.document"  // MIME type for Google Docs
        });
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF/PPT file"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadPdfFile(data.getData());
        }
    }


    private void uploadPdfFile(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        String fileName = getFileName(data);
        if (fileName == null) {
            fileName = System.currentTimeMillis() + ".pdf";
        }

        // Get selected reference from the spinner
        String selectedReference = referenceSpinner.getSelectedItem().toString();

        // Use the selected reference in the Firebase path
        StorageReference reference = storageReference.child(selectedReference + "/" + fileName);

        String finalFileName = fileName;
        reference.putFile(data).addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                long uploadedDate = System.currentTimeMillis();
                long size = taskSnapshot.getTotalByteCount(); // Get file size

                uploadPdf uploadPdf = new uploadPdf(finalFileName, uri.toString(), uploadedDate, size);

                // Store metadata under the selected reference
                databaseReference.child(selectedReference).child(Objects.requireNonNull(databaseReference.push().getKey()))
                        .setValue(uploadPdf)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(pdf_upload.this, "Uploaded file successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(pdf_upload.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
                            }
                        });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(pdf_upload.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(pdf_upload.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressDialog.setMessage("Uploaded: " + (int) progress + "%");
        });
    }


    // Helper method to get the file name from the Uri
    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri != null) {
            Cursor cursor = null;
            try {
                String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME};
                cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    fileName = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return fileName;
    }
}

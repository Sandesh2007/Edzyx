package com.sandesh.note_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
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
        referenceSpinner = findViewById(R.id.reference_spinner);

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
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.google-apps.document"
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
        CustomProgressDialog customProgressDialog = new CustomProgressDialog(this);

        String fileName = getFileName(data);
        if (fileName == null) {
            fileName = System.currentTimeMillis() + ".pdf";
        }

        String selectedReference = referenceSpinner.getSelectedItem().toString();
        StorageReference reference = storageReference.child(selectedReference + "/" + fileName);

        // Check if the file already exists in Firebase Storage
        String finalFileName1 = fileName;
        reference.getDownloadUrl().addOnSuccessListener(uri -> {
            // If the file exists, show a toast and do not proceed with the upload
            showCustomToast(pdf_upload.this, "File already exsits in the storage!!");
        }).addOnFailureListener(exception -> {
            // File does not exist, proceed with the upload
            customProgressDialog.show();

            String finalFileName = finalFileName1;
            reference.putFile(data).addOnSuccessListener(taskSnapshot -> {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                    long uploadedDate = System.currentTimeMillis();
                    long size = taskSnapshot.getTotalByteCount();
                    uploadPdf uploadPdf = new uploadPdf(finalFileName, uri1.toString(), uploadedDate, size);

                    databaseReference.child(selectedReference).child(Objects.requireNonNull(databaseReference.push().getKey()))
                            .setValue(uploadPdf)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    showCustomToast(pdf_upload.this, "Uploaded file successfully");
                                    customProgressDialog.changeAnimation(R.raw.done_anim,()->{
                                        Log.d("Animation ended","Animation ended di it ");
                                    });
                                }else {
                                    showCustomToast(pdf_upload.this, "Failed to upload file");
                                }
                            });
                }).addOnFailureListener(e -> {
                    customProgressDialog.dismiss();
                    showCustomToast(pdf_upload.this, "Failed to get download URL");
                });
            }).addOnFailureListener(e -> {
                customProgressDialog.dismiss();
                showCustomToast(pdf_upload.this, "Failed to upload file");
            }).addOnProgressListener(snapshot -> {
                long bytesTransferred = snapshot.getBytesTransferred();
                long totalBytes = snapshot.getTotalByteCount();
                double progress = (100.0 * bytesTransferred) / totalBytes;

                // Update the progress in the custom progress dialog with both percentage and bytes transferred
                customProgressDialog.setProgress((int) progress, bytesTransferred, totalBytes);
            });
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
    private void showCustomToast(Context context, String message) {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custm_toast));

        TextView toastText = toastLayout.findViewById(R.id.toast_text);

        toastText.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }

}

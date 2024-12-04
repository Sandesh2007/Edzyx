package com.sandesh.note_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class help extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ErrorAdapter errorAdapter;
    private List<ErrorItem> errorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchErrorJsonFromFirebase();
    }

    private void fetchErrorJsonFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/note-app-a0808.appspot.com/o/Errors.json?alt=media&token=50d90e1b-8c6e-45ed-af55-ecfb39a5800c");

        storageReference.getBytes(10 * 1024 * 1024) // 10 mb size for future!!
                .addOnSuccessListener(bytes -> {
                    String json = new String(bytes);

                    // Parse the JSON using Gson
                    Gson gson = new Gson();
                    // Parse as an object first to access the "errors" array
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);

                    // Set the errorList from the "errors" array
                    errorList = errorResponse.getErrors();

                    // Set up the adapter with the error list
                    errorAdapter = new ErrorAdapter(errorList, this);
                    recyclerView.setAdapter(errorAdapter);

                })
                .addOnFailureListener(exception -> Toast.makeText(this, "Error fetching JSON, "+exception, Toast.LENGTH_LONG).show());
    }

}

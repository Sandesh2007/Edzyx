package com.sandesh.note_app;

import android.content.Intent;
//import android.graphics.Color;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
//import android.widget.TextClock;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
//import java.util.Objects;

public class files_fragment extends Fragment {
    Button upload_btn;
    ListView pdf_list;
    SearchView search_view;
    DatabaseReference databaseReference;
    List<uploadPdf> uploadPDFS;
    ArrayAdapter<String> adapter;
    List<String> fileNames;

    Spinner referenceSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files_fragment, container, false);

        // Initialize ListView, Spinner, and SearchView
        pdf_list = view.findViewById(R.id.pdf_list);
        search_view = view.findViewById(R.id.search_view);
        referenceSpinner = view.findViewById(R.id.reference_spinner);
        upload_btn = view.findViewById(R.id.upload_btn);

        uploadPDFS = new ArrayList<>();
        fileNames = new ArrayList<>();

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), pdf_upload.class);
                startActivity(intent);
            }
        });

        // Set up Spinner for references
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.pdf_references));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        referenceSpinner.setAdapter(spinnerAdapter);

        // Load files based on selected reference
        referenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedReference = adapterView.getItemAtPosition(position).toString();
                viewAllFiles(selectedReference);  // Pass selected reference to method
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle case when nothing is selected
            }
        });

        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        return view;
    }

    // Modified viewAllFiles method to accept a reference and load files from that path
    private void viewAllFiles(String reference) {
        databaseReference = FirebaseDatabase.getInstance().getReference(reference);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) {
                    return;
                }

                uploadPDFS.clear();
                fileNames.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    uploadPdf uploadPdf = postSnapshot.getValue(uploadPdf.class);
                    if (uploadPdf != null) {
                        uploadPDFS.add(uploadPdf);

                        String fileName = uploadPdf.getName();
                        String fileSize = android.text.format.Formatter.formatShortFileSize(requireContext(), uploadPdf.getSize());
                        String uploadedDate = DateFormat.format("dd/MM/yyyy", uploadPdf.getUploadedDate()).toString();

                        String displayText = fileName + "\nSize: " + fileSize + "\nUploaded: " + uploadedDate;
                        fileNames.add(displayText);
                    }
                }

                adapter = new ArrayAdapter<>(
                        requireContext(),
                        R.layout.list_item,
                        R.id.text_item,
                        fileNames
                );

                pdf_list.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Error loading files", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
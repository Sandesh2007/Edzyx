package com.sandesh.note_app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class files_fragment extends Fragment {
    Button upload_btn;
    RecyclerView pdfRecyclerView;
    PdfAdapter pdfAdapter;
    SearchView search_view;
    DatabaseReference databaseReference;
    List<uploadPdf> uploadPDFS;
    ArrayAdapter<String> adapter;
    List<String> fileNames;
    FirebaseAuth userAuth;
    Spinner referenceSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files_fragment, container, false);

        // Initialize ListView, Spinner, and SearchView
        pdfRecyclerView = view.findViewById(R.id.pdf_recycler_view);
        search_view = view.findViewById(R.id.search_view);
        referenceSpinner = view.findViewById(R.id.reference_spinner);
        upload_btn = view.findViewById(R.id.upload_btn);

        uploadPDFS = new ArrayList<>();
        fileNames = new ArrayList<>();

        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }


        View parentLayout = view.findViewById(R.id.parent_layout);
        parentLayout.setOnClickListener(view1 -> search_view.clearFocus());
        upload_btn.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), pdf_upload.class);
            startActivity(intent);
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
                // nothing is selected.
            }
        });

        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Implement filter functionality for RecyclerView
                if (pdfAdapter != null) {
                    List<uploadPdf> filteredList = new ArrayList<>();
                    for (uploadPdf item : uploadPDFS) {
                        if (item.getName().toLowerCase().contains(newText.toLowerCase())) {
                            filteredList.add(item);  // Filter based on the name of the PDF
                        }
                    }
                    // Pass the filtered list of uploadPdf objects to the adapter
                    pdfAdapter = new PdfAdapter(getContext(), filteredList);
                    pdfRecyclerView.setAdapter(pdfAdapter);
                }
                return false;
            }
        });


        // Donot allow user to upload file if not verified.
        userAuth = FirebaseAuth.getInstance();
        FirebaseUser user = userAuth.getCurrentUser();
        if (user != null) if (!user.isEmailVerified()) {
            upload_btn.setOnClickListener(view13 -> showCustomToast(getContext(), "Please verify your email to upload files!!","error"));
        }

//      Donot allow user to upload file if version is less than 1.4 .
        try {
            // Get the current app version
            String C_versionName = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;

            // Define the minimum required version
            String min_version = "1.4";

            // Compare the current version with the minimum version
            if (!(C_versionName.compareTo(min_version) >= 0)) {
                upload_btn.setOnClickListener(view14 -> showCustomToast(getContext(),"Update app to upload files!!","error"));
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }



        return view;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoInternetDialog() {
        NoInternetDialogFragment dialog = new NoInternetDialogFragment();
        dialog.show(getParentFragmentManager(), "NoInternetDialog");
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

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    uploadPdf uploadPdf = postSnapshot.getValue(uploadPdf.class);
                    if (uploadPdf != null) {
                        uploadPDFS.add(uploadPdf);  // Add the entire uploadPdf object
                    }
                }

                // Pass the list of uploadPdf objects to the PdfAdapter
                pdfAdapter = new PdfAdapter(requireContext(), uploadPDFS);
                pdfRecyclerView.setAdapter(pdfAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getActivity() != null) {
                    showCustomToast(getContext(), "Error: " + error.getMessage(), "error");
                }
            }
        });
    }

    private void showCustomToast(Context context, String message, String messageType) {
        // Inflate the custom toast layout
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, getView().findViewById(R.id.custm_toast));

        // Set the text in the custom toast layout
        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);

        // Set the text color based on the message type
        switch (messageType.toLowerCase()) {
            case "error":
                toastText.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
            case "success":
                toastText.setTextColor(ContextCompat.getColor(context, R.color.success_color));
                break;
            default:
                toastText.setTextColor(ContextCompat.getColor(context, R.color.white));
                break;
        }

        // Create and display the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }

}
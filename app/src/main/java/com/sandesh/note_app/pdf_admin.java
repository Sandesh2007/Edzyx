package com.sandesh.note_app;

import android.app.AlertDialog;
import android.content.Context;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class pdf_admin extends Fragment {
    RecyclerView pdfRecyclerView;
    admin_PdfAdapter pdfAdapter;
    SearchView search_view;
    DatabaseReference databaseReference;
    List<uploadPdf> uploadPDFS;
    ArrayAdapter<String> adapter;
    List<String> fileNames;
    Spinner referenceSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_admin, container, false);

        pdfRecyclerView = view.findViewById(R.id.pdf_recycler_view);
        search_view = view.findViewById(R.id.search_view);
        referenceSpinner = view.findViewById(R.id.reference_spinner);

        uploadPDFS = new ArrayList<>();
        fileNames = new ArrayList<>();

        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        View parentLayout = view.findViewById(R.id.parent_layout);
        parentLayout.setOnClickListener(view1 -> search_view.clearFocus());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.pdf_references));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        referenceSpinner.setAdapter(spinnerAdapter);

        referenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedReference = adapterView.getItemAtPosition(position).toString();
                viewAllFiles(selectedReference);
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
                if (pdfAdapter != null) {
                    List<uploadPdf> filteredList = new ArrayList<>();
                    for (uploadPdf item : uploadPDFS) {
                        if (item.getName().toLowerCase().contains(newText.toLowerCase())) {
                            filteredList.add(item);
                        }
                    }
                    pdfAdapter = new admin_PdfAdapter(getContext(), filteredList, (pdf, position) -> deletePdfFromDatabaseAndStorage(pdf, position));
                    pdfRecyclerView.setAdapter(pdfAdapter);
                }
                return false;
            }
        });

        return view;
    }

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
                        uploadPDFS.add(uploadPdf);
                    }
                }

                pdfAdapter = new admin_PdfAdapter(requireContext(), uploadPDFS, (pdf, position) -> deletePdfFromDatabaseAndStorage(pdf, position));
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

    private void deletePdfFromDatabaseAndStorage(uploadPdf pdf, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Do you really want to delete this file, admin?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with deletion
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                            .getReference(referenceSpinner.getSelectedItem().toString()); // Reference based on selected spinner item

                    databaseRef.orderByChild("name").equalTo(pdf.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    String pdfKey = postSnapshot.getKey();
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdf.getUrl());

                                    storageRef.delete().addOnSuccessListener(aVoid -> {
                                        databaseRef.child(pdfKey).removeValue().addOnSuccessListener(aVoid1 -> {
                                            if (position >= 0 && position < uploadPDFS.size()) {
                                                uploadPDFS.remove(position);
                                                pdfAdapter.notifyItemRemoved(position);
                                            } else {
                                                showCustomToast(getContext(), "Position out of range", "error");
                                            }
                                            showCustomToast(getContext(), "PDF deleted successfully", "success");
                                        }).addOnFailureListener(e -> {
                                            showCustomToast(getContext(), "Error: " + e.getMessage(), "error");
                                        });
                                    }).addOnFailureListener(e -> {
                                        showCustomToast(getContext(), "Error deleting file from storage: " + e.getMessage(), "error");
                                    });

                                    break;
                                }
                            } else {
                                showCustomToast(getContext(), "PDF not found in database", "error");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showCustomToast(getContext(), "Database error: " + error.getMessage(), "error");
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User cancelled the dialog
                    dialog.dismiss();
                })
                .show();
    }





    private void showCustomToast(Context context, String message, String messageType) {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, getView().findViewById(R.id.custm_toast));

        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);

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

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        toast.show();
    }
}

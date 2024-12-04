package com.sandesh.note_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class admin extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);
        Button pdf = findViewById(R.id.pdf_activity);

            pdf.setOnClickListener(view -> {
                Fragment pdfFragment = new pdf_admin();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, pdfFragment); // Ensure this ID matches the container ID in activity_admin.xml
                fragmentTransaction.addToBackStack(null); // Optional: allows the user to navigate back to the previous fragment
                fragmentTransaction.commit();
            });


        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        // Load users from Firebase
        loadUsers();


        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return false;
            }
        });

        View parentLayout = findViewById(R.id.main);
        parentLayout.setOnClickListener(view1 -> searchView.clearFocus());
    }

    private void loadUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(admin.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        // Handle null values and empty strings
        if (query == null) {
            query = "";
        }

        String lowerCaseQuery = query.toLowerCase();

        // Filter users based on the query
        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseQuery) ||
                    user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
    }
}
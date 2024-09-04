package com.sandesh.note_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);


        Button getstart = findViewById(R.id.getStartedButton);
        if (getstart != null) {
            getstart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, signup.class);
                    startActivity(intent);
                }
            });
        } else {
            // Handle the case where the button is not found
            throw new NullPointerException("Button with ID getStartedButton not found.");
        }


        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // User is logged in
            Intent intent = new Intent(MainActivity.this, home.class);
            startActivity(intent);
            finish(); // Close MainActivity so user can't navigate back to it
        } else {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        // Apply WindowInsets to the view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

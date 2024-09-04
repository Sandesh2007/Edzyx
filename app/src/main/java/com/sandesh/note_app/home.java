package com.sandesh.note_app;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.sandesh.note_app.databinding.ActivityHomeBinding;


public class home extends AppCompatActivity {
     ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchfragment(new home_fragment());

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.navigation_home){
                switchfragment(new home_fragment());
            }
            else if (item.getItemId()== R.id.navigation_files){

                switchfragment(new files_fragment());
            }
            else if (item.getItemId()== R.id.navigation_profile) {

                switchfragment(new ProfileFragment());
            }
            return true;
        });
    }
    private  void switchfragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

}

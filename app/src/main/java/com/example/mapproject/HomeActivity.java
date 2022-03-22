package com.example.mapproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView logout;
    private TextView welcome;
    FirebaseAuth fAuth;
    BottomNavigationView bottomNavigationView;
    String from="";

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case R.id.nav_map:
                            selectedFragment = new MapsFragment();
                            toolbar.setTitle("Map");
                            break;
                        case R.id.nav_note:
                            selectedFragment = new NoteFragment();
                            toolbar.setTitle("Notes");
                            welcome.setText("");
                            break;

                    }
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, selectedFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    return true;
                }
            };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_layout);
        fAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolBar);
        logout = findViewById(R.id.toolBarLogout);
        welcome = findViewById(R.id.welcome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);


        bottomNavigationView.setSelectedItemId(R.id.nav_map);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                fAuth.signOut();
                finish();
            }
        });

    }



}

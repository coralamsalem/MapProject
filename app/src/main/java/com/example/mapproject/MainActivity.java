package com.example.mapproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    Button btnReg;
    Button btnLog;

    private FirebaseAuth fAuth;
    String mUID;
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser User = fAuth.getCurrentUser();
        if(User!=null){
            Intent intent=new Intent(this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.login_field_email);
        password = findViewById(R.id.login_field_Password);
        btnReg = findViewById(R.id.button_Reg);
        btnLog = findViewById(R.id.button_log);


        fAuth = FirebaseAuth.getInstance();

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email1 = email.getText().toString();
                String pass = password.getText().toString();

                if(TextUtils.isEmpty(email1)&&TextUtils.isEmpty(pass))
                {
                    email.setError("Email is required");
                    password.setError("Password is required");
                    return;
                }
                else if(TextUtils.isEmpty(email1))
                {
                    email.setError("Email is required");
                    return;
                }
                else if(TextUtils.isEmpty(pass))
                {
                    password.setError("Password is required");
                    return;
                }
                else if (pass.length()<6)
                {
                    password.setError("Password must be >=6 characters");
                    return;
                }

                else{
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Login");
                    progressDialog.show();


                    fAuth.signInWithEmailAndPassword(email1,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(fAuth.getCurrentUser().getUid());

                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                                Toast.makeText(MainActivity.this,"Log In Successfully", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                            }
                            else{
                                Toast.makeText(MainActivity.this,"Error: "+ task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });
    }
}
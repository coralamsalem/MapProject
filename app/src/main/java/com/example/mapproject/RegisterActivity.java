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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText name, email, password, com_password;
    private Button registerButton;
    ProgressDialog progressDialog;
    FirebaseAuth fAuth;
    String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        name = findViewById(R.id.register_field_Fname);
        email = findViewById(R.id.register_field_email);
        password = findViewById(R.id.register_field_Password);
        com_password = findViewById(R.id.register_field_com_Password);
        registerButton = findViewById(R.id.reg_btn);
        fAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String full_name = name.getText().toString();
                String email1 = email.getText().toString();
                String pass = password.getText().toString();
                String com_pass = com_password.getText().toString();


                if (TextUtils.isEmpty(full_name) && TextUtils.isEmpty(email1) && TextUtils.isEmpty(pass) && TextUtils.isEmpty(com_pass)) {
                    name.setError("Full name is required");
                    email.setError("Email is required");
                    password.setError("Password is required");
                    com_password.setError("Confirm password is required");
                    return;
                } else if (TextUtils.isEmpty(full_name)) {
                    name.setError("Full name is required");
                    return;
                } else if (TextUtils.isEmpty(email1)) {
                    email.setError("Email is required");
                    return;
                } else if (TextUtils.isEmpty(pass)) {
                    password.setError("Password is required");
                    return;
                } else if (TextUtils.isEmpty(com_pass)) {
                    com_password.setError("Confirm password is required");
                    return;
                } else if (pass.length() < 6 || com_pass.length() < 6) {
                    password.setError("Password must be >=6 characters");
                    com_password.setError("Password must be >=6 characters");
                    return;
                } else if (!pass.equals(com_pass)) {
                    com_password.setError("Confirm password is incorrect");
                    return;
                } else {

                    register(full_name, email1, pass);
                }
            }
        });
    }

    private void register(String name,String email,String password){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("User Creating");
        progressDialog.show();

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userID = fAuth.getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

                    Toast.makeText(RegisterActivity.this,"User Created", Toast.LENGTH_LONG).show();
                    userID = fAuth.getCurrentUser().getUid();
                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                    Map<String, Object> user= new HashMap<>();
                    user.put("fullName",name);
                    user.put("email",email);
                    user.put("password", password);
                    reference2.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this,"Error: "+ task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}

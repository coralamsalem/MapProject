package com.example.mapproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class OneNoteActivity extends AppCompatActivity
{
    String title,noteSt,dateSt,noteId, userId, location, from;
    double lon, lat;
    private EditText noteTitle, note;
    private Button date, update, delete;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    FirebaseAuth fAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_note);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        noteSt = intent.getStringExtra("note");
        dateSt = intent.getStringExtra("date");
        noteId = intent.getStringExtra("noteId");
        userId = intent.getStringExtra("userId");
        location = intent.getStringExtra("location");
        lon = intent.getDoubleExtra("lon", 0.0);
        lat = intent.getDoubleExtra("lat", 0.0);


        fAuth = FirebaseAuth.getInstance();

        note = findViewById(R.id.edit_note);
        noteTitle = findViewById(R.id.edit_note_title);
        date = findViewById(R.id.edit_date);
        update = findViewById(R.id.update_btn);
        delete = findViewById(R.id.delete_btn);
        note.setText(noteSt);
        noteTitle.setText(title);
        date.setText(dateSt);


        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        OneNoteActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                dateSt = day + "/" + month + "/" + year;
                date.setText(dateSt);
            }
        };

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title =  noteTitle.getText().toString();
                noteSt = note.getText().toString();
                if (TextUtils.isEmpty(title))
                {
                    noteTitle.setError("Title is required");
                }
                if (TextUtils.isEmpty(noteSt))
                {
                    note.setError("Not description is required");
                }else {

                    uploadToFirebase();
                }
            }


        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noteId.isEmpty())
                {
                    Intent intent = new Intent(OneNoteActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                else{
                    FirebaseDatabase reference3 = FirebaseDatabase.getInstance();
                    reference3.getReference("Users").child(userId).child("Notes").child(noteId).removeValue();
                    Intent intent = new Intent(OneNoteActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });





    }
    private void uploadToFirebase() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        userId = fAuth.getCurrentUser().getUid();
        DatabaseReference reference;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Notes").child(noteId);


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("noteId", noteId);
        hashMap.put("title", title);
        hashMap.put("note", noteSt);
        hashMap.put("date", dateSt);
        hashMap.put("user", userId);
        hashMap.put("latitude", lat);
        hashMap.put("longitute", lon);
        hashMap.put("location", location);
        reference.setValue(hashMap);
        progressDialog.dismiss();
        startActivity(new Intent(OneNoteActivity.this, HomeActivity.class));
        finish();
    }
}

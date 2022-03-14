package com.example.mapproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OneNoteActivity extends AppCompatActivity
{
    String title,noteSt,dateSt,noteId;
    private TextView noteTitle, note, date;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_note);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        noteSt = intent.getStringExtra("note");
        dateSt = intent.getStringExtra("date");
        noteId = intent.getStringExtra("noteId");

        note = findViewById(R.id.one_note);
        noteTitle = findViewById(R.id.one_note_title);
        date = findViewById(R.id.one_note_date);

        note.setText(noteSt);
        noteTitle.setText(title);
        date.setText(dateSt);
    }
}

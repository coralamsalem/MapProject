package com.example.mapproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoteFragment extends Fragment {
    MapsFragment mapsFragment;
    FloatingActionButton addNewNoteBtn;
    ImageView edit;
    RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> notesList;
    FirebaseAuth fAuth;

    public NoteFragment() {

    }


    public static NoteFragment newInstance(String param1, String param2) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        addNewNoteBtn = view.findViewById(R.id.note_btn);

        fAuth = FirebaseAuth.getInstance();

        addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewNoteActivity.class);
                startActivity(intent);
            }

        });



        recyclerView = (RecyclerView) view.findViewById(R.id.note_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        notesList = new ArrayList<>();
        //  postsList.add(post);

        noteAdapter = new NoteAdapter(getContext(), notesList);
        recyclerView.setAdapter(noteAdapter);
        readNote();
        return view;
    }

    private void readNote() {
        String userId = fAuth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Notes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {


                    notesList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String noteId = snapshot1.child("noteId").getValue().toString();
                        String title = snapshot1.child("title").getValue().toString();
                        String note = snapshot1.child("note").getValue().toString();
                        String date = snapshot1.child("date").getValue().toString();
                        String user = snapshot1.child("user").getValue().toString();
                        String latitude = snapshot1.child("latitude").getValue().toString();
                        String longitute = snapshot1.child("longitute").getValue().toString();
                        String location = snapshot1.child("location").getValue().toString();
                        Note note1 = new Note(title, note, user, date, location, latitude, longitute, noteId);
                        notesList.add(note1);
                    }
                    noteAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}



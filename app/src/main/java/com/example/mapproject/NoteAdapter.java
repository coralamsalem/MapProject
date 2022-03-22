package com.example.mapproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class NoteAdapter  extends RecyclerView.Adapter<NoteAdapter.ViewHolder>{

    private Context context;
    private List<Note> noteList;

    private FirebaseUser firebaseUser;

    public NoteAdapter(Context context, List<Note> note){
        this.context = context;
        this.noteList = note;

    }
    @NonNull
    @NotNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);


        return new NoteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.note.setText(note.getDescription());
        holder.date.setText(note.getDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OneNoteActivity.class);
                intent.putExtra("noteId", note.getNoteId());
                intent.putExtra("location", note.getLocation());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("note", note.getDescription());
                intent.putExtra("date", note.getDate());
                intent.putExtra("userId", note.getUser());
                intent.putExtra("lat",note.getPosition().latitude);
                intent.putExtra("lon",note.getPosition().longitude);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView noteTitle, note, date;
        public ImageView image, edit;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.note_title);
            note = itemView.findViewById(R.id.note);
            date =  itemView.findViewById(R.id.note_date);
            image = itemView.findViewById(R.id.note_image);


        }
    }


}

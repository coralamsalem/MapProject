package com.example.mapproject.Utils;

import static com.example.mapproject.Utils.Constants.MARKER_DIMENSION;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mapproject.OneNoteActivity;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


public class GeoNotesUtils {

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, MARKER_DIMENSION, MARKER_DIMENSION);
        Bitmap bitmap = Bitmap.createBitmap(MARKER_DIMENSION, MARKER_DIMENSION, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static void showMarkerInfo(AppCompatActivity context, String user, String title, String description,String date,String location,double latitude,double longitute,String noteId) {
        Intent intent = new Intent(context, OneNoteActivity.class);
        intent.putExtra("noteId", noteId);
        intent.putExtra("location",location);
        intent.putExtra("title", title);
        intent.putExtra("note", description);
        intent.putExtra("date", date);
        intent.putExtra("userId", user);
        intent.putExtra("lat", latitude);
        intent.putExtra("lon", longitute);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
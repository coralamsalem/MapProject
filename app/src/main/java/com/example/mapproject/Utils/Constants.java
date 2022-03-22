package com.example.mapproject.Utils;

public class Constants {

    public static final String SHARED_PREFERENCES = "MyPrefs";
    public static final String WIDGET_NOTES_SELECTED = "NotesList";

    // private constructor – this is to avoid clients from instantiating the class
    private Constants(){}

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";

    public static final Double DEFAULT_LATITUDE = 34.7781308;  //1.2950869;
    public static final Double DEFAULT_LONGITUDE = 32.0215684; //103.8419714;

    public static final String DEFAULT_TITLE = "GeoNotes";
    public static final float DEFAULT_ZOOM = 15f;

    public static final int MARKER_DIMENSION = 80;


}

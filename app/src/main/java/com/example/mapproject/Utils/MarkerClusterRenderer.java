package com.example.mapproject.Utils;

import android.content.Context;

import com.example.mapproject.Note;
import com.example.mapproject.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MarkerClusterRenderer extends DefaultClusterRenderer<Note> {

    private Context context;
    private String user;

    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager<Note> clusterManager, String user) {
        super(context, map, clusterManager);
        this.context = context;
        this.user = user;
        clusterManager.setRenderer(this);
    }


    @Override
    protected void onBeforeClusterItemRendered(Note item, MarkerOptions markerOptions) {

       markerOptions.icon(GeoNotesUtils.bitmapDescriptorFromVector(context, R.drawable.ic_marker_cluster));

        markerOptions.visible(true);
    }
}

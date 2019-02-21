package com.gruas.app.servicio;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Dani on 13/05/2014.
 */
public class ItemCluster implements ClusterItem {


    private LatLng mPosition;
    private String title;
    private Bitmap bm;

    public ItemCluster(LatLng position){
        mPosition = position;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(Bitmap bm) {
        this.bm = bm;
    }

    public Bitmap getIcon() {
        return bm;
    }


}

class MyClusterRenderer extends DefaultClusterRenderer<ItemCluster> {

    public MyClusterRenderer(Context context, GoogleMap map,
                             ClusterManager<ItemCluster> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ItemCluster item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.title(item.getTitle());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(item.getIcon()));
    }

    @Override
    protected void onClusterItemRendered(ItemCluster clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }
}
package com.gruas.app;

import com.gruas.app.couchBaseLite.CouchDelegate;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.services.gps.GPSDelegate;
import com.gruas.app.services.gps.GPSLocation;

public interface GruasDelegate extends CouchDelegate,GPSDelegate,GPSLocation {
    public void notificationReceived(DocumentsAdapter adaptador, String idUsuario);
    public void cancelarNotificacion();
    public void pressBackAnotherActivity();
    public void clearTextSearch();
    public void stopedGPSCouchDBService();
}

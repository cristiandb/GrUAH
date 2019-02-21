package com.gruas.app.services.gps;

import android.app.Activity;

import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;

public interface GPSDelegate {
    public void cambioEstadoGPS(int i);
    public void cambioLocalizacion(GPSObject gpsobject);
    public void enabledGPS(boolean state);
    public String getIdUsuario();
    public Activity getActivity();
    public void startedServiceCouch(DocumentsAdapter adaptador,boolean gps_enabled);
    public void handlerFatalError(GPSException.TypeErrors typeGPS, CouchException.TypeErrors typeCouch, String msg);
}

package com.gruas.app.services.gps;
import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Date;


public class GPSManager {
    private LocationManager manager;
    private GPSLocationListener location_listener;
    private GPSObject lastLocation = null;
    private boolean newLocation = false;
    private Activity delegate = null;

    public GPSManager(LocationManager lm){
        this.manager = lm;
        this.location_listener = new GPSLocationListener();
    }

    public void arrancarServicio(){
        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        String proveedor = manager.getBestProvider(criterio, true);
        manager.requestLocationUpdates(proveedor,0,0,location_listener);
        //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,location_listener);
    }

    public boolean isGPSEnabled(){
        if(manager == null) return false;
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void detenerServicio(){
        manager.removeUpdates(location_listener);
    }


    public GPSObject getLastLocation(){
        newLocation = false;
        return lastLocation;
    }

    public boolean isNewLocation(){
        return this.newLocation;
    }

    public void setDelegate(Activity delegate){
        this.delegate = delegate;
    }

    private class GPSLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            //La distancia se mide en metros, nos puede servir para definir un area de por ejemplo 500m
            //y entonces llamar a un metodo del delegate para avisarle de un evento de zona por ejemplo
            if(lastLocation == null) newLocation = true;
            else if(!newLocation)
                newLocation = (location.distanceTo(lastLocation.getLocation()) != 0);

            if(newLocation)
                lastLocation = new GPSObject(location,new Date());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            if(delegate != null){
                final int status = i;
                System.out.println("Entra");
                // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                /*delegate.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delegate.cambioEstadoGPS(status);
                    }
                });*/
            }
        }

        @Override
        public void onProviderEnabled(String s) {
            //GPS Activado
            System.out.println("Entra");
            if(delegate != null){
                // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                /*delegate.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delegate.enabledGPS(true);
                    }
                });*/
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            //GPS Desactivado
            System.out.println("Entra");
            if(delegate != null){
                // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                /*delegate.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delegate.enabledGPS(false);
                    }
                });*/
            }
        }
    }
}
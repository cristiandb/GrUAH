package com.gruas.app.services.gps;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSObject {
    private Location location;
    private Date date;

    public GPSObject(Location loc, Date date){
        this.location = loc;
        this.date = date;
    }

    public Location getLocation(){
        return this.location;
    }

    public String getStringDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(date);
    }

    public long getLongDate(){
        return date.getTime();
    }

    public static GPSObject createGPSObject(double longitud, double latitud){
        Location targetLocation = new Location(""); //no nos hace falta el provider
        targetLocation.setLatitude(latitud);//your coords of course
        targetLocation.setLongitude(longitud);
        return new GPSObject(targetLocation,new Date());
    }
}

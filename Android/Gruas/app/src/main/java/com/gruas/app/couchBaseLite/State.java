package com.gruas.app.couchBaseLite;

import com.google.android.gms.maps.model.LatLng;
import com.gruas.app.UtilDateFormat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class State implements Serializable {
    private String estado;
    private Date fecha;
    private double latitud, longitud;

    public State(Date fecha, String estado,double latitud, double longitud){
        this.fecha = fecha;
        this.estado = estado;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public LatLng getLocation(){
        return new LatLng(latitud,longitud);
    }

    public String getFecha(){
        return UtilDateFormat.getStringDateFormatWithDate(fecha);
    }

    public String getEstado(){
        return this.estado;
    }
}

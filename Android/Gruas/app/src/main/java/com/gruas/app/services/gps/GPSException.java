package com.gruas.app.services.gps;

import com.gruas.app.couchBaseLite.CouchException;

public class GPSException extends Exception {
    public enum TypeErrors {
        NO_CREATE_GPSMANAGER,
        BAD_DELEGATE,
        BAD_DELEGATE_LOCATION,
        FATAL_ERROR
    }

    private TypeErrors type;
    private String superior_msg;
    public static final String TAG = "GPSException";

    public GPSException(TypeErrors type, String msg){
        super();
        this.type = type;
        this.superior_msg = (msg != null)? msg : "";
    }

    public TypeErrors getType(){
        return type;
    }

    @Override
    public String getMessage() {
        String msg_error = (superior_msg.isEmpty())? "" : superior_msg + "\n";
        if(type == TypeErrors.NO_CREATE_GPSMANAGER)
            msg_error += "ERROR - Acesso Invalido, el servicio no tiene creado un GPSManager.";
        else if(type == TypeErrors.BAD_DELEGATE)
            msg_error += "ERROR - La clase Activity debe implementar la interfaz GPSDelegate.";
        else if(type == TypeErrors.FATAL_ERROR)
            msg_error += "ERROR - No se ha podido lanzar el servicio de GPS/Couch, si el problema persiste reinstale la aplicacion.";
        else if(type == TypeErrors.BAD_DELEGATE_LOCATION)
            msg_error += "ERROR - La clase Activity debe implementar la interfaz GPSLocation.";
        else
            msg_error += super.getMessage();

        return msg_error;
    }
}

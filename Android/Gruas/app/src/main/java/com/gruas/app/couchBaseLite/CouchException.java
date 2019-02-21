package com.gruas.app.couchBaseLite;

import java.lang.reflect.Type;

public class CouchException extends Exception {
    public enum TypeErrors {
        NO_CREATE_MANAGER,
        NO_DEFINE_DELEGATE,
        BAD_DB_NAME,
        NO_GET_DATABASE,
        FAIL_CREATE_DOCUMENT,
        INVALID_ACCESS,
        NO_CREATE_REPLICATION,
        BAD_DELEGATE,
        NO_DELETE_DB,
        UNDEFINED
    }

    private TypeErrors type;
    private String superior_msg;
    public static final String TAG = "CouchException";

    public CouchException(TypeErrors type, String msg){
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
        if(type == TypeErrors.NO_CREATE_MANAGER)
            msg_error += "ERROR - No se ha podido crear el Manager de CouchDB Lite.";
        else if(type == TypeErrors.NO_DEFINE_DELEGATE)
            msg_error += "ERROR - No ha definido un delegado para CouchManager.";
        else if(type == TypeErrors.BAD_DB_NAME)
            msg_error += "ERROR - El nombre de la base de datos no es válido.";
        else if(type == TypeErrors.NO_GET_DATABASE)
            msg_error += "ERROR - No se puede recuperar la base de datos.";
        else if(type == TypeErrors.FAIL_CREATE_DOCUMENT)
            msg_error += "ERROR - Hubo problemas al crear el documento.";
        else if(type == TypeErrors.INVALID_ACCESS)
            msg_error += "ERROR - Acceso Invalido, arranque couch.";
        else if(type == TypeErrors.NO_CREATE_REPLICATION)
            msg_error += "ERROR - No se pudo establecer la replicacion, URL invalida.";
        else if(type == TypeErrors.BAD_DELEGATE)
            msg_error += "ERROR - El Activity debe implementar la interfaz CouchDelegate.";
        else if(type == TypeErrors.NO_DELETE_DB)
            msg_error += "ERROR - Hubo problemas para borrar la base de datos.";
        else
            msg_error += super.getMessage();

        return msg_error;
    }
}

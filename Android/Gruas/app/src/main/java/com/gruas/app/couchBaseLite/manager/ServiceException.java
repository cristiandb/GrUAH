package com.gruas.app.couchBaseLite.manager;

public class ServiceException extends Exception {
    public enum TypeErrors {
        EXISTS_SERVICE_COURSE,
    }

    private TypeErrors type;
    public static final String TAG = "ServiceException";

    public ServiceException(TypeErrors type){
        super();
        this.type = type;
    }

    public TypeErrors getType(){
        return type;
    }

    @Override
    public String getMessage() {
        String msg_error = "";
        if(type == TypeErrors.EXISTS_SERVICE_COURSE)
            msg_error = "ERROR - Existe un servicio en curso.";

        return msg_error;
    }
}

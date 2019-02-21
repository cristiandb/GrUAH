package com.gruas.app.couchBaseLite.manager;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.CouchManager;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.services.gps.GPSObject;
import java.util.HashMap;
import java.util.Map;

public class ManagerDocumentService {
    private Document parentDoc = null; //Sirve para modificar el documento seleccionado en este momento
    private boolean service_on_course = false; //Variable de control para el estado en curso
    private boolean permitir_cambio_estado = false;
    private String idDoc;
    private String statePrincipal;
    private Service.StateService ss;
    private CouchManager couch;
    private ServiceDelegate serviceDelegate; //Recibe notificaciones relacionadas con los estados

    public ManagerDocumentService(CouchManager couch){
        this.couch = couch;
    }

    protected void addStateToHistory(GPSObject gpso) throws CouchException,CouchbaseLiteException{
        if(permitir_cambio_estado){
            if(gpso == null){
                if(serviceDelegate != null)
                    serviceDelegate.notifyNoAccessGPSLocation(); //El GPS esta deshabilitado
            } else if(idDoc != null){
                if(parentDoc == null)
                    this.parentDoc = couch.getDocument(idDoc);

                Map<String,Object> propertys = parentDoc.getProperties();
                Map<String,Object> currentPropertys = new HashMap();
                currentPropertys.putAll(propertys);
                Map<String,Object> history  = (Map<String,Object>)propertys.get("historial");
                history.put(Integer.toString(history.size() + 1),createState(gpso));
                currentPropertys.put("historial",history);
                currentPropertys.put("Estado",statePrincipal);
                parentDoc.putProperties(currentPropertys);

                if(serviceDelegate != null)
                    serviceDelegate.changeState(ss);
            }
        } else {
            if(serviceDelegate != null)
                serviceDelegate.nochangeStateEnCurso(); //Informamos al delegado del bloqueo
        }
    }

    protected void selectDocument(String idDoc,Service.StateService estado) throws CouchException{
        permitir_cambio_estado = false;
        String estadoCambiar = estado.getDescription();
        if(estadoCambiar.equals(Service.StateService.CURSO.getDescription())){
            if(!service_on_course){
                permitir_cambio_estado = true;
                service_on_course = true;
            }
        } else
            permitir_cambio_estado = true;

        if(permitir_cambio_estado){
            this.idDoc = idDoc;
            this.statePrincipal = estado.getDescription();
            this.parentDoc = couch.getDocument(idDoc);
            this.ss = estado;

            String estadoAnterior = parentDoc.getProperty("Estado").toString();
            if(estadoAnterior.equals(Service.StateService.CURSO.getDescription()))
                service_on_course = false; //Cambio del estado en curso a otro
        }
    }

    protected void anyoneOnCourse(boolean curso){
        this.service_on_course = curso;
    }

    protected void setServiceDelegate(ServiceDelegate sd){
        this.serviceDelegate = sd;
    }

    private Map<String,Object> createState(GPSObject gpso){
        Map<String,Object> state = new HashMap();
        state.put("estado",statePrincipal);
        state.put("latitud", gpso.getLocation().getLatitude());
        state.put("longitud", gpso.getLocation().getLongitude());
        state.put("fecha", gpso.getLongDate());
        return state;
    }
}

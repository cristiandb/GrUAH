package com.gruas.app.couchBaseLite.manager;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.CouchManager;
import com.gruas.app.services.gps.GPSObject;
import java.util.HashMap;
import java.util.Map;

public class ManagerDocumentLocation {
    private Document doc = null;
    private String id_empleado;
    private CouchManager couch;
    private Map<String, Object> docContent = new HashMap();

    public ManagerDocumentLocation(CouchManager couch, String id_empleado){
        this.id_empleado = id_empleado;
        this.couch = couch;
    }

    protected void nuevaLocalizacion(GPSObject gpso) throws CouchException,CouchbaseLiteException{
        docContent.clear();
        if(couch.getNumDocuments()== 0)
            doc = couch.addDocument(docContent,null);
        else {
            if(doc == null)
                doc = couch.getFirstDocumentForField("type", "location");

            //En la base de datos hay servicios, pero no se ha creado una localizacion
            if(doc == null)
                doc = couch.addDocument(docContent,null);
        }

        crearNuevaLocalizacion(gpso);
    }

    private void addPropertysGPS(GPSObject gpso){
        docContent.put("latitud", gpso.getLocation().getLatitude());
        docContent.put("longitud", gpso.getLocation().getLongitude());
        docContent.put("type","location");
        docContent.put("id_empleado",id_empleado);
        docContent.put("fecha", gpso.getLongDate());
    }

    private void crearNuevaLocalizacion(GPSObject gpso) throws CouchbaseLiteException{
        //Modificamos la localización
        Map<String, Object> curProperties = doc.getProperties();
        docContent.putAll(curProperties);
        addPropertysGPS(gpso);
        doc.putProperties(docContent);
    }
}

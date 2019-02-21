package com.gruas.app.couchBaseLite.replications;

import android.app.*;
import android.app.Service;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.replicator.Replication.ChangeListener;
import com.couchbase.lite.replicator.Replication.ChangeEvent;
import com.gruas.app.couchBaseLite.*;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.notificaciones.Notificacion;
import com.gruas.app.services.gps.GPSDelegate;
import com.gruas.app.services.gps.GPSException;
import com.gruas.app.services.gps.GPSLocation;
import com.gruas.app.services.gps.GPSObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceChangeListener implements ChangeListener {
    private Activity delegate;
    private CouchManager couch;
    private int countNewDocuments = 0;
    private Notificacion notify;

    public ServiceChangeListener(Activity delegate,CouchManager couch){
        super();
        this.delegate = delegate;
        this.couch = couch;
        this.notify = new Notificacion(1, "¡Servicio requerido!", "¡Nuevo servicio requerido!", "Nº de servicios en espera", delegate);
    }

    public void setDelegate(Activity delegate){
        this.delegate = delegate;
    }

    @Override
    public void changed(ChangeEvent changeEvent) {
        try {
            int processed = changeEvent.getSource().getCompletedChangesCount();
            int total = changeEvent.getSource().getChangesCount();

            if(processed == total){
                QueryEnumerator lista = couch.getDocuments();
                int size = lista.getCount();
                Document newDoc = null;

                while(lista.hasNext()){
                    Document doc = lista.next().getDocument();
                    String type = (String) doc.getProperty("type");
                    if(type.equals("servicio")){
                        String estado = (String) doc.getProperty("Estado");
                        if(estado.equals(com.gruas.app.couchBaseLite.Service.StateService.ESPERA.getDescription())){
                            //Creamos el historial con la primera entrada y enviamos una notificacion de servicio
                            if(doc.getProperty("historial") == null){
                                crearHistorial(doc);
                                newDoc = doc;
                                countNewDocuments++;
                            }
                        }
                    }
                }

                if(countNewDocuments > 0){
                    if(countNewDocuments == 1) enviarNotificacion(newDoc);
                    else enviarNotificacion(countNewDocuments);
                }

                countNewDocuments = 0;
            }
        } catch (CouchException e) {
            Log.d(CouchException.TAG,e.getMessage());
        }
    }

    private void enviarNotificacion(int countNewDocuments){
        notify.displayNotification(countNewDocuments);
    }

    private void enviarNotificacion(Document doc){
        DocumentsAdapter da = ((CouchDelegate) delegate).getDocumentsAdapter();
        da.setAdaptador(DocumentsAdapter.TypeAdapter.SERVICIOS);
        com.gruas.app.couchBaseLite.Service s = (com.gruas.app.couchBaseLite.Service)da.transformDocument(doc,true);
        notify.displayNotification(s);
    }

    private void crearHistorial(Document parentDoc){
        Map<String,Object> propertys = parentDoc.getProperties();
        Map<String,Object> currentPropertys = new HashMap();
        currentPropertys.putAll(propertys);
        Map<String,Object> history = new HashMap();
        history.put("1",createState(((GPSLocation)delegate).getLastLocation()));
        currentPropertys.put("historial",history);

        try {
            parentDoc.putProperties(currentPropertys);
        } catch (CouchbaseLiteException e) {
            Log.d(CouchException.TAG, e.getMessage());
        }
    }

    private Map<String,Object> createState(GPSObject gpso){
        Map<String,Object> state = new HashMap();
        state.put("estado", com.gruas.app.couchBaseLite.Service.StateService.ESPERA.getDescription());
        state.put("latitud", gpso.getLocation().getLatitude());
        state.put("longitud", gpso.getLocation().getLongitude());
        state.put("fecha", gpso.getLongDate());
        return state;
    }
}

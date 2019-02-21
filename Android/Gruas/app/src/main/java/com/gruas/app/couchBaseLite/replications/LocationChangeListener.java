package com.gruas.app.couchBaseLite.replications;

import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.CouchManager;
import com.gruas.app.services.gps.NoLocation;

import java.util.Map;

public class LocationChangeListener implements Replication.ChangeListener {
    private NoLocation delegate;
    private CouchManager couch;
    private boolean launched_delegate = false;

    public LocationChangeListener(NoLocation nl, CouchManager couch){
        super();
        this.delegate = nl;
        this.couch = couch;
    }

    @Override
    public void changed(Replication.ChangeEvent changeEvent) {
        int processed = changeEvent.getSource().getCompletedChangesCount();
        int total = changeEvent.getSource().getChangesCount();

        if(processed == total){
            try {
                Document doc = couch.getFirstDocumentForField("type", "location");

                if(doc != null){
                    double latitud = (Double) doc.getProperty("latitud");
                    double longitud = (Double) doc.getProperty("longitud");

                    if(latitud == 0 && longitud == 0){
                        if(!launched_delegate){
                            launched_delegate = true;
                            //Lanzamos un hilo para llamar al delegado, ya que si no, no podemos finalizar el metodo changed
                            new Thread(new Runnable() {
                                public void run() {
                                    delegate.locationZero();
                                }
                            }).start();
                        }
                    }
                }
            } catch (CouchException e) {
            }
        }
    }
}

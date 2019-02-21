package com.gruas.app.couchBaseLite.replications;

import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;

import java.util.Map;

public class FilterPreferences implements ReplicationFilter {
    @Override
    public boolean filter(SavedRevision savedRevision, Map<String, Object> stringObjectMap) {
        String tipo = (String) savedRevision.getDocument().getProperty("type");
        boolean permitido = true;

        if(tipo != null){
            if(tipo.equals("preferences"))
                permitido = false;
        }

        return permitido;
    }
}

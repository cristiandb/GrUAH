package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Document;
import com.couchbase.lite.Reducer;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReducerPositionsClients implements Reducer {
    @Override
    public Object reduce(List<Object> objects, List<Object> objects2, boolean b) {
        double latitud, longitud;
        List<LatLng> localizaciones = new ArrayList<LatLng>();
        Iterator i = objects2.iterator();

        while(i.hasNext()){
            Map<String,Object> service = (LinkedHashMap)i.next();
            Map<String,Object> locationClient = (Map<String,Object>)service.get("location");
            latitud = (Double)locationClient.get("lat");
            longitud = (Double)locationClient.get("long");
            localizaciones.add(new LatLng(latitud,longitud));
        }

        return localizaciones;
    }
}

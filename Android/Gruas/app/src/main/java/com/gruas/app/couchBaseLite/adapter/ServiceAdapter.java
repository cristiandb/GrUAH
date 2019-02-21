package com.gruas.app.couchBaseLite.adapter;

import com.couchbase.lite.Document;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.State;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceAdapter implements Adapter {
    @Override
    public boolean isDocumentValid(Document doc, String key, String compare) {
        boolean filtrarBusqueda = (key != null && compare != null);
        boolean isValid = false;

        if(doc.getProperty("type").equals("servicio")){
            if(filtrarBusqueda){
                if(doc.getProperty(key).equals(compare)) isValid = true;
            } else isValid = true;
        }

        return isValid;
    }

    @Override
    public Object transformDocument(Document doc) {
        Service service = null;

        if(doc != null){
            service = createService(doc);
            createHistorial(service,doc);
        }

        return service;
    }

    private Service createService(Document doc){
        String nIncidencia = (String)doc.getProperty("NumIncidencia");
        String nombreCliente = (String)doc.getProperty("NombreCliente");
        String direccion = (String)doc.getProperty("Direccion");
        String observaciones = (String)doc.getProperty("Observaciones");
        String telefono = (String)doc.getProperty("Telefono");
        String matricula = (String)doc.getProperty("Matricula");
        String modelo = (String)doc.getProperty("Modelo");
        String color = (String)doc.getProperty("Color");
        long fecha = (Long)doc.getProperty("Fecha");
        String estado = (String)doc.getProperty("Estado");

        Map<String,Object> location = (LinkedHashMap) doc.getProperty("location");
        double latitudC = ((Double)location.get("lat")).doubleValue();
        double longC = ((Double)location.get("long")).doubleValue();
        return new Service(nIncidencia,nombreCliente,direccion,observaciones,telefono,matricula,modelo,color,fecha,estado,latitudC, longC, doc.getId());
    }

    private void createHistorial(Service service,Document doc){
        String estadoE;
        Date fechaE;
        double latitud, longitud;
        Iterator i = ((Map<String,Object>)doc.getProperty("historial")).values().iterator();

        while(i.hasNext()){
            Map<String,Object> estadoPropertys = (Map)i.next();
            fechaE = new Date();
            fechaE.setTime(((Long) estadoPropertys.get("fecha")).longValue());
            estadoE = (String)estadoPropertys.get("estado");
            latitud = ((Double) estadoPropertys.get("latitud")).doubleValue();
            longitud = ((Double) estadoPropertys.get("longitud")).doubleValue();
            service.addState(new State(fechaE,estadoE,latitud,longitud));
        }
    }
}

package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Emitter;
import com.gruas.app.couchBaseLite.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapperServiceHistorial extends MapperService {
    public void emitterService(Map<String, Object> document, Emitter emitter) {
        Object estado = document.get("Estado");
        if(estado.equals(Service.StateService.FINALIZADO.getDescription()) || estado.equals(Service.StateService.RECHAZADO.getDescription())){
            //La clave está formada por la fecha y el estado
            List<Object> compoundKey = new ArrayList<Object>();
            compoundKey.add(document.get("Estado"));
            compoundKey.add(document.get("Fecha"));
            emitter.emit(compoundKey,document);
        }
    }
}

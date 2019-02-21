package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Emitter;
import com.gruas.app.couchBaseLite.Service;

import java.util.Map;

public class MapperServiceEsperandoAprobacion extends MapperService {
    @Override
    public void emitterService(Map<String, Object> document, Emitter emitter) {
        if(document.get("Estado").equals(Service.StateService.ESPERA.getDescription()))
            emitter.emit(document.get("Fecha"),document);
    }
}

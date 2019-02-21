package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Emitter;
import com.gruas.app.couchBaseLite.Service;

import java.util.Map;

public class MapperServiceEnCurso extends MapperService {
    @Override
    public void emitterService(Map<String, Object> document, Emitter emitter) {
        if(document.get("Estado").equals(Service.StateService.CURSO.getDescription()))
            emitter.emit(document.get("Fecha"),document);
    }
}

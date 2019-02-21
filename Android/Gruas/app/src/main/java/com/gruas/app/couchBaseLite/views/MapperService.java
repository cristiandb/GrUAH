package com.gruas.app.couchBaseLite.views;


import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;

import java.util.Map;

public abstract class MapperService implements Mapper {
    @Override
    public void map(Map<String, Object> document, Emitter emitter) {
        String type = (String) document.get("type");
        if (type != null) {
            if(type.equals("servicio")) {
                emitterService(document,emitter);
            }
        }
    }

    public abstract void emitterService(Map<String, Object> stringObjectMap, Emitter emitter);
}

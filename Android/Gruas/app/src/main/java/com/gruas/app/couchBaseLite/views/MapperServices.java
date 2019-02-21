package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Emitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapperServices extends MapperService{
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void emitterService(Map<String, Object> document, Emitter emitter) {
        List<Object> compoundKey = new ArrayList<Object>();
        compoundKey.add(document.get("Estado"));
        compoundKey.add(document.get("Fecha"));
        emitter.emit(compoundKey,document);
    }
}

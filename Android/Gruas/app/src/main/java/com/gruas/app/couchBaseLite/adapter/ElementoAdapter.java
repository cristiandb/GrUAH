package com.gruas.app.couchBaseLite.adapter;

import com.couchbase.lite.Document;
import com.gruas.app.lista.Elemento;

public class ElementoAdapter implements Adapter {
    @Override
    public boolean isDocumentValid(Document doc, String key, String compare) {
        boolean filtrarBusqueda = (key != null && compare != null) && (key != "") ;
        boolean isValid = false;

        if(doc.getProperty("type").equals("servicio")){
            if(filtrarBusqueda){
                if(!key.isEmpty())
                    if(doc.getProperty(key).equals(compare)) isValid = true;
            } else isValid = true;
        }

        return isValid;
    }

    @Override
    public Object transformDocument(Document doc) {
        Elemento elem = null;
        if(doc != null) elem = new Elemento((Long) doc.getProperty("Fecha"),(String) doc.getProperty("Estado"), doc.getId());
        return elem;
    }
}

package com.gruas.app.couchBaseLite.adapter;

import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.gruas.app.GruasDelegate;
import com.gruas.app.couchBaseLite.CouchDelegate;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.CouchManager;
import com.gruas.app.fragments.FiltroLista;

import java.util.ArrayList;
import java.util.List;

public class DocumentsAdapter {
    public enum TypeAdapter {
        ELEMENTOS("ElementoAdapter"),
        SERVICIOS("ServiceAdapter");

        private String className = "";
        TypeAdapter(String cn){
            className = cn;
        }

        public String getClassName(){ return className;}
    }

    private CouchManager couch;
    private Adapter adaptador;
    private CouchDelegate delegate;
    private AdapterObject obj_cache = null;

    public DocumentsAdapter(CouchManager couch, CouchDelegate delegate){
        this.couch = couch;
        this.delegate = delegate;
        createAdapter(TypeAdapter.ELEMENTOS);
    }

    public void setDelegate(CouchDelegate delegate){
        this.delegate = delegate;
    }

    public void setAdaptador(TypeAdapter type){
        if(!adaptador.getClass().getSimpleName().equals(type.getClassName())) createAdapter(type);
    }

    public ArrayList<Object> transformDocuments(String key,String compare){
        QueryEnumerator listaDocs = null;
        ArrayList<Object> lista = new ArrayList();

        try {
            listaDocs = couch.getDocuments();
            while(listaDocs.hasNext()){
                Document doc = listaDocs.next().getDocument();
                if(adaptador.isDocumentValid(doc,key,compare))
                    lista.add(adaptador.transformDocument(doc));
            }
        } catch (CouchException e) {
            delegate.handlerErrorCouch(e.getType(),e.getMessage());
        }

        return lista;
    }

    /**
     * Devuelve una lista de Objetos con los criterios establecidso
     * @param key Campo que debe tener el document
     * @param listcompare Lista de valores que debe tener al menos el document con la clave indicada
     * @return lista de documents que cumplen con el criterio
     */
    public ArrayList<Object> transformDocuments(String key,ArrayList<String> listcompare){
        QueryEnumerator listaDocs;
        ArrayList<Object> lista = new ArrayList();

        if(listcompare != null){
            try {
                int index = 0;
                listaDocs = couch.getDocuments();
                boolean isValid = false;

                while(listaDocs.hasNext()){
                    Document doc = listaDocs.next().getDocument();

                    if(listcompare.size() == 0) isValid = adaptador.isDocumentValid(doc,key,"");
                    else {
                        while(index != listcompare.size() && !isValid){
                            isValid = (isValid || adaptador.isDocumentValid(doc,key,listcompare.get(index)));
                            index++;
                        }
                    }

                    if(isValid) lista.add(adaptador.transformDocument(doc));
                    index = 0;
                    isValid = false;
                }
            } catch (CouchException e) {
                delegate.handlerErrorCouch(e.getType(),e.getMessage());
            }
        }

        return lista;
    }

    public ArrayList<Object> transformDocuments(FiltroLista filtro){
        ArrayList<Object> lista = new ArrayList();
        QueryEnumerator listaDocs = null;

        try {
            listaDocs = couch.executeView(filtro.getNameView());
            while(listaDocs.hasNext()){
                Document doc = listaDocs.next().getDocument();
                if(adaptador.isDocumentValid(doc,null,null))
                    lista.add(adaptador.transformDocument(doc));
            }
        } catch (CouchException e) {
            delegate.handlerErrorCouch(e.getType(),e.getMessage());
        }

        return lista;
    }

    public Object transformDocument(String idDoc,boolean use_cache){
        Object obj = null;

        try {
            Document doc = couch.getDocument(idDoc);
            obj = adaptador.transformDocument(doc);
            if(use_cache) obj_cache = (AdapterObject)obj;
        } catch (CouchException e) {
            delegate.handlerErrorCouch(e.getType(),e.getMessage());
        } catch(NullPointerException npe){
            delegate.handlerErrorCouch(CouchException.TypeErrors.UNDEFINED, npe.getMessage());
        }

        return obj;
    }

    public Object transformDocument(Document doc,boolean use_cache){
        Object obj = null;

        try {
            obj = adaptador.transformDocument(doc);
            if(use_cache) obj_cache = (AdapterObject)obj;
        } catch(NullPointerException npe){
            delegate.handlerErrorCouch(CouchException.TypeErrors.UNDEFINED,npe.getMessage());
        }

        return obj;
    }

    /** Devuelve el último objeto al que se accedió con el adaptador correspondiente
     * @return Objeto guardado temporalmente, despues es destruida */
    public AdapterObject getObjectCache(){
        return obj_cache;
    }

    public void clearCache(){
        obj_cache = null;
    }

    private void createAdapter(TypeAdapter type){
        switch(type) {
            case ELEMENTOS:
                this.adaptador = new ElementoAdapter();
                break;
            case SERVICIOS:
                this.adaptador = new ServiceAdapter();
                break;
            default:
                this.adaptador = new ElementoAdapter();
        }
    }
}

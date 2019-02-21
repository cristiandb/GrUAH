package com.gruas.app.couchBaseLite;

import android.app.Activity;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouchManager {
    private Manager manager = null;
    private Database database = null;

    public void startCouch(Activity manager, String dbname) throws CouchException {
        if(manager == null) throw new CouchException(CouchException.TypeErrors.NO_DEFINE_DELEGATE,null);

        try {
            if(database == null) {
                createManager(manager);
                getDatabase(dbname);
            }
        } catch (IOException io){
            throw new CouchException(CouchException.TypeErrors.NO_CREATE_MANAGER, io.getMessage());
        }
    }

    public Document addDocument(Map<String,Object> docContent,Document docParent) throws CouchException{
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        Document docCreate = docParent;
        try {
            if(docCreate == null)
                docCreate = database.createDocument(); // Creamos un documento vacio, el cual rellenaremos con el map

            docCreate.putProperties(docContent); // Damos de alta el nuevo documento
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.FAIL_CREATE_DOCUMENT, e.getMessage());
        }

        return docCreate;
    }

    public void createFilter(String nameFilter, ReplicationFilter filter) throws CouchException{
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        database.setFilter(nameFilter,filter);
    }

    public Replication createReplication(String url, boolean typeReplication) throws CouchException{
        URL syncUrl;
        try {
            syncUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new CouchException(CouchException.TypeErrors.NO_CREATE_REPLICATION,e.getMessage());
        }

        Replication r = null;
        if(typeReplication) r = database.createPushReplication(syncUrl);
        else r = database.createPullReplication(syncUrl);

        return r;
    }

    public void stop(){
        if(database != null) database.close();
        if(manager != null) manager.close();

        database = null;
        manager = null;
    }

    public QueryEnumerator getDocuments() throws CouchException {
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        Query query = database.createAllDocumentsQuery();
        QueryEnumerator lista = null;

        try {
            lista = query.run();
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.UNDEFINED, e.getMessage());
        }

        return lista;
    }

    public Document getDocument(String docID) throws CouchException{
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        return database.getDocument(docID);
    }

    public int getNumDocuments() throws CouchException {
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        return database.getDocumentCount();
    }

    public Document getFirstDocumentForField(String key,Object compare) throws CouchException {
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        Document first = null;
        QueryEnumerator lista = getDocuments();
        boolean encontrado = false;
        while(lista.hasNext() && !encontrado){
            Document doc = lista.next().getDocument();
            if(doc.getProperty(key).equals(compare)){
                first = doc;
                encontrado = true;
            }
        }
        return first;
    }

    public void createView(String nameView, Mapper mapper) throws CouchException{
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        View view = database.getView(nameView);
        view.setMap(mapper,"1.0");
    }

    public QueryEnumerator executeView(String nameView, Mapper mapper, Reducer reducer) throws CouchException {
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        QueryEnumerator lista = null;
        View view = database.getView(nameView);
        view.setMapReduce(mapper,reducer,"1.0");
        Query query = view.createQuery();
        query.setDescending(true);

        try {
            lista = query.run();
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.UNDEFINED, e.getMessage());
        }

        return lista;
    }

    public QueryEnumerator executeView(String view_name) throws CouchException {
        if(database == null)
            throw new CouchException(CouchException.TypeErrors.INVALID_ACCESS, null);

        QueryEnumerator lista = null;
        View view = database.getView(view_name);
        Query query = view.createQuery();
        query.setDescending(true);

        try {
            lista = query.run();
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.UNDEFINED, e.getMessage());
        }

        return lista;
    }

    /* ----------------------------------------------------------------------------------------------*/
    /* Métodos Privados*/
    /* ----------------------------------------------------------------------------------------------*/

    private void createManager(Activity delegate) throws IOException {
        manager = new Manager(delegate.getApplicationContext().getFilesDir(), Manager.DEFAULT_OPTIONS);
    }

    private void getDatabase(String dbname) throws CouchException {
        if(!validNameDatabase(dbname)) throw new CouchException(CouchException.TypeErrors.NO_CREATE_MANAGER,null);

        try {
            database = manager.getDatabase(dbname);
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.NO_GET_DATABASE,e.getMessage());
        }
    }

    private boolean validNameDatabase(String dbname){
        if (!Manager.isValidDatabaseName(dbname)) return false;
        return true;
    }

    public void borrarBD() throws  CouchException{
        try {
            if(database != null) {
                database.delete();
                database = null;
            }
        } catch (CouchbaseLiteException e) {
            throw new CouchException(CouchException.TypeErrors.NO_DELETE_DB,e.getMessage());
        }
    }
}

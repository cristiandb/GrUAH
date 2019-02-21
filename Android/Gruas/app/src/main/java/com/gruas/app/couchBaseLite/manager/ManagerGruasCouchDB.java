package com.gruas.app.couchBaseLite.manager;

import android.app.Activity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.replicator.Replication;
import com.google.android.gms.maps.model.LatLng;
import com.gruas.app.R;
import com.gruas.app.couchBaseLite.CouchDelegate;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.CouchManager;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.replications.FilterPreferences;
import com.gruas.app.couchBaseLite.replications.LocationChangeListener;
import com.gruas.app.couchBaseLite.replications.ServiceChangeListener;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.couchBaseLite.views.MapperServiceEnCurso;
import com.gruas.app.couchBaseLite.views.MapperServiceEsperandoAprobacion;
import com.gruas.app.couchBaseLite.views.MapperServiceHistorial;
import com.gruas.app.couchBaseLite.views.MapperServicePendientes;
import com.gruas.app.couchBaseLite.views.MapperServices;
import com.gruas.app.couchBaseLite.views.ReducerPositionsClients;
import com.gruas.app.couchBaseLite.views.ReducerStatistic;
import com.gruas.app.couchBaseLite.views.reducerFilters.Parameters;
import com.gruas.app.services.gps.GPSException;
import com.gruas.app.services.gps.GPSLocation;
import com.gruas.app.services.gps.GPSObject;
import com.gruas.app.services.gps.NoLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerGruasCouchDB {
    private CouchManager couch;
    private Activity delegate; //Recibe posibles notificaciones de errores con couch
    private NoLocation delegateNL; //Se informa de cuando se ha replicado una localizacion de lat y long 0
    private boolean started_couch = false;
    private boolean created_replications = false;
    private String id_empleado;
    private ManagerDocumentLocation mdl;
    private ManagerDocumentService mds;
    private ServiceChangeListener scl;
    private Replication pushReplication;
    private Replication pullReplication;
    private Document preferenciasDoc;
    private MapperServices vistaServicios;
    private ReducerStatistic reducerStatistic;

    /* CONSTANTES */
    private static boolean INICIALIZATE_VARS = false;
    private static String DB_NAME;
    private static String URL_REPLICATION; //10.0.2.2 para simuladores android excepto genymotion (para el 10.0.3.2)
    private static String FILTRO_PULLER;
    private static String FILTRO_PUSHER;
    public static String VIEW_SERVICE_FOR_LISTA_CURSO;
    public static String VIEW_SERVICE_FOR_LISTA_PENDIENTE;
    public static String VIEW_SERVICE_FOR_LISTA_ESPERANDO_APROBACION;
    public static String VIEW_SERVICE_FOR_LISTA_HISTORIAL;
    public static String VIEW_SERVICE_FOR_ALL_SERVICES;

    public ManagerGruasCouchDB(Activity activity,NoLocation delegateNL, String idEmpleado) throws GPSException,CouchException{
        setDelegate(activity);
        this.delegateNL = delegateNL;
        this.id_empleado = idEmpleado;
        this.couch = new CouchManager();
        this.mdl = new ManagerDocumentLocation(couch,idEmpleado);
        this.mds = new ManagerDocumentService(couch);
    }

    public void setDelegate(Activity activity) throws GPSException,CouchException{
        if(!(activity instanceof GPSLocation)) throw new GPSException(GPSException.TypeErrors.BAD_DELEGATE_LOCATION,null);
        else if(!(activity instanceof CouchDelegate)) throw new CouchException(CouchException.TypeErrors.BAD_DELEGATE,null);
        this.delegate = activity;
        if(scl != null) scl.setDelegate(activity);
    }

    public void setServiceDelegate(ServiceDelegate sd){
        mds.setServiceDelegate(sd);
    }

    public boolean isCreated_replications(){ return this.created_replications; }

    public void startCouch() throws CouchException{
        if(!started_couch) {
            couch.startCouch(delegate, DB_NAME);
            crearVistas();
            crearFiltros();
            recuperarPreferencias();
            mds.anyoneOnCourse(couch.executeView(VIEW_SERVICE_FOR_LISTA_CURSO).getCount() != 0);
            started_couch = true;
        }
    }

    public void stopCouch(){
        stopReplications();
        stopCouchDatabase();
    }

    private void recuperarPreferencias() throws CouchException {
        preferenciasDoc = couch.getFirstDocumentForField("type", "preferences");

        if(preferenciasDoc == null){
            //No está vinculada la cuenta, la vinculamos
            Map<String, Object> docContent = new HashMap();
            docContent.put("type","preferences");
            docContent.put("idUsuario",id_empleado);
            preferenciasDoc = couch.addDocument(docContent,null);
        }
    }

    public void stopReplications(){
        if(created_replications) {
            if(pushReplication != null)pushReplication.stop();//Antes se debe enviar un location con todo 0
            if(pullReplication != null)pullReplication.stop();
            created_replications = false;
        }
    }

    public void stopCouchDatabase(){
        if(started_couch){
            couch.stop();
            started_couch = false;
        }
    }

    public void startReplications(){
        if(started_couch && !created_replications){
            try {
                startPullReplication();
                startPushReplication();
                created_replications = true;
            } catch (CouchException e) {
                //Manejar con el delegate el error
                ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
            }
        }
    }

    public void startOnlyPusherReplication(){
        if(started_couch) {
            try {
                startPushReplication();
                created_replications = true;
            } catch (CouchException e) {
                e.printStackTrace();
            }
        }
    }

    public DocumentsAdapter createDocumentsAdapter(){
        DocumentsAdapter adapter = null;
        if(started_couch) adapter = new DocumentsAdapter(couch,(CouchDelegate)delegate);
        return adapter;
    }

    public void nuevaLocalizacion(GPSObject gpso){
        try {
            mdl.nuevaLocalizacion(gpso);
        } catch (CouchException e) {
            //Manejar con el delegate el error
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
        } catch (CouchbaseLiteException e) {
            //Manejar con el delegate el error
            ((CouchDelegate)delegate).handlerErrorCouch(CouchException.TypeErrors.UNDEFINED,e.getMessage());
        }
    }

    public void addStateToHistory(String idDoc, Service.StateService state,GPSObject gpso){
        try {
            mds.selectDocument(idDoc,state);
            mds.addStateToHistory(gpso);
        } catch (CouchException e) {
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
        } catch (CouchbaseLiteException e) {
            ((CouchDelegate)delegate).handlerErrorCouch(CouchException.TypeErrors.UNDEFINED,e.getMessage());
        }
    }

    public Document getDocumentServiceEnCurso(){
        Document enCurso = null;

        try {
            QueryEnumerator lista = couch.executeView(VIEW_SERVICE_FOR_LISTA_CURSO);
            if(lista.getCount() != 0) enCurso = couch.executeView(VIEW_SERVICE_FOR_LISTA_CURSO).next().getDocument();
        } catch (CouchException e) {
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
        }

        return enCurso;
    }

    public Statistics getStatistics(Parameters parametros){
        Statistics statistics = null;

        try {
            if(vistaServicios == null) vistaServicios = new MapperServices();
            if(reducerStatistic == null) reducerStatistic = new ReducerStatistic();

            reducerStatistic.setFiltro(parametros);
            QueryEnumerator listaDocs = couch.executeView(VIEW_SERVICE_FOR_ALL_SERVICES, vistaServicios, reducerStatistic);

            if(listaDocs.getCount() == 0) statistics = new Statistics(0,0,0);
            else statistics = (Statistics)listaDocs.next().getValue();
        } catch (CouchException e) {
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
        }

        return statistics;
    }

    public List<LatLng> getPositionsClients(){
        List<LatLng> listaClientes = new ArrayList();

        try {
            QueryEnumerator listaDocs = couch.executeView(VIEW_SERVICE_FOR_ALL_SERVICES,new MapperServices(),new ReducerPositionsClients());
            if(listaDocs.getCount() != 0) listaClientes = (List)listaDocs.next().getValue();
        } catch (CouchException e) {
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(), e.getMessage());
        }

        return listaClientes;
    }

    public void clearDatabase(){
        try {
            couch.borrarBD();
        } catch (CouchException e) {
            //Manejar con el delegate el error
            ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
        }
    }

    //////////////// Metodos Estaticos ////////////////

    public static String accountLinked(Activity activity, String idUsuario) throws CouchException {
        if(!INICIALIZATE_VARS) inicializarVariablesGlobales(activity);

        String id = "";
        CouchManager couch = new CouchManager();
        couch.startCouch(activity,DB_NAME);
        Document doc = couch.getFirstDocumentForField("type", "preferences");

        if(doc != null)
            id = (String)doc.getProperty("idUsuario");

        couch.stop();
        return id;
    }

    public static void createLink(Activity activity, String idUsuario) throws CouchException{
        if(!INICIALIZATE_VARS) inicializarVariablesGlobales(activity);

        CouchManager couch = new CouchManager();

        //Vaciamos la base de datos
        couch.startCouch(activity,DB_NAME);
        couch.borrarBD();
        couch.stop();

        //Creamos preferencias
        couch.startCouch(activity,DB_NAME);
        Map<String, Object> docContent = new HashMap();
        docContent.put("type","preferences");
        docContent.put("idUsuario",idUsuario);
        couch.addDocument(docContent,null);
        couch.stop();
    }

    private static void inicializarVariablesGlobales(Activity activity){
        INICIALIZATE_VARS = true;
        DB_NAME = activity.getString(R.string.db_name);
        URL_REPLICATION = activity.getString(R.string.url_replication) + DB_NAME;
        FILTRO_PULLER = activity.getString(R.string.filtro_puller);
        FILTRO_PUSHER = activity.getString(R.string.filtro_pusher);
        VIEW_SERVICE_FOR_LISTA_CURSO = activity.getString(R.string.view_service_for_lista_curso);
        VIEW_SERVICE_FOR_LISTA_PENDIENTE = activity.getString(R.string.view_service_for_lista_pendiente);
        VIEW_SERVICE_FOR_LISTA_ESPERANDO_APROBACION = activity.getString(R.string.view_service_for_lista_esperando_aprobacion);
        VIEW_SERVICE_FOR_LISTA_HISTORIAL = activity.getString(R.string.view_service_for_lista_historial);
        VIEW_SERVICE_FOR_ALL_SERVICES = activity.getString(R.string.view_service_for_lista_all_services);
    }

    public static Parameters createParametersFilterStatistic(Object parameters){
        return ReducerStatistic.createParametersFilterStatistic(parameters);
    }

    //////////////// Metodos Privados ////////////////

    private void startPushReplication() throws CouchException {
        if(pushReplication == null){
            pushReplication = couch.createReplication(URL_REPLICATION, true);
            pushReplication.setContinuous(true);
            pushReplication.setFilter(FILTRO_PUSHER);
            pushReplication.addChangeListener(new LocationChangeListener(delegateNL,couch));
            pushReplication.start();
        }
    }

    private void startPullReplication() throws CouchException{
        if(pullReplication == null){
            //Creamos los parametros
            scl = new ServiceChangeListener(delegate,couch);
            Map<String,Object> filterP = new HashMap();
            filterP.put("id_empleado",id_empleado);

            pullReplication = couch.createReplication(URL_REPLICATION,false);
            pullReplication.setContinuous(true);
            pullReplication.setFilter(FILTRO_PULLER);
            pullReplication.setFilterParams(filterP);
            pullReplication.addChangeListener(scl);
            pullReplication.start();
        }
    }

    private void crearVistas() throws CouchException {
        couch.createView(VIEW_SERVICE_FOR_LISTA_CURSO, new MapperServiceEnCurso());
        couch.createView(VIEW_SERVICE_FOR_LISTA_PENDIENTE, new MapperServicePendientes());
        couch.createView(VIEW_SERVICE_FOR_LISTA_ESPERANDO_APROBACION, new MapperServiceEsperandoAprobacion());
        couch.createView(VIEW_SERVICE_FOR_LISTA_HISTORIAL, new MapperServiceHistorial());
        couch.createView(VIEW_SERVICE_FOR_ALL_SERVICES, new MapperServices());
    }

    private void crearFiltros() throws CouchException {
        couch.createFilter(FILTRO_PUSHER, new FilterPreferences());
    }
}

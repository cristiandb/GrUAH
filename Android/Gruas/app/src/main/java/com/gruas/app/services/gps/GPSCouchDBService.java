package com.gruas.app.services.gps;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import com.couchbase.lite.Document;
import com.gruas.app.couchBaseLite.Service;
import com.google.android.gms.maps.model.LatLng;
import com.gruas.app.GruasDelegate;
import com.gruas.app.couchBaseLite.CouchDelegate;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.adapter.AdapterObject;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.couchBaseLite.Service.StateService;
import com.gruas.app.couchBaseLite.manager.ManagerGruasCouchDB;
import com.gruas.app.couchBaseLite.manager.ServiceDelegate;
import com.gruas.app.couchBaseLite.manager.Statistics;
import com.gruas.app.couchBaseLite.views.ReducerStatistic;
import com.gruas.app.couchBaseLite.views.reducerFilters.Parameters;
import com.gruas.app.couchBaseLite.views.reducerFilters.RangeDate;
import com.gruas.app.notificaciones.Notificacion;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GPSCouchDBService extends android.app.Service implements NoLocation{
    public static final int UPDATE_INTERVAL = 5000; //En milisegundos
    private Timer timer = new Timer();
    private final IBinder binder = new GPSBinder();
    private GPSManager gps;
    private Activity delegate = null;
    private ManagerGruasCouchDB managerGruas;
    private boolean service_launched = false;
    private boolean service_stopping = false;
    private DocumentsAdapter da;
    private String idUsuario = "";
    private boolean desvincular_user = false;

    public void createGPSManager(LocationManager lm){
        if(gps == null) this.gps = new GPSManager(lm); //Solo se establece una vez
    }

    public void setDelegate(Activity delegate) throws GPSException{
        if(!(delegate instanceof GPSDelegate)) throw new GPSException(GPSException.TypeErrors.BAD_DELEGATE,null);
        this.delegate = delegate;
        gps.setDelegate(delegate);
    }

    public void launchGPS(String idUsuario) throws GPSException{
        if(gps == null)
            throw new GPSException(GPSException.TypeErrors.NO_CREATE_GPSMANAGER,null);
        else if(!service_launched){
            this.idUsuario = idUsuario;

            //Creamos el manejador de la base de datos gruas
            try {
                this.managerGruas = new ManagerGruasCouchDB(this.delegate,this,idUsuario);
            } catch (CouchException e) {
                //Manejar con el delegate el error
                ((CouchDelegate)delegate).handlerErrorCouch(e.getType(),e.getMessage());
            }

            try {
                //Arrancamos los servicios
                gps.arrancarServicio();
                managerGruas.startCouch();
                service_launched = true;
                da = managerGruas.createDocumentsAdapter();
                ((GPSDelegate)delegate).startedServiceCouch(da,gps.isGPSEnabled());

                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(gps.isNewLocation()){
                            managerGruas.startReplications();
                            managerGruas.nuevaLocalizacion(gps.getLastLocation());

                            delegate.runOnUiThread(new Runnable() {
                                // No se pueden lanzar desde un hilo que no sea el hilo principal, cambios en un view
                                @Override
                                public void run() {
                                    ((GPSDelegate)delegate).cambioLocalizacion(gps.getLastLocation());
                                }
                            });
                        }
                    }
                },0,UPDATE_INTERVAL);
            } catch (CouchException e) {
                ((GPSDelegate) delegate).handlerFatalError(GPSException.TypeErrors.FATAL_ERROR,e.getType(),e.getMessage());
            }
        }
    }

    public void stopServices(boolean desvinculated){
        if(service_launched){
            timer.cancel();
            desvincular_user = desvinculated;
            service_stopping = true;
            gps.detenerServicio();

            if(!managerGruas.isCreated_replications())
                managerGruas.startOnlyPusherReplication(); //El usuario es tan impaciente que ni ha esperado a obtener una localizacion con el gps

            //Debemos enviar una localizacion on longitud 0 y latitud 0 para los de mapas
            managerGruas.nuevaLocalizacion(GPSObject.createGPSObject(0.0, 0.0));
        }
    }

    public void checkNotification(Activity activity, String action) {
        if(action != null && service_launched){
            if(action.equals(Notificacion.ACTION_NOTIFY)) {
                //Practicamente es un nuevo activity, se debe resetear ciertos valores
                try {
                    setDelegate(activity);
                    managerGruas.setDelegate(activity);
                    da.setDelegate((CouchDelegate)activity);
                    ((GruasDelegate) delegate).notificationReceived(da, idUsuario);
                } catch (GPSException g) {
                    ((GPSDelegate) delegate).handlerFatalError(GPSException.TypeErrors.FATAL_ERROR,CouchException.TypeErrors.UNDEFINED,g.getMessage());
                } catch (CouchException e) {
                    ((CouchDelegate) delegate).handlerErrorCouch(e.getType(),e.getMessage());
                }
            }
        }
    }

    public void changeStatusService(String idDoc, StateService state){
        managerGruas.addStateToHistory(idDoc,state,gps.getLastLocation());
    }

    public Statistics obtenerEstadisticas(ReducerStatistic.DateFiltro filtro){
        Parameters params = ManagerGruasCouchDB.createParametersFilterStatistic(filtro);
        return managerGruas.getStatistics(params);
    }

    public Statistics obtenerEstadisticas(long dateS, long dateE){
        Parameters params = ManagerGruasCouchDB.createParametersFilterStatistic(new RangeDate(dateS,dateE));
        return managerGruas.getStatistics(params);
    }

    public List<LatLng> obtenerPosicionesClientes(){
        return managerGruas.getPositionsClients();
    }

    public Service obtenerServicioEnCurso(){
        if(da == null) return null;

        Service s = null;
        Document doc = managerGruas.getDocumentServiceEnCurso();
        if(doc != null){
            da.setAdaptador(DocumentsAdapter.TypeAdapter.SERVICIOS);
            s = (Service) da.transformDocument(doc,false);
        }

        return s;
    }

    public AdapterObject useCacheObjAdapter(){
        DocumentsAdapter da =  ((CouchDelegate)delegate).getDocumentsAdapter();
        if(da == null) return null;
        else return da.getObjectCache();
    }

    public void clearCacheAdapter(){
        DocumentsAdapter da =  ((CouchDelegate)delegate).getDocumentsAdapter();
        if(da != null) da.clearCache();
    }

    public void establishedServiceDelegate(ServiceDelegate sd){
        managerGruas.setServiceDelegate(sd);
    }

    public void notifyBackToMainActivity(){
        if(delegate != null)
            ((GruasDelegate)delegate).pressBackAnotherActivity();
    }

    public boolean isServiceLaunched(){
        return service_launched;
    }

    public GPSObject getLastLocation(){
        return this.gps.getLastLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void locationZero() {
        if(service_stopping){
            service_stopping = false;

            if(desvincular_user){
                managerGruas.stopReplications();
                managerGruas.clearDatabase();
                managerGruas.stopCouch();
            } else
                managerGruas.stopCouch();

            service_launched = false;
            gps = null;
            managerGruas = null;
            da = null;

            //Avisamos al activity de que se ha parado por completo el servicio. GPS y Couch inactivos
            if(delegate != null)
                ((GruasDelegate)delegate).stopedGPSCouchDBService();
        }
    }

    public class GPSBinder extends Binder {
        public GPSCouchDBService getService(){
            return GPSCouchDBService.this;
        }
    }
}

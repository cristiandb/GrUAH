package com.gruas.app.servicio;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.gruas.app.R;
import com.gruas.app.couchBaseLite.CouchException;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.State;
import com.gruas.app.couchBaseLite.adapter.AdapterObject;
import com.gruas.app.couchBaseLite.manager.ServiceDelegate;
import com.gruas.app.notificaciones.Notificacion;
import com.gruas.app.services.gps.GPSCouchDBService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class VistaServicio extends Activity implements ServiceDelegate {
    ArrayList<String> listaEstados = new ArrayList<String>();
    Service service;
    private ScrollView mScrollView;
    private GoogleMap mMap;
    private ClusterManager<ItemCluster> mClusterManager;
    private Spinner spinnerEstados=null;
    private boolean spinnerUso=false;
    private  VistaServicio vs=this;
    private Iterator historial;
    private boolean animacion=true;
    private Marker markerAnimacion;

    //Variables del GPS
    private GPSCouchDBService gpsservice;
    private ServiceConnection scgps = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gpsservice = ((GPSCouchDBService.GPSBinder) iBinder).getService();
            bindServiced();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gpsservice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_servicio);

        mMap = ((MapFragmentNavegable) getFragmentManager().findFragmentById(R.id.mapaServicio)).getMap();
        mScrollView = (ScrollView) findViewById(R.id.scrollVistaServicio);

        ((MapFragmentNavegable) getFragmentManager().findFragmentById(R.id.mapaServicio)).setListener(new MapFragmentNavegable.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        //Obtenemos el servicio pasado por Intent
        Intent i = getIntent();
        service = (Service)i.getSerializableExtra("Servicio");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        bindService(new Intent(this, GPSCouchDBService.class), scgps, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Volvemos hacia atras
        super.onBackPressed();
        gpsservice.notifyBackToMainActivity();
        unbindService(scgps);
        return true;
    }

    //Replicamos cambios
    private void establecerCambios(Service.StateService state){
        gpsservice.changeStatusService(service.getIdDoc(),state);
    }

    private void mostrarDatos(){
        ((TextView)findViewById(R.id.nIncidencia)).setText(service.getnIncidencia());
        ((TextView)findViewById(R.id.nombreCliente)).setText(service.getNombreCliente());
        ((TextView)findViewById(R.id.direccion)).setText(service.getDireccion());
        ((TextView)findViewById(R.id.observaciones)).setText(service.getObservaciones());
        ((TextView)findViewById(R.id.telefono)).setText(service.getTelefono());
        ((TextView)findViewById(R.id.matricula)).setText(service.getMatricula());
        ((TextView)findViewById(R.id.modelo)).setText(service.getModelo());
        ((TextView)findViewById(R.id.color)).setText(service.getColor());
        ((TextView)findViewById(R.id.fechaHora)).setText(service.getFecha());
        ((TextView)findViewById(R.id.estado)).setText(service.getEstado());
        clusterMapa();
    }
    private void mostrarDatosServicio (){
        (findViewById(R.id.botonAceptar)).setVisibility(View.GONE);
        (findViewById(R.id.botonRechazar)).setVisibility(View.GONE);
        (findViewById(R.id.botonIniciarServicio)).setVisibility(View.GONE);
        (findViewById(R.id.estado)).setVisibility(View.GONE);
        (findViewById(R.id.spinnerEstados)).setVisibility(View.VISIBLE);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaEstados);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spinnerEstados)).setAdapter(adaptador);

    }

    private void mostrarDatosPendientesIniciar(){
        (findViewById(R.id.estado)).setVisibility(View.VISIBLE);
        (findViewById(R.id.spinnerEstados)).setVisibility(View.GONE);
        (findViewById(R.id.botonAceptar)).setVisibility(View.GONE);
        (findViewById(R.id.botonRechazar)).setVisibility(View.VISIBLE);
        (findViewById(R.id.botonIniciarServicio)).setVisibility(View.VISIBLE);
    }
    private void mostrarDatosPendienteAceptar (){
        (findViewById(R.id.estado)).setVisibility(View.VISIBLE);
        (findViewById(R.id.spinnerEstados)).setVisibility(View.GONE);
        (findViewById(R.id.botonAceptar)).setVisibility(View.VISIBLE);
        (findViewById(R.id.botonRechazar)).setVisibility(View.VISIBLE);
        (findViewById(R.id.botonIniciarServicio)).setVisibility(View.GONE);
    }

    private void mostrarDatosHistorial (){
        (findViewById(R.id.botonAceptar)).setVisibility(View.GONE);
        (findViewById(R.id.botonRechazar)).setVisibility(View.GONE);
        (findViewById(R.id.spinnerEstados)).setVisibility(View.GONE);
        (findViewById(R.id.botonIniciarServicio)).setVisibility(View.GONE);
        (findViewById(R.id.estado)).setVisibility(View.VISIBLE);
    }

    private void cancelarNotificacion(){
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificactionID = 1;
        if(nm!=null) nm.cancel(notificactionID);
        gpsservice.clearCacheAdapter();
        Notificacion.resetParametersNotification();
    }

    private void bindServiced(){
        if(service == null){
            //Es una notificacion, por lo tanto esta almacenada temporalmente en el adaptador
            service = (Service) gpsservice.useCacheObjAdapter();
            cancelarNotificacion();
        } else {
            AdapterObject obj = gpsservice.useCacheObjAdapter();
            if(obj != null){
                if(service.getIdDoc().equals(obj.getIdDoc())){
                    //Si el elemento accedido es el mismo que el de una notificacion, se elimina la notificación
                    cancelarNotificacion();
                }
            }
        }

        gpsservice.establishedServiceDelegate(this);

        final Button aceptar = (Button) findViewById(R.id.botonAceptar);
        final Button rechazar = (Button) findViewById(R.id.botonRechazar);
        final Button iniciar = (Button) findViewById(R.id.botonIniciarServicio);
        spinnerEstados = (Spinner) findViewById(R.id.spinnerEstados);

        listaEstados.add(Service.StateService.CURSO.getDescription());
        listaEstados.add(Service.StateService.PAUSADO.getDescription());
        listaEstados.add(Service.StateService.FINALIZADO.getDescription());
        listaEstados.add(Service.StateService.RECHAZADO.getDescription());
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaEstados);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner) findViewById(R.id.spinnerEstados)).setAdapter(adaptador);

        // Mostrar los datos del servicio
        mostrarDatosServicio(service.getEstado());


        // ACCION BOTON ACEPTAR
        aceptar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                establecerCambios(Service.StateService.ACEPTADO);
            }
        });

        // ACCION BOTON RECHAZAR
        rechazar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                establecerCambios(Service.StateService.RECHAZADO);
            }
        });
        // ACCION BOTON INICIAR SERVICIO
        iniciar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                establecerCambios(Service.StateService.CURSO);
            }
        });

        //si el estado es pausado se actualiza el spinner
        if(service.getEstado().equals(Service.StateService.PAUSADO.getDescription())){
            spinnerEstados.setSelection(1);
        }

        spinnerEstados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean cargado=false;
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if ((spinnerEstados.getVisibility() == View.VISIBLE) && cargado){
                        String estado = arg0.getItemAtPosition(arg2).toString();
                        if (!estado.equals(service.getEstado())) {
                            spinnerUso=true;
                            establecerCambios(Service.getStateService(estado));
                            service.addState(new State(new Date(),service.getEstado(),gpsservice.getLastLocation().getLocation().getLatitude(),gpsservice.getLastLocation().getLocation().getLatitude()));
                        }
                }else{
                    cargado=true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(animacion){
                    animacion=false;
                    if(markerAnimacion!=null){
                        markerAnimacion.remove();
                    }
                    historial=service.getHistorial();
                    final LatLng posicion=((State)historial.next()).getLocation();
                     markerAnimacion = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.grua))
                            .position(posicion));

                    final ValueAnimator va = ValueAnimator.ofFloat(20, 1);
                    va.setDuration(500);
                    va.setInterpolator(new AccelerateInterpolator());
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            markerAnimacion.setAnchor(0.5f, (Float) animation.getAnimatedValue());
                        }
                    });
                    va.addListener(new ValueAnimator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            LatLngInterpolator mLatLngInterpolator;
                            mLatLngInterpolator = new LatLngInterpolator.Spherical();
                            if(historial.hasNext())
                                MarkerAnimation.animateMarkerToICS(markerAnimacion,((State)historial.next()).getLocation(), mLatLngInterpolator,vs);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    va.start();
                }
            }
        });
    }

    public void rutaGrua(Marker marker,LatLngInterpolator latLngInterpolator){
        if(historial.hasNext()) {
            MarkerAnimation.animateMarkerToICS(marker, ((State) historial.next()).getLocation(), latLngInterpolator, vs);
        }else{
            animacion=true;
        }
    }
    private void mostrarDatosServicio(String estado){
        this.mostrarDatos();
        if(estado.equals(Service.StateService.ESPERA.getDescription())){
            this.mostrarDatosPendienteAceptar();
        } else if(estado.equals(Service.StateService.ACEPTADO.getDescription())){
            this.mostrarDatosPendientesIniciar();
        } else if(estado.equals(Service.StateService.RECHAZADO.getDescription())){
            this.mostrarDatosHistorial();
        } else if(estado.equals(Service.StateService.PAUSADO.getDescription())){
            this.mostrarDatosServicio();
        } else if(estado.equals(Service.StateService.CURSO.getDescription())){
            this.mostrarDatosServicio();
        } else if(estado.equals(Service.StateService.FINALIZADO.getDescription())){
            this.mostrarDatosHistorial();
        } else {
            this.mostrarDatosPendienteAceptar();
        }

    }

    private void clusterMapa() {
        mClusterManager = new ClusterManager<ItemCluster>(this, mMap);
        mClusterManager.setRenderer(new MyClusterRenderer(this, mMap, mClusterManager));
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        addMarkers();
    }

    public void addMarkers(){


        if(!(service.getLocation().longitude==0.0&&service.getLocation().latitude==0.0)) {
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                    .anchor(0.5f, 1.0f)
                    .position(service.getLocation()));
        }
        Iterator i =service.getHistorial();
        int indice=1;
        while(i.hasNext()){
           State estado= (State)i.next();
            ItemCluster item = new ItemCluster(estado.getLocation());
            IconGenerator tc = new IconGenerator(this);

            if(estado.getEstado().equals(Service.StateService.ESPERA.getDescription())){
                tc.setStyle(IconGenerator.STYLE_WHITE);
            } else if(estado.getEstado().equals(Service.StateService.ACEPTADO.getDescription())){
                tc.setStyle(IconGenerator.STYLE_ORANGE);
            } else if(estado.getEstado().equals(Service.StateService.RECHAZADO.getDescription())){
                tc.setStyle(IconGenerator.STYLE_RED);
            } else if(estado.getEstado().equals(Service.StateService.PAUSADO.getDescription())){
                tc.setStyle(IconGenerator.STYLE_PURPLE);
            } else if(estado.getEstado().equals(Service.StateService.CURSO.getDescription())){
                tc.setStyle(IconGenerator.STYLE_BLUE);
            } else if(estado.getEstado().equals(Service.StateService.FINALIZADO.getDescription())) {
                tc.setStyle(IconGenerator.STYLE_GREEN);
            }

            Bitmap bmp = tc.makeIcon(indice+". "+estado.getEstado()); // pass the text you want.
            item.setIcon(bmp);
            item.setTitle(estado.getFecha());
            mClusterManager.addItem(item);
            indice++;
            }



        }

    @Override
    public void nochangeStateEnCurso() {
        //Cualquier cambio visual hacerlo con this.runOnUiThread()
        Log.d("Service", "Ya hay un servicio en curso");
        if(spinnerUso){
            if(service.getEstado().equals(Service.StateService.CURSO.getDescription()))
            {
                spinnerEstados.setSelection(0);
            }else{
                spinnerEstados.setSelection(1);
            }
        }
        spinnerUso=false;


        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Ya hay un servicio en curso", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void notifyNoAccessGPSLocation() {
        //Cualquier cambio visual hacerlo con this.runOnUiThread()
        Log.d("Service", "No hay Localizacion");

        mostrarDatosServicio(service.getEstado());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(VistaServicio.this,AlertDialog.THEME_HOLO_DARK);

                alertDialog.setTitle("Activar GPS")
                        .setMessage("El GPS está desactivado. Debe activarlo para el correcto funcionamiento de la aplicación.")
                        .setIcon(R.drawable.ic_launcher);

                alertDialog.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                
                alertDialog.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });
    }



    @Override
    public void changeState(Service.StateService stateService) {
        String auxServicio=service.getEstado();
        service.setEstado(stateService);
       if(!(((auxServicio.equals(Service.StateService.CURSO.getDescription()))&&(stateService.equals(Service.StateService.PAUSADO)))||((auxServicio.equals(Service.StateService.PAUSADO.getDescription()))&&(stateService.equals(Service.StateService.CURSO))))){
            mostrarDatosServicio(stateService.getDescription());
        }
        clusterMapa();
    }



}
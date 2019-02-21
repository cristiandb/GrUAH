/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gruas.app;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.gruas.app.R;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.manager.Statistics;
import com.gruas.app.couchBaseLite.views.ReducerStatistic;
import com.gruas.app.holographlibrary.Line;
import com.gruas.app.holographlibrary.LineGraph;
import com.gruas.app.holographlibrary.LinePoint;
import com.gruas.app.holographlibrary.PieGraph;
import com.gruas.app.holographlibrary.PieSlice;
import com.gruas.app.services.gps.GPSCouchDBService;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Estadisticas extends FragmentActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;


    private static GPSCouchDBService gpsservice;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        bindService(new Intent(this, GPSCouchDBService.class), scgps, Context.BIND_AUTO_CREATE);
    }

    private void bindServiced(){
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Volvemos hacia atras
        super.onBackPressed();
        gpsservice.notifyBackToMainActivity();
        unbindService(scgps);
        return true;
    }



    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new EstadisticasDatosFragment();
                case 1:
                    return new EstadisticasEvolucionFragment();
                case 2:
                    return new EstadisticasMapaFragment();
                default:
                    return new EstadisticasDatosFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Datos";
                case 1:
                    return "Evolución";
                case 2:
                   return "Mapa";
                default:
                    return "Datos";
            }

        }
    }

    public static class EstadisticasDatosFragment extends Fragment {

        private  View rootView;
        private PieGraph pg;
        private  PieSlice rechazados,finalizados,pendientes;
        private TextView totaltv,finalizadostv,rechazadostv,pendientestv;
        private RelativeLayout rv;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            if(rootView==null){
                rootView = inflater.inflate(R.layout.estadisticasdatos, container, false);
                String [] values = {"Dia","Semana","Mes","Total"};
                final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinnerFecha);
                rv=(RelativeLayout)rootView.findViewById(R.id.noDatos);
                totaltv=(TextView)rootView.findViewById(R.id.totalServicios);
                finalizadostv=(TextView)rootView.findViewById(R.id.serviciosFinalizados);
                rechazadostv=(TextView)rootView.findViewById(R.id.serviciosRechazados);
                pendientestv=(TextView)rootView.findViewById(R.id.serviciosPendientes);
                ArrayAdapter<String> LTRadapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
                LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(LTRadapter);
                pg = (PieGraph)rootView.findViewById(R.id.graph);
                rechazados = new PieSlice();
                rechazados.setColor(Color.parseColor("#CC0000"));
                finalizados = new PieSlice();
                finalizados.setColor(Color.parseColor("#99CC00"));
                pendientes = new PieSlice();
                pendientes.setColor(Color.parseColor("#FF8800"));

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        cambiarGrafico(arg2);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

            }
            else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
            }

            return rootView;
        }

        private void cambiarGrafico(Integer opcion){
            Statistics est;
           switch(opcion){
                case 0:
                    est=gpsservice.obtenerEstadisticas(ReducerStatistic.DateFiltro.DIA);
                    break;
                case 1:
                    est=gpsservice.obtenerEstadisticas(ReducerStatistic.DateFiltro.SEMANA);
                    break;
                case 2:
                    est=gpsservice.obtenerEstadisticas(ReducerStatistic.DateFiltro.MES);
                    break;
                default:
                    est=gpsservice.obtenerEstadisticas(ReducerStatistic.DateFiltro.ALL);
                    break;
            }
            if(est.getNumServicesTotal()>0)
                rv.setVisibility(View.GONE);
            else
                rv.setVisibility(View.VISIBLE);
            pg.removeSlices();
            rechazados.setValue(est.getNumServicesRechazados());
            if(est.getNumServicesRechazados()>0)
            pg.addSlice(rechazados);
            finalizados.setValue(est.getNumServicesFinalizados());
            if(est.getNumServicesFinalizados()>0)
            pg.addSlice(finalizados);
            pendientes.setValue((est.getNumServicesTotal()-est.getNumServicesRechazados()-est.getNumServicesFinalizados()));
            if((est.getNumServicesTotal()-est.getNumServicesRechazados()-est.getNumServicesFinalizados())>0)
            pg.addSlice(pendientes);

            totaltv.setText(String.valueOf(est.getNumServicesTotal()));
            rechazadostv.setText(String.valueOf(est.getNumServicesRechazados()));
            finalizadostv.setText(String.valueOf(est.getNumServicesFinalizados()));
            pendientestv.setText(String.valueOf((est.getNumServicesTotal()-est.getNumServicesRechazados()-est.getNumServicesFinalizados())));
        }
    }


    public static class EstadisticasEvolucionFragment extends Fragment {

        private  Line rechazados,finalizados,total,otros;
        private  View rootView;
        private LineGraph li;

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if(rootView==null) {
                rootView = inflater.inflate(R.layout.estadisticasevolucion, container, false);

                String [] values = {"Mes","Año"};
                final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinnerevolucion);
                ArrayAdapter<String> LTRadapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
                LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(LTRadapter);
                li = (LineGraph)rootView.findViewById(R.id.graphevolucion);


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        cambiarGrafico(arg2);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
            }
            return rootView;
        }

        private void cambiarGrafico(Integer opcion){
            Statistics est;
            LinePoint p;
            int valorMaximo=0;
            switch(opcion){
                case 0:
                    li.removeAllLines();
                    valorMaximo=0;
                    Calendar calIni= Calendar.getInstance();
                    Calendar calFin= Calendar.getInstance();
                    int max=calIni.getMaximum(Calendar.DAY_OF_MONTH);
                    rechazados=new Line();
                    rechazados.setColor(Color.parseColor("#CC0000"));
                    finalizados=new Line();
                    finalizados.setColor(Color.parseColor("#99CC00"));
                    total=new Line();
                    total.setColor(Color.parseColor("#000000"));
                    otros=new Line();
                    otros.setColor(Color.parseColor("#FF8800"));
                    for (int i=1;i<=max;i++){
                        calIni.set(calIni.get(Calendar.YEAR),calIni.get(Calendar.MONTH),i,00,00);
                        calFin.set(calFin.get(Calendar.YEAR),calFin.get(Calendar.MONTH),i,23,59);
                        est=gpsservice.obtenerEstadisticas(calFin.getTime().getTime(),calIni.getTime().getTime());
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesFinalizados());
                        finalizados.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesRechazados());
                        rechazados.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY((est.getNumServicesTotal() - est.getNumServicesRechazados() - est.getNumServicesFinalizados()));
                        otros.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesTotal());
                        if(est.getNumServicesTotal()>valorMaximo)valorMaximo=est.getNumServicesTotal();
                        total.addPoint(p);

                    }
                    li.addLine(finalizados);
                    li.addLine(rechazados);
                    li.addLine(otros);
                    li.addLine(total);
                    li.showHorizontalGrid(true);
                    li.showMinAndMaxValues(true);
                    li.setGridColor(Color.parseColor("#000000"));
                    li.setTextColor(10);
                    li.setMinY(0);
                    li.setRangeY(0, valorMaximo+1);
                    li.showContextMenu();

                    break;
                case 1:
                    li.removeAllLines();

                    valorMaximo=0;
                    Calendar calIniM= Calendar.getInstance();
                    Calendar calFinM= Calendar.getInstance();
                    int maxM=calIniM.getMaximum(Calendar.DAY_OF_MONTH);
                    rechazados=new Line();
                    rechazados.setColor(Color.parseColor("#CC0000"));
                    finalizados=new Line();
                    finalizados.setColor(Color.parseColor("#99CC00"));
                    total=new Line();
                    total.setColor(Color.parseColor("#000000"));
                    otros=new Line();
                    otros.setColor(Color.parseColor("#FF8800"));
                    for (int i=0;i<12;i++){
                        calIniM.set(calIniM.get(Calendar.YEAR),i,1,00,01);
                        calFinM.set(calFinM.get(Calendar.YEAR),i,maxM,23,59);
                        est=gpsservice.obtenerEstadisticas(calFinM.getTime().getTime(),calIniM.getTime().getTime());
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesFinalizados());
                        finalizados.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesRechazados());
                        rechazados.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY((est.getNumServicesTotal() - est.getNumServicesRechazados() - est.getNumServicesFinalizados()));
                        otros.addPoint(p);
                        p = new LinePoint();
                        p.setX(i);
                        p.setY(est.getNumServicesTotal());
                        if(est.getNumServicesTotal()>valorMaximo)valorMaximo=est.getNumServicesTotal();
                        total.addPoint(p);
                    }
                    li.addLine(finalizados);
                    li.addLine(rechazados);
                    li.addLine(otros);
                    li.addLine(total);
                    li.showHorizontalGrid(true);
                    li.showMinAndMaxValues(true);
                    li.setMinY(0);
                    li.setTextColor(Color.parseColor("#000000"));
                    li.setRangeY(0, valorMaximo+1);
                    li.showContextMenu();
                    break;
                default:
                    li.removeAllLines();
                    break;
            }
        }
    }

    public static class EstadisticasMapaFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private GoogleMap mapa;

        private HeatmapTileProvider mProvider;
        private TileOverlay mOverlay=null;
        private View rootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if(rootView==null){
                rootView = inflater.inflate(R.layout.estadisticasmapa, container, false);

                if(mapa==null) {
                    mapa = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapaEstadisticas)).getMap();
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4378271, -3.6795367), 5));
                }
                    addHeatMap();

            }else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
                if(mapa==null) {
                    mapa = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapaEstadisticas)).getMap();
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4378271, -3.6795367), 5));
                }
                addHeatMap();
            }
            return rootView;
        }

        private void addHeatMap() {
            if(mOverlay!=null)
            mOverlay.remove();
            List<LatLng> list =gpsservice.obtenerPosicionesClientes();
            if(list.size()>0) {
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                mProvider = new HeatmapTileProvider.Builder()
                        .data(list)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mOverlay = mapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }
        }
    }
}

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

package com.gruas.app.fragments;
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
import com.gruas.app.holographlibrary.Line;
import com.gruas.app.holographlibrary.LineGraph;
import com.gruas.app.holographlibrary.LinePoint;
import com.gruas.app.holographlibrary.PieGraph;
import com.gruas.app.holographlibrary.PieSlice;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Estadisticas extends FragmentActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            if(rootView==null){
                rootView = inflater.inflate(R.layout.estadisticasdatos, container, false);
                String [] values = {"Diario","Semanal","Mensual","Total"};
                Spinner spinner = (Spinner) rootView.findViewById(R.id.spinnerFecha);
                ArrayAdapter<String> LTRadapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
                LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(LTRadapter);

                PieGraph pg = (PieGraph)rootView.findViewById(R.id.graph);
                PieSlice rechazados = new PieSlice();
                rechazados.setColor(Color.parseColor("#CC0000"));
                rechazados.setValue(2);
                pg.addSlice(rechazados);
                PieSlice finalizados = new PieSlice();
                finalizados.setColor(Color.parseColor("#99CC00"));
                finalizados.setValue(3);
                pg.addSlice(finalizados);
            }
            else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
            }




          /*  rootView.findViewById(R.id.demo_collection_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });


            rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image/*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });*/

            return rootView;
        }
    }


    public static class EstadisticasEvolucionFragment extends Fragment {

        private  View rootView;
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if(rootView==null) {
                rootView = inflater.inflate(R.layout.estadisticasevolucion, container, false);

                String [] values = {"Diario","Semanal","Mensual","Total"};
                Spinner spinner = (Spinner) rootView.findViewById(R.id.spinnerevolucion);
                ArrayAdapter<String> LTRadapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, values);
                LTRadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(LTRadapter);

                Line rechazados = new Line();
                Line finalizados = new Line();
                Line total = new Line();
                LinePoint p = new LinePoint();
                p.setX(0);
                p.setY(5);
                finalizados.addPoint(p);
                p = new LinePoint();
                p.setX(8);
                p.setY(8);
                finalizados.addPoint(p);
                p = new LinePoint();
                p.setX(10);
                p.setY(4);
                finalizados.addPoint(p);
                finalizados.setColor(Color.parseColor("#99CC00"));


                LinePoint p2 = new LinePoint();
                p2.setX(2);
                p2.setY(3);
                rechazados.addPoint(p2);
                p2 = new LinePoint();
                p2.setX(5);
                p2.setY(7);
                rechazados.addPoint(p2);
                p2 = new LinePoint();
                p2.setX(8);
                p2.setY(5);
                rechazados.addPoint(p2);
                rechazados.setColor(Color.parseColor("#CC0000"));

                LinePoint p3 = new LinePoint();
                p3.setX(6);
                p3.setY(6);
                rechazados.addPoint(p3);
                p3 = new LinePoint();
                p3.setX(5);
                p3.setY(1);
                rechazados.addPoint(p3);
                p3 = new LinePoint();
                p3.setX(3);
                p3.setY(2);
                rechazados.addPoint(p3);
                rechazados.setColor(Color.parseColor("#000000"));

                LineGraph li = (LineGraph)rootView.findViewById(R.id.graphevolucion);
                li.addLine(finalizados);
                li.addLine(rechazados);
                li.addLine(total);
                li.setRangeY(0, 10);
                li.setLineToFill(0);

            }else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
            }
            return rootView;
        }
    }

    public static class EstadisticasMapaFragment extends Fragment {


        public static final String ARG_SECTION_NUMBER = "section_number";
        private GoogleMap mapa;

        private HeatmapTileProvider mProvider;
        private TileOverlay mOverlay;
        private View rootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if(rootView==null){
                rootView = inflater.inflate(R.layout.estadisticasmapa, container, false);

                if(mapa==null) {
                    mapa = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapaEstadisticas)).getMap();
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.4378271, -3.6795367), 5));

                    addHeatMap();
                }
            }else{
                ViewGroup parent = (ViewGroup) rootView.getParent();
                parent.removeView(rootView);
            }


            return rootView;
        }

        private void addHeatMap() {
            List<LatLng> list = new ArrayList<LatLng>();
            list.add(new LatLng(-37.1886,145.708));
            list.add(new LatLng(-37.8361,144.845));
            list.add(new LatLng(-38.4034,144.192));
            list.add(new LatLng(-38.7597,143.67));
            list.add(new LatLng(-36.9672,141.083));


            // Create a heat map tile provider, passing it the latlngs of the police stations.
             mProvider = new HeatmapTileProvider.Builder()
                    .data(list)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
           mOverlay= mapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }
}

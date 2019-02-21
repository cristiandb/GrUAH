package com.gruas.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.gruas.app.GruasDelegate;
import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;
import com.gruas.app.lista.Elemento;
import com.gruas.app.lista.ElementoAdapter;
import com.gruas.app.R;
import com.gruas.app.servicio.VistaServicio;

import java.util.ArrayList;

public class Lista extends Fragment {
    private ArrayList<Object> datos = new ArrayList();
    private DocumentsAdapter adapter;
    private Intent vistaServicio;
    private SwipeRefreshLayout swipeLayout;
    private ElementoAdapter adaptador;
    private ListView lista;
    private FiltroLista filtro;
    private boolean use_refresh = false;

    public Lista(DocumentsAdapter adaptador){
        super();
        this.adapter = adaptador;
    }

    public void setFiltro(FiltroLista filtro){
        this.filtro = filtro;
    }

    public void filterList(CharSequence constraint){
        if(lista != null && adaptador != null)
            adaptador.getFilter().filter(constraint);
    }

    public void refresh(){
        if(lista != null) {
            ((GruasDelegate)getActivity()).clearTextSearch();
            adapter.setAdaptador(DocumentsAdapter.TypeAdapter.ELEMENTOS);
            datos = adapter.transformDocuments(filtro);
            if(adaptador == null) adaptador = new ElementoAdapter(this, datos);
            adaptador.setElementos(datos);
            lista.setAdapter(adaptador);

            if(filtro == FiltroLista.LISTA_ESPERANDO_APROBACION)
                ((GruasDelegate)getActivity()).cancelarNotificacion();

            if(use_refresh){
                getActivity().runOnUiThread(new Runnable() {
                    // El fragment no es capaz de actualizar la parte de la interfaz fuera del metodo onCreateView
                    @Override
                    public void run() {
                        adaptador.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_lista, container, false);
        lista = (ListView)rootView.findViewById(R.id.Lista);
        lista.setEmptyView(rootView.findViewById(R.id.emptyListView));
        lista.setTextFilterEnabled(true);
        refresh();
        use_refresh = true;

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.SwipeRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeLayout.setRefreshing(false);
            }
        });

        swipeLayout.setColorScheme(android.R.color.holo_orange_dark, android.R.color.holo_orange_light, android.R.color.holo_orange_dark, android.R.color.holo_orange_light);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Le pasamos a VistaServicio un objeto de tipo Service
                adapter.setAdaptador(DocumentsAdapter.TypeAdapter.SERVICIOS);
                if(vistaServicio == null)
                    vistaServicio= new Intent(view.getContext(), VistaServicio.class);

                vistaServicio.putExtra("Servicio",(Service)adapter.transformDocument(((Elemento)datos.get(position)).getIdDoc(),false));
                startActivity(vistaServicio);
            }

        });


        return rootView;
    }
}

package com.gruas.app.fragments;

import com.gruas.app.couchBaseLite.Service;
import com.gruas.app.couchBaseLite.manager.ManagerGruasCouchDB;

import java.util.ArrayList;
import java.util.List;

public enum FiltroLista {
    LISTA_CURSO {
        @Override
        protected void createFilter(){
            this.nameView = ManagerGruasCouchDB.VIEW_SERVICE_FOR_LISTA_CURSO;
        }
    },
    LISTA_PENDIENTES{
        @Override
        protected void createFilter(){
            this.nameView = ManagerGruasCouchDB.VIEW_SERVICE_FOR_LISTA_PENDIENTE;
        }
    },
    LISTA_ESPERANDO_APROBACION{
        @Override
        protected void createFilter(){
            this.nameView = ManagerGruasCouchDB.VIEW_SERVICE_FOR_LISTA_ESPERANDO_APROBACION;
        }
    },
    LISTA_HISTORIAL{
        @Override
        protected void createFilter(){
            this.nameView = ManagerGruasCouchDB.VIEW_SERVICE_FOR_LISTA_HISTORIAL;
        }
    };

    protected String nameView;

    FiltroLista(){
        createFilter();
    }

    protected abstract void createFilter();
    public String getNameView(){ return this.nameView; }
}

package com.gruas.app.couchBaseLite;

import com.google.android.gms.maps.model.LatLng;
import com.gruas.app.UtilDateFormat;
import com.gruas.app.couchBaseLite.adapter.AdapterObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Service implements Serializable,AdapterObject {
    public enum StateService {
        ESPERA ("En espera"),
        ACEPTADO ("Aceptado"),
        RECHAZADO ("Rechazado"),
        PAUSADO ("Pausado"),
        CURSO ("En curso"),
        FINALIZADO ("Finalizado");

        private String description;

        StateService(String cadena){
            this.description = cadena;
        }

        public String getDescription(){
            return this.description;
        }
    }

    /* Variables */
    private String nIncidencia, nombreCliente, direccion, observaciones, telefono, matricula, modelo, color, estado, idDoc;
    private String fecha;
    private double latCliente, longCliente;
    private StateService ss;
    private ArrayList<State> historial;

    public Service(String nIncidencia, String nombreCliente, String direccion, String observaciones, String telefono, String matricula, String modelo, String color, long date, String estado, double latCliente, double longCliente, String idDoc) {
        this.nIncidencia = nIncidencia;
        this.nombreCliente = nombreCliente;
        this.direccion = direccion;
        this.observaciones = observaciones;
        this.telefono = telefono;
        this.matricula = matricula;
        this.modelo = modelo;
        this.color = color;
        this.fecha = UtilDateFormat.getStringDateFormatWithLongTime(date);
        this.latCliente = latCliente;
        this.longCliente = longCliente;
        this.idDoc = idDoc;
        setEstado(estado);
        this.historial = new ArrayList();
    }

    /*Metodos Getter*/
    public String getnIncidencia() { return nIncidencia; }
    public String getNombreCliente() { return nombreCliente; }
    public String getDireccion() { return direccion; }
    public String getObservaciones() { return observaciones; }
    public String getTelefono() { return telefono; }
    public String getMatricula() { return matricula; }
    public String getModelo() { return modelo; }
    public String getColor() { return color; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public StateService getSs(){ return this.ss;}

    @Override
    public String getIdDoc() { return idDoc; }

    public void setEstado(StateService state){
        this.estado = state.getDescription();
        this.ss = state;
    }

    public void setEstado(String state){
        if(state.equals(StateService.ESPERA.getDescription())){
            this.estado = state;
            this.ss = Service.StateService.ESPERA;
        } else if(state.equals(StateService.ACEPTADO.getDescription())){
            this.estado = state;
            this.ss = StateService.ACEPTADO;
        } else if(state.equals(StateService.RECHAZADO.getDescription())){
            this.estado = state;
            this.ss = StateService.RECHAZADO;
        } else if(state.equals(StateService.PAUSADO.getDescription())){
            this.estado = state;
            this.ss = StateService.PAUSADO;
        } else if(state.equals(StateService.CURSO.getDescription())){
            this.estado = state;
            this.ss = StateService.CURSO;
        } else if(state.equals(StateService.FINALIZADO.getDescription())){
            this.estado = state;
            this.ss = StateService.FINALIZADO;
        } else {
            this.estado = StateService.ESPERA.getDescription();
            this.ss = Service.StateService.ESPERA;
        }
    }

    public void addState(State estado){
        historial.add(estado);
    }

    public Iterator<State> getHistorial(){
        return historial.iterator();
    }

    public LatLng getLocation(){
        return new LatLng(latCliente,longCliente);
    }

    public static StateService getStateService(String state){
        StateService newSS;
        if(state.equals(StateService.ESPERA.getDescription()))
            newSS = Service.StateService.ESPERA;
        else if(state.equals(StateService.ACEPTADO.getDescription()))
            newSS = StateService.ACEPTADO;
        else if(state.equals(StateService.RECHAZADO.getDescription()))
            newSS = StateService.RECHAZADO;
        else if(state.equals(StateService.PAUSADO.getDescription()))
            newSS = StateService.PAUSADO;
        else if(state.equals(StateService.CURSO.getDescription()))
            newSS = StateService.CURSO;
        else if(state.equals(StateService.FINALIZADO.getDescription()))
            newSS = StateService.FINALIZADO;
        else
            newSS = Service.StateService.ESPERA;

        return newSS;
    }
}

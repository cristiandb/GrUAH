package com.gruas.app.couchBaseLite.views.reducerFilters;

import com.gruas.app.couchBaseLite.Service.StateService;
import com.gruas.app.couchBaseLite.manager.Statistics;
import com.gruas.app.couchBaseLite.views.ReducerStatistic.DateFiltro;

import java.util.Calendar;
import java.util.Date;

public class FilterByActualDate implements FilterStatistic {
    private DateFiltro filtro;
    private long fechaActualMS;
    private int numServicesTotal = 0, numServicesRechazados = 0, numServicesFinalizados = 0;

    public FilterByActualDate(DateFiltro filter){
        this.filtro = filter;
        this.fechaActualMS = restarDiasFechaActual(); //En milisegundos
    }

    @Override
    public void filtrar(String estado, long fechaServicio) {
        if(filtro == DateFiltro.ALL){
            numServicesTotal++;
            if(estado.equals(StateService.FINALIZADO.getDescription())) numServicesFinalizados++;
            if(estado.equals(StateService.RECHAZADO.getDescription())) numServicesRechazados++;
        } else {
            if((fechaActualMS - fechaServicio) < 0){
                numServicesTotal++;
                if(estado.equals(StateService.FINALIZADO.getDescription())) numServicesFinalizados++;
                if(estado.equals(StateService.RECHAZADO.getDescription())) numServicesRechazados++;
            }
        }
    }

    @Override
    public Statistics createStatistics() {
        return new Statistics(numServicesTotal,numServicesRechazados,numServicesFinalizados);
    }

    private long restarDiasFechaActual(){
        Date fechaActual = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);

        switch(filtro){
            case DIA:
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case SEMANA:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case MES:
                calendar.add(Calendar.MONTH, -1);
                break;
        }

        return calendar.getTime().getTime();
    }
}

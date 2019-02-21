package com.gruas.app.couchBaseLite.views.reducerFilters;

import com.gruas.app.couchBaseLite.Service.StateService;
import com.gruas.app.couchBaseLite.manager.Statistics;

public class FilterByRange implements FilterStatistic {
    private RangeDate range;
    private int numServicesTotal = 0, numServicesRechazados = 0, numServicesFinalizados = 0;

    public FilterByRange(RangeDate range){
        this.range = range;
    }

    @Override
    public void filtrar(String estado, long fechaServicio) {
        if(fechaServicio >= range.getDateEnd() && fechaServicio <= range.getDateStart()){
            numServicesTotal++;
            if(estado.equals(StateService.FINALIZADO.getDescription())) numServicesFinalizados++;
            if(estado.equals(StateService.RECHAZADO.getDescription())) numServicesRechazados++;
        }
    }

    @Override
    public Statistics createStatistics() {
        return new Statistics(numServicesTotal,numServicesRechazados,numServicesFinalizados);
    }
}

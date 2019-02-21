package com.gruas.app.couchBaseLite.views;

import com.couchbase.lite.Reducer;
import com.gruas.app.couchBaseLite.views.reducerFilters.FilterByActualDate;
import com.gruas.app.couchBaseLite.views.reducerFilters.FilterByRange;
import com.gruas.app.couchBaseLite.views.reducerFilters.FilterStatistic;
import com.gruas.app.couchBaseLite.views.reducerFilters.Parameters;
import com.gruas.app.couchBaseLite.views.reducerFilters.RangeDate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReducerStatistic implements Reducer {
    public enum DateFiltro { DIA, SEMANA, MES, ALL }

    //Variables
    private FilterStatistic filtro = new FilterByActualDate(DateFiltro.ALL);

    @Override
    public Object reduce(List<Object> objects, List<Object> objects2, boolean b) {
        Iterator i = objects.iterator();

        while(i.hasNext()){
            ArrayList<Object> keys = (ArrayList)i.next();
            String estado = (String)keys.get(0);
            long fechaServicio = (Long) keys.get(1);
            filtro.filtrar(estado,fechaServicio);
        }

        return filtro.createStatistics();
    }

    public void setFiltro(Parameters parameters){
        String className = parameters.getParameters().getClass().getSimpleName();
        if(className.equals("RangeDate"))
            this.filtro = new FilterByRange((RangeDate) parameters.getParameters());
        else if(className.equals("DateFiltro"))
            this.filtro = new FilterByActualDate((DateFiltro) parameters.getParameters());
        else
            this.filtro = new FilterByActualDate(DateFiltro.ALL);
    }

    public static Parameters createParametersFilterStatistic(Object parameters){
        return new Parameters(parameters);
    }
}

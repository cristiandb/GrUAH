package com.gruas.app.couchBaseLite.views.reducerFilters;

import com.gruas.app.couchBaseLite.manager.Statistics;

public interface FilterStatistic {
    public void filtrar(String estado, long fechaServicio);
    public Statistics createStatistics();
}

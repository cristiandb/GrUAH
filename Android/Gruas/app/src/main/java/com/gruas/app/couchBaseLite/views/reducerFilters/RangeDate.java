package com.gruas.app.couchBaseLite.views.reducerFilters;

public class RangeDate {
    private long dateStart = 0;
    private long dateEnd = 0;

    public RangeDate(long dateStart, long dateEnd){
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public long getDateStart(){ return dateStart; }
    public long getDateEnd(){ return dateEnd; }
}

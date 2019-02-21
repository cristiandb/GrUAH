package com.gruas.app.couchBaseLite.manager;

public class Statistics {
    private int numServicesTotal = 0, numServicesRechazados = 0, numServicesFinalizados = 0;

    public Statistics(int nst, int nsr, int nsf){
        this.numServicesTotal = nst;
        this.numServicesRechazados = nsr;
        this.numServicesFinalizados = nsf;
    }

    public int getNumServicesTotal(){
        return this.numServicesTotal;
    }

    public int getNumServicesRechazados(){
        return this.numServicesRechazados;
    }

    public int getNumServicesFinalizados(){
        return this.numServicesFinalizados;
    }
}

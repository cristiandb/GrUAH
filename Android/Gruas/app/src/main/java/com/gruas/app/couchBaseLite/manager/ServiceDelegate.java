package com.gruas.app.couchBaseLite.manager;

import com.gruas.app.couchBaseLite.Service;

public interface ServiceDelegate {
    public void nochangeStateEnCurso();
    public void notifyNoAccessGPSLocation();
    public void changeState(Service.StateService stateService);
}

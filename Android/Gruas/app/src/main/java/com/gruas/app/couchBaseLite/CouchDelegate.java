package com.gruas.app.couchBaseLite;

import com.gruas.app.couchBaseLite.adapter.DocumentsAdapter;

public interface CouchDelegate {
    public DocumentsAdapter getDocumentsAdapter();
    public void handlerErrorCouch(CouchException.TypeErrors type,String error);
}

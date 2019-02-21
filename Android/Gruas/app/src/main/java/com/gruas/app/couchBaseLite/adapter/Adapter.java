package com.gruas.app.couchBaseLite.adapter;

import com.couchbase.lite.Document;

public interface Adapter {
    public boolean isDocumentValid(Document doc,String key,String compare);
    public Object transformDocument(Document doc);
}

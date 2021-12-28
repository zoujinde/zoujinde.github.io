package com.jinde.web.model;

public interface DataObject {
    public String getTableName();
    // Get the AutoId name
    public String getAutoIdName();
    // Get the slave table ID name
    public String getSlaveIdName();
}

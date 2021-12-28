package com.jinde.web.model;

public interface DataObject {

    // Get the table name
    public String getTableName();

    // Get the primary key name
    public String[] getPrimaryKey();

    // Get the AutoId name
    public String getAutoIdName();

    // Get the slave table ID name
    public String getSlaveIdName();

}

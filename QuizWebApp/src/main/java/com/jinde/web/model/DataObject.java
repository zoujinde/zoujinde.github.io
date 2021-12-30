package com.jinde.web.model;

public abstract class DataObject {

    // The next object for the singly linked list
    private DataObject next;

    // Get the next object
    @SuppressWarnings("unchecked")
    public <T> T getNext(){
        return (T) next;
    }

    // Set the next object
    public void setNext(DataObject obj){
        this.next = obj;
    }

    // Get the table name
    public abstract String getTableName();

    // Get the primary key name
    public abstract String[] getPrimaryKey();

    // Get the AutoId name
    public abstract String getAutoIdName();

}

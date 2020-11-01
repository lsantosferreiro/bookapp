package com.example.bookapp.userAPI;

public class Event {
    //Variables
    private String typeEvents;
    private long dni;
    private String description;
    private long id;

    //Getters and setters
    public String getTypeEvents() {
        return typeEvents;
    }

    public void setTypeEvents(String typeEvents) {
        this.typeEvents = typeEvents;
    }

    public long getDni() {
        return dni;
    }

    public void setDni(long dni) {
        this.dni = dni;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

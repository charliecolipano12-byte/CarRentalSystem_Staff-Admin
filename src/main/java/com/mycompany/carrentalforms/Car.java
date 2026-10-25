package com.mycompany.carrentalforms;

public class Car {
    public static final String AVAILABLE   = "Available";
    public static final String RESERVED    = "Reserved";
    public static final String RENTED      = "Rented";
    public static final String MAINTENANCE = "Maintenance";

    private final int id;
    private final String plate, model, type, status;
    private final double dailyRate;

    public Car(int id, String plate, String model, String type, double dailyRate, String status) {
        this.id = id; this.plate = plate; this.model = model;
        this.type = type; this.dailyRate = dailyRate; this.status = status;
    }

    public int getId()           { return id; }
    public String getPlate()     { return plate; }
    public String getModel()     { return model; }
    public String getType()      { return type; }
    public double getDailyRate() { return dailyRate; }
    public String getStatus()    { return status; }
    public boolean isAvailable() { return AVAILABLE.equals(status); }
}

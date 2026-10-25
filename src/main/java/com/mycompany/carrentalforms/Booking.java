package com.mycompany.carrentalforms;

import java.time.LocalDateTime;

public class Booking {
    public static final String PENDING   = "Pending";
    public static final String CONFIRMED = "Confirmed";
    public static final String REJECTED  = "Rejected";
    public static final String ON_RENT   = "On Rent";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";

    private String id;                 // booking_ref, e.g. BK-0001 (shown in the UI)
    private Customer customer;
    private String vehicle, dates, status;
    private double total;
    private LocalDateTime timestamp;   // queue arrival time

    // Database-side details (set by BookingDAO)
    private int dbId, carId;
    private LocalDateTime pickup, returnDate;
    private String note;
    private String handledBy;

    public Booking(String id, Customer customer, String vehicle,
                   String dates, double total, String status, LocalDateTime timestamp) {
        this.id = id;
        this.customer = customer;
        this.vehicle = vehicle;
        this.dates = dates;
        this.total = total;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId()               { return id; }
    public Customer getCustomer()       { return customer; }
    public String getVehicle()          { return vehicle; }
    public String getDates()            { return dates; }
    public double getTotal()            { return total; }
    public String getStatus()           { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public int getDbId()                    { return dbId; }
    public void setDbId(int dbId)           { this.dbId = dbId; }
    public int getCarId()                   { return carId; }
    public void setCarId(int carId)         { this.carId = carId; }
    public LocalDateTime getPickup()        { return pickup; }
    public void setPickup(LocalDateTime p)  { this.pickup = p; }
    public LocalDateTime getReturnDate()    { return returnDate; }
    public void setReturnDate(LocalDateTime r) { this.returnDate = r; }
    public String getNote()                 { return note; }
    public void setNote(String note)        { this.note = note; }
    public String getHandledBy()              { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
}

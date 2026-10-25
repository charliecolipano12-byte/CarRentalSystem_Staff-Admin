package com.mycompany.carrentalforms;

public class Customer {
    public static final int VIP_THRESHOLD = 3;   // low for demos; raise it later

    private int id;
    private String fullName, phone, email, licenseNo;
    private int completedRentals;

    public int getId()                 { return id; }
    public void setId(int id)          { this.id = id; }
    public String getFullName()        { return fullName; }
    public void setFullName(String n)  { this.fullName = n; }
    public String getPhone()           { return phone; }
    public void setPhone(String p)     { this.phone = p; }
    public String getEmail()           { return email; }
    public void setEmail(String e)     { this.email = e; }
    public String getLicenseNo()       { return licenseNo; }
    public void setLicenseNo(String l) { this.licenseNo = l; }

    public int getCompletedRentals()       { return completedRentals; }
    public void setCompletedRentals(int n) { this.completedRentals = n; }

    public boolean isVip()      { return completedRentals >= VIP_THRESHOLD; }
    public int rentalsToVip()   { return Math.max(0, VIP_THRESHOLD - completedRentals); }
}

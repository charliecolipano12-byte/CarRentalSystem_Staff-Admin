package com.mycompany.carrentalforms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Live numbers for the Dashboard tab. Two read-only queries, nothing is cached. */
public class StatsDAO {

    /** A plain bag of numbers for the Dashboard. */
    public static final class Stats {
        // fleet
        public int totalCars, available, reserved, onRent, maintenance;
        public double avgDailyRate;
        // bookings
        public int pending, vipPending, regularPending;
        public int confirmed, active, completed, rejectedOrCancelled;
        public double revenueBooked;
        public LocalDateTime oldestPending;      // null when nobody is waiting
    }

    // Available + Reserved + On Rent + Maintenance always add up to Total Vehicles:
    //   Reserved = a car that is not rented / in maintenance but has a Confirmed booking waiting
    //   Available = the other cars that are free
    private static final String CARS_SQL =
        "SELECT COUNT(*) AS total, " +
        " COALESCE(SUM(k.status = 'Maintenance'), 0) AS maint, " +
        " COALESCE(SUM(k.status = 'Rented'), 0) AS rented, " +
        " COALESCE(SUM(k.status IN ('Available','Reserved') AND EXISTS (" +
        "     SELECT 1 FROM bookings b WHERE b.car_id = k.id AND b.status = 'Confirmed')), 0) AS reserved, " +
        " COALESCE(SUM(k.status IN ('Available','Reserved') AND NOT EXISTS (" +
        "     SELECT 1 FROM bookings b WHERE b.car_id = k.id AND b.status = 'Confirmed')), 0) AS available, " +
        " COALESCE(AVG(k.daily_rate), 0) AS avg_rate " +
        "FROM cars k";

    // Revenue Booked = total of every booking staff accepted (Confirmed, On Rent, Completed).
    private static final String BOOKINGS_SQL =
        "SELECT " +
        " COALESCE(SUM(b.status = 'Pending'), 0) AS pending, " +
        " COALESCE(SUM(b.status = 'Pending' AND c.completed_rentals >= ?), 0) AS vip_pending, " +
        " COALESCE(SUM(b.status = 'Confirmed'), 0) AS confirmed, " +
        " COALESCE(SUM(b.status = 'On Rent'), 0) AS on_rent, " +
        " COALESCE(SUM(b.status = 'Completed'), 0) AS completed, " +
        " COALESCE(SUM(b.status IN ('Rejected','Cancelled')), 0) AS rejected, " +
        " COALESCE(SUM(CASE WHEN b.status IN ('Confirmed','On Rent','Completed') " +
        "                   THEN b.total ELSE 0 END), 0) AS revenue, " +
        " MIN(CASE WHEN b.status = 'Pending' THEN b.created_at END) AS oldest " +
        "FROM bookings b JOIN customers c ON c.id = b.customer_id";

    public Stats load() throws SQLException {
        Stats s = new Stats();
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(CARS_SQL);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                s.totalCars    = rs.getInt("total");
                s.maintenance  = rs.getInt("maint");
                s.onRent       = rs.getInt("rented");
                s.reserved     = rs.getInt("reserved");
                s.available    = rs.getInt("available");
                s.avgDailyRate = rs.getDouble("avg_rate");
            }
            try (PreparedStatement ps = c.prepareStatement(BOOKINGS_SQL)) {
                ps.setInt(1, Customer.VIP_THRESHOLD);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    s.pending             = rs.getInt("pending");
                    s.vipPending          = rs.getInt("vip_pending");
                    s.regularPending      = s.pending - s.vipPending;
                    s.confirmed           = rs.getInt("confirmed");
                    s.active              = rs.getInt("on_rent");
                    s.completed           = rs.getInt("completed");
                    s.rejectedOrCancelled = rs.getInt("rejected");
                    s.revenueBooked       = rs.getDouble("revenue");
                    Timestamp t = rs.getTimestamp("oldest");
                    s.oldestPending = t == null ? null : t.toLocalDateTime();
                }
            }
        }
        return s;
    }
}

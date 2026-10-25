package com.mycompany.carrentalforms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    public List<Car> getCars() throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT id, plate, model, car_type, daily_rate, status FROM cars ORDER BY model";
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Car(rs.getInt("id"), rs.getString("plate"), rs.getString("model"),
                        rs.getString("car_type"), rs.getDouble("daily_rate"), rs.getString("status")));
            }
        }
        return list;
    }

    /** @return the car, or null if there is no car with that id */
    public Car findById(int carId) throws SQLException {
        String sql = "SELECT id, plate, model, car_type, daily_rate, status FROM cars WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Car(rs.getInt("id"), rs.getString("plate"), rs.getString("model"),
                        rs.getString("car_type"), rs.getDouble("daily_rate"), rs.getString("status"));
            }
        }
    }

    /** @return true if the car existed and its status was changed */
    public boolean updateStatus(int carId, String status) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE cars SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, carId);
            return ps.executeUpdate() > 0;
        }
    }

    // ================================================================ Phase 4: fleet

    private static final int MYSQL_DUPLICATE_KEY = 1062;
    private static final int MYSQL_FK_IN_USE     = 1451;

    /** @return a message for the staff member if the input is bad, otherwise null */
    private static String validate(String plate, String model, String type, double rate) {
        if (plate == null || plate.trim().isEmpty()) return "Plate number is required.";
        if (model == null || model.trim().isEmpty()) return "Model is required.";
        if (type == null  || type.trim().isEmpty())  return "Category is required.";
        if (Double.isNaN(rate) || rate <= 0)         return "Daily rate must be more than 0.";
        if (rate > 1_000_000)                        return "Daily rate looks too large.";
        return null;
    }

    /** New cars always start as Available. @return null on success, otherwise a message. */
    public String addCar(String plate, String model, String type, double rate) throws SQLException {
        String bad = validate(plate, model, type, rate);
        if (bad != null) return bad;
        String sql = "INSERT INTO cars (plate, model, car_type, daily_rate, status) VALUES (?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, plate.trim().toUpperCase());
            ps.setString(2, model.trim());
            ps.setString(3, type.trim());
            ps.setDouble(4, rate);
            ps.setString(5, Car.AVAILABLE);
            ps.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_KEY) {
                return "A car with plate " + plate.trim().toUpperCase() + " already exists.";
            }
            throw e;
        }
    }

    /**
     * Edits plate / model / category / daily rate (status has its own method).
     * Existing bookings keep the total they were saved with; only new bookings use the new rate.
     * @return null on success, otherwise a message
     */
    public String updateCar(int carId, String plate, String model, String type, double rate)
            throws SQLException {
        String bad = validate(plate, model, type, rate);
        if (bad != null) return bad;
        String sql = "UPDATE cars SET plate = ?, model = ?, car_type = ?, daily_rate = ? WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, plate.trim().toUpperCase());
            ps.setString(2, model.trim());
            ps.setString(3, type.trim());
            ps.setDouble(4, rate);
            ps.setInt(5, carId);
            if (ps.executeUpdate() != 1) return "This car no longer exists. Press Refresh.";
            return null;
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_KEY) {
                return "Another car already uses plate " + plate.trim().toUpperCase() + ".";
            }
            throw e;
        }
    }

    /**
     * Deletes a car, but ONLY if it has no active booking (Pending, Confirmed or On Rent).
     * The car row is locked first, so a booking cannot be added for it while we check.
     * A car that has old bookings (Completed / Rejected / ...) is also protected by the
     * foreign key; in that case staff are told to use Maintenance instead.
     * @return null on success, otherwise a message
     */
    public String deleteCar(int carId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM cars WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "This car no longer exists. Press Refresh."; }
                    }
                }
                int active;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM bookings WHERE car_id = ? " +
                        "AND status IN ('Pending','Confirmed','On Rent')")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); active = rs.getInt(1); }
                }
                if (active > 0) {
                    c.rollback();
                    return "Cannot delete: this car has " + active + " active booking"
                         + (active == 1 ? "" : "s") + " (pending, confirmed or on rent). "
                         + "Finish or reject them first.";
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM cars WHERE id = ?")) {
                    ps.setInt(1, carId);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                if (e.getErrorCode() == MYSQL_FK_IN_USE) {
                    return "Cannot delete: this car appears in past bookings (history). "
                         + "Set it to Maintenance to take it out of service instead.";
                }
                throw e;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Manual status change by staff. Only Available and Maintenance can be set by hand;
     * Rented is set by "Mark picked up" and cleared by "Mark returned".
     * Refused while the car is out on rent, and refused for Maintenance while it still
     * has a Confirmed booking waiting for pickup.
     * @return null on success, otherwise a message
     */
    public String setStatus(int carId, String newStatus) throws SQLException {
        if (!Car.AVAILABLE.equals(newStatus) && !Car.MAINTENANCE.equals(newStatus)) {
            return "Staff can only set Available or Maintenance. Rented is set by 'Mark picked up'.";
        }
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String current;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM cars WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "This car no longer exists. Press Refresh."; }
                        current = rs.getString(1);
                    }
                }
                if (newStatus.equals(current)) { c.rollback(); return "The car is already " + current + "."; }

                int onRent, confirmed;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT SUM(status = 'On Rent'), SUM(status = 'Confirmed') " +
                        "FROM bookings WHERE car_id = ?")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); onRent = rs.getInt(1); confirmed = rs.getInt(2); }
                }
                if (Car.RENTED.equals(current) || onRent > 0) {
                    c.rollback();
                    return "This car is out on rent. Mark the rental as returned first.";
                }
                if (Car.MAINTENANCE.equals(newStatus) && confirmed > 0) {
                    c.rollback();
                    return "This car has " + confirmed + " confirmed booking"
                         + (confirmed == 1 ? "" : "s") + " waiting for pickup, "
                         + "so it cannot go into maintenance yet.";
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE cars SET status = ? WHERE id = ?")) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, carId);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
}

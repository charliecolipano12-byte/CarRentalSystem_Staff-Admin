package com.mycompany.carrentalforms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter LONG  = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static final String SELECT_WITH_JOINS =
        "SELECT b.id, b.booking_ref, b.pickup_date, b.return_date, b.total, b.status, " +
        "       b.note, b.created_at, b.car_id, " +
        "       c.id AS cust_id, c.full_name, c.phone, c.email, c.license_no, c.completed_rentals, " +
        "       k.model, k.plate " +
        "FROM bookings b " +
        "JOIN customers c ON c.id = b.customer_id " +
        "JOIN cars k      ON k.id = b.car_id ";

    /**
     * All bookings still waiting for staff. Called at app start (and on Refresh) to
     * rebuild the in-memory queue, so a crash or restart loses nothing.
     */
    public List<Booking> getPendingBookings() throws SQLException {
        return query(SELECT_WITH_JOINS + "WHERE b.status = 'Pending' ORDER BY b.created_at", null);
    }

    /** Bookings in a given status, e.g. "Confirmed" (awaiting pickup) or "On Rent". */
    public List<Booking> getByStatus(String status) throws SQLException {
        return query(SELECT_WITH_JOINS + "WHERE b.status = ? ORDER BY b.pickup_date", status);
    }

    /**
     * Creates a Pending booking (used for testing now; the customer app will call it later).
     * The total is computed from the car's daily rate. Returns the new booking_ref.
     */
    public String insertPending(int customerId, int carId,
                                LocalDateTime pickup, LocalDateTime ret) throws SQLException {
        if (!ret.isAfter(pickup)) {
            throw new IllegalArgumentException("Return date must be after pickup date.");
        }
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                double rate;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT daily_rate FROM cars WHERE id = ?")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Car " + carId + " not found");
                        rate = rs.getDouble(1);
                    }
                }
                long days = Math.max(1, (long) Math.ceil(ChronoUnit.HOURS.between(pickup, ret) / 24.0));
                double total = days * rate;

                int newId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO bookings (customer_id, car_id, pickup_date, return_date, total) " +
                        "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, customerId);
                    ps.setInt(2, carId);
                    ps.setTimestamp(3, Timestamp.valueOf(pickup));
                    ps.setTimestamp(4, Timestamp.valueOf(ret));
                    ps.setDouble(5, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getInt(1);
                    }
                }
                String ref = String.format("BK-%04d", newId);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET booking_ref = ? WHERE id = ?")) {
                    ps.setString(1, ref);
                    ps.setInt(2, newId);
                    ps.executeUpdate();
                }
                c.commit();
                return ref;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Moves a booking from one status to another, but ONLY if it is still in the
     * expected status. Returns false if someone else already changed it, so the same
     * booking can never be confirmed twice or confirmed after being rejected.
     *
     * Write to the DB FIRST; only dequeue from the in-memory queue if this returns true.
     *
     * @param staffId nullable (pass 0 or less to leave handled_by untouched)
     */
    public boolean changeStatus(int bookingDbId, String from, String to,
                                int staffId, String note) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, note = COALESCE(?, note), " +
                     "handled_by = IF(? > 0, ?, handled_by) " +
                     "WHERE id = ? AND status = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, to);
            ps.setString(2, note);
            ps.setInt(3, staffId);
            ps.setInt(4, staffId);
            ps.setInt(5, bookingDbId);
            ps.setString(6, from);
            return ps.executeUpdate() == 1;
        }
    }

    public static final String ALREADY_PROCESSED =
            "This booking was already processed (confirmed, rejected or cancelled).";

    /**
     * Confirms a Pending booking, all inside ONE database transaction:
     *   1. lock the booking row and make sure it is still Pending
     *   2. lock the car row (two staff confirming the same car run one after the other)
     *   3. refuse if the car is under maintenance
     *   4. refuse if the car already has a Confirmed / On Rent booking that overlaps these dates
     *   5. set status = Confirmed and record who handled it
     *
     * @return null on success, otherwise a message to show to the staff member.
     *         Write to the DB first; only dequeue from the in-memory queue on success.
     */
    public String confirm(int bookingDbId, int staffId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int carId;
                Timestamp pickup, ret;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT car_id, pickup_date, return_date, status " +
                        "FROM bookings WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, bookingDbId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Booking.PENDING.equals(rs.getString("status"))) {
                            c.rollback();
                            return ALREADY_PROCESSED;
                        }
                        carId  = rs.getInt("car_id");
                        pickup = rs.getTimestamp("pickup_date");
                        ret    = rs.getTimestamp("return_date");
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM cars WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return "The selected car no longer exists.";
                        }
                        if (Car.MAINTENANCE.equals(rs.getString("status"))) {
                            c.rollback();
                            return "This car is under maintenance and cannot be booked. "
                                 + "Reject this request instead.";
                        }
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT booking_ref FROM bookings " +
                        "WHERE car_id = ? AND id <> ? AND status IN ('Confirmed','On Rent') " +
                        "AND pickup_date < ? AND return_date > ? LIMIT 1")) {
                    ps.setInt(1, carId);
                    ps.setInt(2, bookingDbId);
                    ps.setTimestamp(3, ret);
                    ps.setTimestamp(4, pickup);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            c.rollback();
                            return "This car is already booked for overlapping dates (" +
                                   rs.getString(1) + "). Reject this request, or ask the " +
                                   "customer to choose other dates.";
                        }
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET status = 'Confirmed', handled_by = IF(? > 0, ?, handled_by) " +
                        "WHERE id = ? AND status = 'Pending'")) {
                    ps.setInt(1, staffId);
                    ps.setInt(2, staffId);
                    ps.setInt(3, bookingDbId);
                    if (ps.executeUpdate() != 1) {
                        c.rollback();
                        return ALREADY_PROCESSED;
                    }
                }
                c.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    // ================================================================ Phase 3: rentals

    /** Result of {@link #markReturned(int)}. problem == null means success. */
    public static final class ReturnResult {
        public final String problem;
        public final int completedRentals;   // the customer's new total
        public final boolean becameVip;      // true if this return just made them a VIP
        ReturnResult(String problem, int completedRentals, boolean becameVip) {
            this.problem = problem;
            this.completedRentals = completedRentals;
            this.becameVip = becameVip;
        }
    }

    /**
     * Customer collected the car: booking Confirmed -> On Rent, car Available -> Rented.
     * Refuses if the car is still out with a previous customer or in maintenance.
     * @return null on success, otherwise a message for the staff member
     */
    public String markPickedUp(int bookingDbId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int carId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT car_id, status FROM bookings WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, bookingDbId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Booking.CONFIRMED.equals(rs.getString("status"))) {
                            c.rollback();
                            return "This booking is no longer waiting for collection. Press Refresh.";
                        }
                        carId = rs.getInt("car_id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM cars WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return "The car no longer exists."; }
                        String carStatus = rs.getString("status");
                        if (!Car.AVAILABLE.equals(carStatus)) {
                            c.rollback();
                            return "This car is currently marked as " + carStatus + ". Mark the "
                                 + "previous rental as returned (or finish maintenance) first.";
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET status = 'On Rent' WHERE id = ? AND status = 'Confirmed'")) {
                    ps.setInt(1, bookingDbId);
                    if (ps.executeUpdate() != 1) { c.rollback(); return "Booking was changed by someone else. Press Refresh."; }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE cars SET status = 'Rented' WHERE id = ?")) {
                    ps.setInt(1, carId);
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

    /**
     * Car came back: booking On Rent -> Completed, car -> Available, and the customer's
     * completed_rentals goes up by 1 (this is what eventually makes them a VIP).
     * All four changes happen in ONE transaction: either everything is saved or nothing.
     */
    public ReturnResult markReturned(int bookingDbId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int carId, customerId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT car_id, customer_id, status FROM bookings WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, bookingDbId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Booking.ON_RENT.equals(rs.getString("status"))) {
                            c.rollback();
                            return new ReturnResult("This booking is not currently on rent. Press Refresh.", 0, false);
                        }
                        carId = rs.getInt("car_id");
                        customerId = rs.getInt("customer_id");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET status = 'Completed' WHERE id = ? AND status = 'On Rent'")) {
                    ps.setInt(1, bookingDbId);
                    if (ps.executeUpdate() != 1) {
                        c.rollback();
                        return new ReturnResult("Booking was changed by someone else. Press Refresh.", 0, false);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE cars SET status = 'Available' WHERE id = ? AND status = 'Rented'")) {
                    ps.setInt(1, carId);
                    ps.executeUpdate();   // left alone if staff already put it in Maintenance
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE customers SET completed_rentals = completed_rentals + 1 WHERE id = ?")) {
                    ps.setInt(1, customerId);
                    ps.executeUpdate();
                }
                int total;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT completed_rentals FROM customers WHERE id = ?")) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getInt(1); }
                }
                c.commit();
                return new ReturnResult(null, total, total == Customer.VIP_THRESHOLD);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Adds days to a rental that is On Rent. The extra days are charged at the car's daily
     * rate and added to the total. Refused if another Confirmed / On Rent booking for the
     * same car starts inside the extra days.
     * @return null on success, otherwise a message for the staff member
     */
    public String extend(int bookingDbId, int extraDays) throws SQLException {
        if (extraDays < 1 || extraDays > 30) {
            throw new IllegalArgumentException("Extra days must be between 1 and 30.");
        }
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int carId;
                LocalDateTime oldReturn;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT car_id, return_date, status FROM bookings WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, bookingDbId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !Booking.ON_RENT.equals(rs.getString("status"))) {
                            c.rollback();
                            return "Only a booking that is currently on rent can be extended. Press Refresh.";
                        }
                        carId = rs.getInt("car_id");
                        oldReturn = rs.getTimestamp("return_date").toLocalDateTime();
                    }
                }
                double rate;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT daily_rate FROM cars WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, carId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); rate = rs.getDouble(1); }
                }
                LocalDateTime newReturn = oldReturn.plusDays(extraDays);
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT booking_ref FROM bookings " +
                        "WHERE car_id = ? AND id <> ? AND status IN ('Confirmed','On Rent') " +
                        "AND pickup_date < ? AND return_date > ? LIMIT 1")) {
                    ps.setInt(1, carId);
                    ps.setInt(2, bookingDbId);
                    ps.setTimestamp(3, Timestamp.valueOf(newReturn));
                    ps.setTimestamp(4, Timestamp.valueOf(oldReturn));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            c.rollback();
                            return "Cannot extend: this car is already booked by " + rs.getString(1)
                                 + " during those extra days. Try fewer days.";
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET return_date = ?, total = total + ? WHERE id = ? AND status = 'On Rent'")) {
                    ps.setTimestamp(1, Timestamp.valueOf(newReturn));
                    ps.setDouble(2, extraDays * rate);
                    ps.setInt(3, bookingDbId);
                    if (ps.executeUpdate() != 1) { c.rollback(); return "Booking was changed by someone else. Press Refresh."; }
                }
                c.commit();
                return null;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }
    
    public List<Booking> getHistory() throws SQLException {
        String sql =
            "SELECT b.id, b.booking_ref, b.pickup_date, b.return_date, b.total, b.status, " +
            "       b.note, b.created_at, b.car_id, " +
            "       c.id AS cust_id, c.full_name, c.phone, c.email, c.license_no, c.completed_rentals, " +
            "       k.model, k.plate, s.full_name AS handler " +
            "FROM bookings b " +
            "JOIN customers c ON c.id = b.customer_id " +
            "JOIN cars k      ON k.id = b.car_id " +
            "LEFT JOIN staff s ON s.id = b.handled_by " +
            "WHERE b.status IN ('Completed','Rejected','Cancelled') " +
            "ORDER BY b.created_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Booking b = map(rs);
                b.setHandledBy(rs.getString("handler"));
                list.add(b);
            }
        }
        return list;
    }

    // ---------------------------------------------------------------- helpers

    private List<Booking> query(String sql, String param) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Booking map(ResultSet rs) throws SQLException {
        Customer cust = new Customer();
        cust.setId(rs.getInt("cust_id"));
        cust.setFullName(rs.getString("full_name"));
        cust.setPhone(rs.getString("phone"));
        cust.setEmail(rs.getString("email"));
        cust.setLicenseNo(rs.getString("license_no"));
        cust.setCompletedRentals(rs.getInt("completed_rentals"));

        LocalDateTime pickup = rs.getTimestamp("pickup_date").toLocalDateTime();
        LocalDateTime ret    = rs.getTimestamp("return_date").toLocalDateTime();
        String dates   = pickup.format(SHORT) + " - " + ret.format(LONG);
        String vehicle = rs.getString("model") + " (" + rs.getString("plate") + ")";

        Booking b = new Booking(rs.getString("booking_ref"), cust, vehicle, dates,
                rs.getDouble("total"), rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime());
        b.setDbId(rs.getInt("id"));
        b.setCarId(rs.getInt("car_id"));
        b.setPickup(pickup);
        b.setReturnDate(ret);
        b.setNote(rs.getString("note"));
        return b;
    }
}

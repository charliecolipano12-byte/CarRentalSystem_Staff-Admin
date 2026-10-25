package com.mycompany.carrentalforms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    /** Adds 1 to completed_rentals and returns the new count (or -1 if no such customer). */
    public int incrementCompletedRentals(int customerId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement up = c.prepareStatement(
                     "UPDATE customers SET completed_rentals = completed_rentals + 1 WHERE id = ?");
                 PreparedStatement sel = c.prepareStatement(
                     "SELECT completed_rentals FROM customers WHERE id = ?")) {
                up.setInt(1, customerId);
                if (up.executeUpdate() == 0) { c.rollback(); return -1; }
                sel.setInt(1, customerId);
                try (ResultSet rs = sel.executeQuery()) {
                    int n = rs.next() ? rs.getInt(1) : -1;
                    c.commit();
                    return n;
                }
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}

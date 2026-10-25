/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.carrentalforms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author charl
 */
public class StaffDAO {
      
     /** @return the Staff if username + password are correct, otherwise null. */
    public Staff authenticate(String username, char[] password) throws SQLException {
        String sql = "SELECT id, username, full_name, salt, password_hash, role " +
                     "FROM staff WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    PasswordUtil.hash("00000000", password);   // similar timing for unknown users
                    return null;
                }
                String actual = PasswordUtil.hash(rs.getString("salt"), password);
                if (!PasswordUtil.matches(rs.getString("password_hash"), actual)) {
                    return null;
                }
                return new Staff(rs.getInt("id"), rs.getString("username"),
                                 rs.getString("full_name"), rs.getString("role"));
            }
        }
    }
}

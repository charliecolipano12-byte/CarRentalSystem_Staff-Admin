/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.carrentalsystem_staff;


import com.mycompany.carrentalforms.Booking;
import com.mycompany.carrentalforms.BookingDAO;
import com.mycompany.carrentalforms.BookingQueue;
import com.mycompany.carrentalforms.Database;
import com.mycompany.carrentalforms.StaffLogin;
import java.sql.SQLException;
import javax.swing.UnsupportedLookAndFeelException;


public class CarRentalSystem_Staff {
     
    private static final java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(CarRentalSystem_Staff.class.getName());

    public static void main(String[] args) throws SQLException {
         
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, (UnsupportedLookAndFeelException) ex);
        }
        
       StaffLogin login = new StaffLogin();
       login.pack();
       login.setLocationRelativeTo(null);
       login.setVisible(true);
       
       System.out.println("DB reachable: " + Database.isReachable());
    BookingQueue q = new BookingQueue();
    q.rebuild(new BookingDAO().getPendingBookings());
    for (Booking b : q.toSortedList())
    System.out.println(b.getId() + "  " + b.getCustomer().getFullName()
        + (b.getCustomer().isVip() ? "  [VIP]" : ""));
    }
}

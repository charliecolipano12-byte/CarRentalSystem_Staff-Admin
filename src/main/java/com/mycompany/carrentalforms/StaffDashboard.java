/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.carrentalforms;

import java.awt.CardLayout;
import java.awt.Color;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author charl
 */
public class StaffDashboard extends javax.swing.JFrame {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StaffDashboard.class.getName());
    private TableRowSorter<TableModel> fleetSorter;
    private TableRowSorter<TableModel> historySorter;

    
    private static final int COL_PLATE  = 0;
    private static final int COL_MODEL  = 1;
    private static final int COL_TYPE   = 2;
    private static final int COL_STATUS = 5;
    
    private void panelBorderColor(){
    Color containerLine = new Color(70, 20, 25);    
    Color cardLine      = new Color(110, 22, 30);
        
       // big containers
    JPanel[] containers = {
        cardPanel, cardHeaderPanel, jPanel2
    };

    // stat cards
    JPanel[] cards = {
        statsTotalVehiclePanel, statsAvailableVehiclePanel, statsReservedVehiclePanel,
        statsOnRentVehiclePanel, statsInMaintenancePanel,
        statsInQueuePanel, statsConfirmedPanel, statsActivePanel,
        statsCompletedPanel, statsRejectedPanel,
        statsRevenuePanel, statsAveRatePanel,
        statsQueueLengthPanel, statsLongestWaitTimePanel
    };

    for (int i = 0; i < containers.length; i++) {
        RoundedUI.RoundedPanel p = (RoundedUI.RoundedPanel) containers[i];
        p.setBorderColor(containerLine);
        p.setBorderWidth(1.5f);
    }

    for (int i = 0; i < cards.length; i++) {
        RoundedUI.RoundedPanel p = (RoundedUI.RoundedPanel) cards[i];
        p.setBorderColor(cardLine);
        p.setBorderWidth(1.5f);
    }
}
     
    private void panelBackgroundColor(){
       cardHeaderPanel.setBackground(new Color(20,9,11,190));
       cardPanel.setBackground(new Color(20, 9, 11, 190));
       jPanel2.setBackground(new Color(20, 9, 11, 200));
       rentalTabMiddlePanel.setBackground(new Color(20, 9, 11, 190));
       
       Color cardFill = new Color(24,10,13,190);
       
       JPanel[] cards = {
            statsTotalVehiclePanel, statsAvailableVehiclePanel, statsReservedVehiclePanel,
        statsOnRentVehiclePanel, statsInMaintenancePanel,
        statsInQueuePanel, statsConfirmedPanel, statsActivePanel,
        statsCompletedPanel, statsRejectedPanel, statsRevenuePanel, statsAveRatePanel,
        statsQueueLengthPanel, statsLongestWaitTimePanel};
       
       for (int i = 0; i < cards.length; i++) {
        cards[i].setBackground(cardFill);
    }
    }
    
    private void toggleButtonBorderColor(){
    Color tabLine     = new Color(150, 20, 30);    // unselected border
    Color tabFill     = new Color(30, 8, 11);      // unselected fill (dark)
    Color tabText     = new Color(240, 240, 240);  // unselected text
    Color activeRed   = new Color(215, 25, 40);    // selected border and fill

    RoundedUI.RoundedToggleButton[] tabs = {
        (RoundedUI.RoundedToggleButton) tabQueue,
        (RoundedUI.RoundedToggleButton) tabFleet,
        (RoundedUI.RoundedToggleButton) tabRentals,
        (RoundedUI.RoundedToggleButton) tabHistory,
        (RoundedUI.RoundedToggleButton) tabDashboard
    };

    for (int t = 0; t < tabs.length; t++) {
        tabs[t].setNormalColors(tabLine, tabFill, tabText);              // unselected
        tabs[t].setSelectedColors(activeRed, activeRed, Color.WHITE);    // selected
    }
}
    
   private void buttonColorBorder(){
    Color fill    = new Color(40, 8, 12);      // dark red-black fill
    Color hover   = new Color(70, 12, 20);     // slightly lighter when hovering
    Color outline = new Color(190, 30, 45);    // red outline
    Color text    = new Color(240, 80, 90); 
    Color whiteText = new Color(255,255,240);
    signOutButton.setForeground(text);
    
    JButton[] buttons ={
        signOutButton,btnrefreshQueue,btnfleetAddVehicle,
        btnFleetTableRefresh, btnFleetStatus,btnEditFleet,  
        btnDeleteFleet,btnHistoryTableRefresh,
        btnRentalsRefresh, btnNextQueue, btnStatusReturned,
        btnStatusPickedUp, btnStatusExtend, btnClearDate
    };
    
     for (int i = 0; i < buttons.length; i++) {
         RoundedUI.RoundedButton p = (RoundedUI.RoundedButton) buttons[i];
        p.setColors(fill,hover,outline);
        p.setForeground(whiteText);
    }  
}
   
   private void ButtonColor(){
       Color colorfill = new Color(215, 25, 40);
       Color hoverfill = new Color (175, 15, 30);
       
    ((RoundedUI.RoundedButton) refreshButton).setColors(
            new Color(215, 25, 40),     // fill
            new Color(175, 15, 30),     // hover fill
            null);                      // no outline
    refreshButton.setForeground(Color.WHITE);
    
     ((RoundedUI.RoundedButton) btnrefreshQueue).setColors(colorfill,hoverfill,null);
        btnrefreshQueue.setForeground(Color.WHITE);
    
}
   
   private final BookingQueue queue = new BookingQueue();
private final BookingDAO bookingDAO = new BookingDAO();
private java.util.List<Booking> queueRows = new java.util.ArrayList<>();

/** Used by the VIP cell renderer: is the booking shown on this table row a VIP's? */
private boolean isVip(int row) {
    int m = queueTable.convertRowIndexToModel(row);
    return m >= 0 && m < queueRows.size()
            && queueRows.get(m).getCustomer().isVip();
}

/** Reloads pending bookings from MySQL into the queue and redraws the table. */
/** Reloads pending bookings from MySQL into the queue and redraws the table. */
private void loadQueue() {
    try {
        queue.rebuild(bookingDAO.getPendingBookings());
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Could not load the queue:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    redrawQueue();
}

/** Redraws the table from the in-memory queue (no database call). */
private void redrawQueue() {
    queueRows = queue.toSortedList();
    javax.swing.table.DefaultTableModel m =
            (javax.swing.table.DefaultTableModel) queueTable.getModel();
    m.setRowCount(0);
    int n = 1;
    for (Booking b : queueRows) {
        long mins = java.time.Duration.between(
                b.getTimestamp(), java.time.LocalDateTime.now()).toMinutes();
        m.addRow(new Object[]{
            n++, b.getId(), b.getCustomer().getFullName(), b.getVehicle(),
            b.getDates(), String.format("PHP %,.2f", b.getTotal()),
            mins + " min", b.getStatus(),
            b.getCustomer().isVip() ? "VIP" : "Regular"
                
            
        });
        
    }
    statsDisplayTotalVehicles1.setText(queue.size() + " Waiting");
}

private java.util.List<Booking> awaitingRows = new java.util.ArrayList<>();
private java.util.List<Booking> onRentRows   = new java.util.ArrayList<>();
private static final java.time.format.DateTimeFormatter RENT_FMT =
        java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a");

/** Reloads both Rentals tables from MySQL. */
private void loadRentals() {
    try {
        awaitingRows = bookingDAO.getByStatus(Booking.CONFIRMED);
        onRentRows   = bookingDAO.getByStatus(Booking.ON_RENT);
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Could not load rentals:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    fillRentalTable(awaitingTable, awaitingRows);
    fillRentalTable(onRentTable, onRentRows);
}

          private final CarDAO carDAO = new CarDAO();
    private java.util.List<Car> fleetRows = new java.util.ArrayList<>();
 
    /** Reloads the fleet table from MySQL. */
    private void loadFleet() {
        try {
            fleetRows = carDAO.getCars();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load the fleet:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        javax.swing.table.DefaultTableModel m =
                (javax.swing.table.DefaultTableModel) fleetTable.getModel();
        m.setRowCount(0);
        for (Car c : fleetRows) {
            // column 3 ("Year") has no database column, so it stays empty and is hidden in step 3
            m.addRow(new Object[]{ c.getPlate(), c.getModel(), c.getType(), "",
                    String.format("PHP %,.2f", c.getDailyRate()), c.getStatus() });
        }
    }
 
    /** The car on the selected row (works with the search/filter sorter), or null + a message. */
    private Car selectedCar() {
        int view = fleetTable.getSelectedRow();
        if (view < 0) {
            JOptionPane.showMessageDialog(this, "Select a car in the table first.",
                    "No car selected", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int model = fleetTable.convertRowIndexToModel(view);
        return model < fleetRows.size() ? fleetRows.get(model) : null;
    }
 
    private void showFleetResult(String problem, String title) {
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, title, JOptionPane.WARNING_MESSAGE);
        }
    }
 
    private void addCar() {
        CarDialog.Result r = CarDialog.show(this, null);
        if (r == null) return;
        try {
            showFleetResult(carDAO.addCar(r.plate, r.model, r.type, r.rate), "Cannot add car");
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        loadFleet();
    }
 
    private void editSelectedCar() {
        Car car = selectedCar();
        if (car == null) return;
        CarDialog.Result r = CarDialog.show(this, car);
        if (r == null) return;
        try {
            showFleetResult(carDAO.updateCar(car.getId(), r.plate, r.model, r.type, r.rate),
                    "Cannot save changes");
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        loadFleet();
    }
 
    private void deleteSelectedCar() {
        Car car = selectedCar();
        if (car == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete " + car.getModel() + " (" + car.getPlate() + ")?\nThis cannot be undone.",
                "Delete car", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            showFleetResult(carDAO.deleteCar(car.getId()), "Cannot delete car");
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        loadFleet();
    }
 
    private void changeSelectedCarStatus() {
        Car car = selectedCar();
        if (car == null) return;
        String[] options = { Car.AVAILABLE, Car.MAINTENANCE };
        Object pick = JOptionPane.showInputDialog(this,
                car.getModel() + " (" + car.getPlate() + ") is currently " + car.getStatus()
                + ".\nSet it to:", "Change car status", JOptionPane.QUESTION_MESSAGE,
                null, options, car.isAvailable() ? Car.MAINTENANCE : Car.AVAILABLE);
        if (pick == null) return;
        try {
            showFleetResult(carDAO.setStatus(car.getId(), pick.toString()), "Cannot change status");
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
        loadFleet();
    }

private void fillRentalTable(javax.swing.JTable table, java.util.List<Booking> rows) {
    javax.swing.table.DefaultTableModel m =
            (javax.swing.table.DefaultTableModel) table.getModel();
    m.setRowCount(0);
    for (Booking b : rows) {
        m.addRow(new Object[]{
            b.getId(), b.getCustomer().getFullName(),
            b.getCustomer().isVip() ? "Yes" : "No", b.getVehicle(),
            b.getPickup().format(RENT_FMT), b.getReturnDate().format(RENT_FMT),
            String.format("PHP %,.2f", b.getTotal())
        });
    }
}

/** The booking on the selected row, or null (after telling the user) if none is selected. */
private Booking selectedBooking(javax.swing.JTable table, java.util.List<Booking> rows,
                                String tableName) {
    int v = table.getSelectedRow();
    if (v < 0) {
        JOptionPane.showMessageDialog(this,
                "Select a booking in the \"" + tableName + "\" table first.",
                "No booking selected", JOptionPane.INFORMATION_MESSAGE);
        return null;
    }
    return rows.get(table.convertRowIndexToModel(v));
}

private void pickedUpSelected() {
    Booking b = selectedBooking(awaitingTable, awaitingRows, "Awaiting on collection");
    if (b == null) return;
    int ok = JOptionPane.showConfirmDialog(this,
            b.getCustomer().getFullName() + " is collecting " + b.getVehicle()
            + ".\nMark booking " + b.getId() + " as picked up?",
            "Mark picked up", JOptionPane.YES_NO_OPTION);
    if (ok != JOptionPane.YES_OPTION) return;
    try {
        String problem = bookingDAO.markPickedUp(b.getDbId());
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Cannot mark picked up",
                    JOptionPane.WARNING_MESSAGE);
        }
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
    loadRentals();
}

private void extendSelected() {
    Booking b = selectedBooking(onRentTable, onRentRows, "Currently on rent");
    if (b == null) return;
    String in = JOptionPane.showInputDialog(this,
            "Extend booking " + b.getId() + " by how many days? (1 to 30)",
            "Extend rental", JOptionPane.QUESTION_MESSAGE);
    if (in == null) return;
    int days;
    try { days = Integer.parseInt(in.trim()); } catch (NumberFormatException e) { days = -1; }
    if (days < 1 || days > 30) {
        JOptionPane.showMessageDialog(this, "Enter a whole number from 1 to 30.",
                "Invalid number of days", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        String problem = bookingDAO.extend(b.getDbId(), days);
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Cannot extend",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Extended by " + days + (days == 1 ? " day." : " days.")
                    + "\nNew return: " + b.getReturnDate().plusDays(days).format(RENT_FMT),
                    "Rental extended", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
    loadRentals();
}
   
   private void TableDesign(){
       RoundedUI.styleTable(queueTable, queueTablePane);
       RoundedUI.styleTable(awaitingTable, awaitingScroll);
       RoundedUI.styleTable(onRentTable, onRentScroll);
       RoundedUI.styleTable(fleetTable, fleetTablePane);
       RoundedUI.styleTable(historyTable, historyTablePane);
       RoundedUI.styleStatusColumn(queueTable, 7);
       queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       queueTable.getTableHeader().setReorderingAllowed(false);
       queueTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        queueTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        queueTable.getColumnModel().getColumn(2).setPreferredWidth(160);   // fits "★ VIP  Customer Name"
        queueTable.getColumnModel().getColumn(3).setPreferredWidth(180);   // vehicle
        queueTable.getColumnModel().getColumn(4).setPreferredWidth(150);   // dates
       
     queueTable.getColumnModel().getColumn(2).setCellRenderer(
    new javax.swing.table.DefaultTableCellRenderer() {
        private final Color rowColor = new Color(24, 10, 13);   // same as RoundedUI
        private final Color altColor = new Color(30, 12, 16);

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean sel, boolean focus, int row, int col) {
            javax.swing.JLabel l = (javax.swing.JLabel) super.getTableCellRendererComponent(
                    table, value, sel, false, row, col);
            l.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (!sel) l.setBackground(row % 2 == 0 ? rowColor : altColor);

            if (isVip(row)) {
                l.setText("★ VIP  " + value);
                if (!sel) l.setForeground(new Color(212, 175, 55));          // gold
                l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD));
            } else {
                l.setText(String.valueOf(value));
                if (!sel) l.setForeground(new Color(240, 240, 240));         // same light text as other columns
                l.setFont(l.getFont().deriveFont(java.awt.Font.PLAIN));
            }
            return l;
        }
    });
   }
 
   
    public StaffDashboard() {
        initComponents();
        
       panelBorderColor();
       toggleButtonBorderColor();
       buttonColorBorder();
       panelBackgroundColor();
       ButtonColor();
       TableDesign();
       initFleetFilter();
       comboBoxUI();
       attachSearch();
       setupDateFilter();
       loadHistory();
       widenStatLabel(statsDisplayRevenue,  statsRevenuePanel);
       widenStatLabel(statsDisplayAveRate,  statsAveRatePanel);
       widenStatLabel(statsDisplayWaitTime, statsLongestWaitTimePanel);
       loadDashboard();
       historySearch.setCaretColor(Color.WHITE);
       loadQueue();
       loadRentals();
       btnRentalsRefresh.addActionListener(e -> loadRentals());
       btnStatusPickedUp.addActionListener(e -> pickedUpSelected());
       btnStatusExtend.addActionListener(e -> extendSelected());
       loadFleet();
       fleetTable.removeColumn(fleetTable.getColumnModel().getColumn(3));
       btnfleetAddVehicle.addActionListener(e -> addCar());
       btnEditFleet.addActionListener(e -> editSelectedCar());
       btnFleetStatus.addActionListener(e -> changeSelectedCarStatus());        btnFleetTableRefresh.addActionListener(e -> loadFleet());
       btnFleetStatus.setText("Set status");
    }
    
    private Staff currentStaff;
    
    public StaffDashboard(Staff staff) {
    this();                                        // runs the setup above first
    this.currentStaff = staff;
    jLabel5.setText(staff.getFullName() + " -");   // safe now: components already exist
}

public Staff getCurrentStaff() { return currentStaff; }
    
    
    private void comboBoxUI(){
    RoundedUI.styleComboBox(cmbFleetStatus);
    RoundedUI.styleComboBox(cmbCarType);
    RoundedUI.styleComboBox(cmbFleetStatus1);
    }
    
    private void initFleetFilter() {
        fleetSorter = new TableRowSorter<>(fleetTable.getModel());
        fleetTable.setRowSorter(fleetSorter);

        fleetSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFleetFilters(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFleetFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFleetFilters(); }
        });

        cmbFleetStatus.addActionListener(e -> applyFleetFilters());
        cmbCarType.addActionListener(e -> applyFleetFilters());
    }
    private void applyFleetFilters() {
        java.util.List<RowFilter<TableModel, Integer>> filters = new java.util.ArrayList<>();

        String text = fleetSearch.getText().trim();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter(
                "(?i)" + Pattern.quote(text), COL_PLATE, COL_MODEL));
        }

        Object status = cmbFleetStatus.getSelectedItem();
        if (status != null && !status.toString().trim().equals("All")) {
            filters.add(RowFilter.regexFilter(
                "^" + Pattern.quote(status.toString().trim()) + "$", COL_STATUS));
        }
        Object type = cmbCarType.getSelectedItem(); 
        if (type != null && !type.toString().trim().equals("All")
                && !type.toString().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter(
                "^" + Pattern.quote(type.toString().trim()) + "$", COL_TYPE));
        }

        fleetSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
    private void attachSearch(){
        RoundedUI.attachSearch(queueSearch, queueTable);
    }
       
    private java.util.List<Booking> historyRows = new java.util.ArrayList<>();
    private boolean dateFilterOn = false;       // stays off until the staff member picks a date
    private boolean historyAdjusting = false;   // true while Clear is resetting the controls
 
     /** Reloads Completed / Rejected / Cancelled bookings from MySQL. */
    private void loadHistory() {
        try {
            historyRows = bookingDAO.getHistory();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load the history:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        javax.swing.table.DefaultTableModel m =
                (javax.swing.table.DefaultTableModel) historyTable.getModel();
        m.setRowCount(0);
        for (Booking b : historyRows) {
            m.addRow(new Object[]{
                b.getId(), b.getCustomer().getFullName(), b.getVehicle(), b.getDates(),
                String.format("PHP %,.2f", b.getTotal()), b.getStatus(),
                b.getHandledBy() == null ? "-" : b.getHandledBy(),
                b.getNote() == null ? "" : b.getNote()
            });
        }
    }
    
     private final StatsDAO statsDAO = new StatsDAO();
    private void loadDashboard() {
        StatsDAO.Stats s;
        try {
            s = statsDAO.load();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load the statistics:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        statsDisplayTotalVehicles.setText(String.valueOf(s.totalCars));
        statsDisplayAvailable.setText(String.valueOf(s.available));
        statsDisplayReserved.setText(String.valueOf(s.reserved));
        statsDisplayOnRent.setText(String.valueOf(s.onRent));
        statsDisplayInMaintenance.setText(String.valueOf(s.maintenance));
 
        statsDisplayRegularQueue.setText(String.valueOf(s.regularPending));
        statsDisplayVipQueue.setText(String.valueOf(s.vipPending));
        statsDisplayConfirmed.setText(String.valueOf(s.confirmed));
        statsDisplayActive.setText(String.valueOf(s.active));
        statsDisplayCompleted.setText(String.valueOf(s.completed));
        statsDisplayRejected.setText(String.valueOf(s.rejectedOrCancelled));
 
        statsDisplayRevenue.setText(compactMoney(s.revenueBooked));
        statsDisplayRevenue.setToolTipText(String.format("PHP %,.2f", s.revenueBooked));
        statsDisplayAveRate.setText(compactMoney(s.avgDailyRate));
        statsDisplayAveRate.setToolTipText(String.format("PHP %,.2f", s.avgDailyRate));
        statsDisplayQueueLength.setText(String.valueOf(s.pending));
        statsDisplayWaitTime.setText(waitText(s.oldestPending));
    }
    
         /** PHP 950 / PHP 45.2K / PHP 1.2M - the card is narrow, the tooltip has the exact amount. */
    private static String compactMoney(double v) {
        if (v >= 1_000_000) return String.format("PHP %.1fM", v / 1_000_000);
        if (v >= 10_000)    return String.format("PHP %.1fK", v / 1_000);
        return String.format("PHP %,.0f", v);
    }
 
    /** "None", "12 min", "3 h 5 min" or "2 d 4 h" for the booking that has waited longest. */
    private static String waitText(java.time.LocalDateTime since) {
        if (since == null) return "None";
        long mins = Math.max(0, java.time.Duration.between(since, java.time.LocalDateTime.now()).toMinutes());
        if (mins < 60)        return mins + " min";
        if (mins < 60 * 24)   return (mins / 60) + " h " + (mins % 60) + " min";
        return (mins / (60 * 24)) + " d " + ((mins / 60) % 24) + " h";
    }
    
     private void widenStatLabel(javax.swing.JLabel label, javax.swing.JPanel panel) {
        panel.remove(label);
        panel.add(label, new org.netbeans.lib.awtextra.AbsoluteConstraints(
                70, 20, Math.max(60, panel.getPreferredSize().width - 80), 40));
    }
     
 
    /** One sorter for the whole History tab: search + status + date range + VIP only. */
    private void setupDateFilter() {
        historySorter = new TableRowSorter<>(historyTable.getModel());
        historyTable.setRowSorter(historySorter);
 
        // this combo was copied from the Fleet tab, so give it the History statuses
        cmbFleetStatus1.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "All", "Completed", "Rejected", "Cancelled" }));
 
        spnFrom.setModel(new SpinnerDateModel());
        spnTo.setModel(new SpinnerDateModel());
        spnFrom.setEditor(new JSpinner.DateEditor(spnFrom, "yyyy-MM-dd"));
        spnTo.setEditor(new JSpinner.DateEditor(spnTo, "yyyy-MM-dd"));
        RoundedUI.styleSpinner(spnFrom);
        RoundedUI.styleSpinner(spnTo);
 
        spnFrom.addChangeListener(e -> { if (!historyAdjusting) { dateFilterOn = true; applyHistoryFilters(); } });
        spnTo.addChangeListener(e   -> { if (!historyAdjusting) { dateFilterOn = true; applyHistoryFilters(); } });
        cmbFleetStatus1.addActionListener(e -> applyHistoryFilters());
        vipOnlyFilter.addActionListener(e -> applyHistoryFilters());
        historySearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyHistoryFilters(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyHistoryFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyHistoryFilters(); }
        });
        btnClearDate.addActionListener(e -> clearHistoryFilters());
        btnHistoryTableRefresh.addActionListener(e -> loadHistory());
    }
 
    private void clearHistoryFilters() {
        historyAdjusting = true;
        Date now = new Date();
        spnFrom.setValue(now);
        spnTo.setValue(now);
        cmbFleetStatus1.setSelectedIndex(0);
        vipOnlyFilter.setSelected(false);
        historySearch.setText("");
        historyAdjusting = false;
        dateFilterOn = false;
        applyHistoryFilters();
    }
 
    /** The date range is checked against the booking's PICKUP date. */
    private void applyHistoryFilters() {
        final String text = historySearch.getText().trim().toLowerCase();
        Object st = cmbFleetStatus1.getSelectedItem();
        final String status = (st == null || st.toString().trim().equals("All"))
                ? null : st.toString().trim();
        final boolean vipOnly = vipOnlyFilter.isSelected();
        final boolean useDate = dateFilterOn;
        LocalDate d1 = toLocalDate((Date) spnFrom.getValue());
        LocalDate d2 = toLocalDate((Date) spnTo.getValue());
        final LocalDate from = d1.isAfter(d2) ? d2 : d1;    // tolerate From > To
        final LocalDate to   = d1.isAfter(d2) ? d1 : d2;
 
        historySorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                int idx = entry.getIdentifier();                 // row index in the model
                if (idx < 0 || idx >= historyRows.size()) return false;
                Booking bk = historyRows.get(idx);
                if (status != null && !status.equals(bk.getStatus())) return false;
                if (vipOnly && !bk.getCustomer().isVip()) return false;
                if (useDate) {
                    LocalDate d = bk.getPickup().toLocalDate();
                    if (d.isBefore(from) || d.isAfter(to)) return false;
                }
                if (!text.isEmpty()) {
                    boolean hit = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(text)) { hit = true; break; }
                    }
                    if (!hit) return false;
                }
                return true;
            }
        });
    }
  
    private LocalDate toLocalDate(Date d) {
    return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
}


    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        signOutButton = new RoundedUI.RoundedButton(12)
        ;
        jLabel5 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new RoundedUI.RoundedPanel(15);
        tabDashboard = new RoundedUI.RoundedToggleButton(12);
        tabRentals = new RoundedUI.RoundedToggleButton(12)
        ;
        tabQueue = new RoundedUI.RoundedToggleButton(12);
        tabHistory = new RoundedUI.RoundedToggleButton(12);
        tabFleet = new RoundedUI.RoundedToggleButton(12);
        cardHeaderPanel = new RoundedUI.RoundedPanel(15);
        dashboardHeaderPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        refreshButton = new RoundedUI.RoundedButton(12)
        ;
        rentalsHeaderPanel = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        btnRentalsRefresh = new RoundedUI.RoundedButton(12)
        ;
        queueHeaderPanel = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        btnrefreshQueue = new RoundedUI.RoundedButton(12)
        ;
        statsDisplayTotalVehicles1 = new javax.swing.JLabel();
        queueSearch = new RoundedUI.SearchField("Search Queue....");
        historyHeaderPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        historySearch = new RoundedUI.SearchField("Search Bookings....");
        jLabel9 = new javax.swing.JLabel();
        btnHistoryTableRefresh = new RoundedUI.RoundedButton(12);
        cmbFleetStatus1 = new javax.swing.JComboBox<>();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        spnTo = new javax.swing.JSpinner();
        spnFrom = new javax.swing.JSpinner();
        jLabel53 = new javax.swing.JLabel();
        btnClearDate = new RoundedUI.RoundedButton(8);
        vipOnlyFilter = new javax.swing.JCheckBox();
        fleetHeaderPanel = new javax.swing.JPanel();
        jLabel48 = new javax.swing.JLabel();
        btnfleetAddVehicle = new RoundedUI.RoundedButton(12);
        btnFleetTableRefresh = new RoundedUI.RoundedButton(12);
        cmbCarType = new javax.swing.JComboBox<>();
        cmbFleetStatus = new javax.swing.JComboBox<>();
        jLabel50 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        fleetSearch = new RoundedUI.SearchField("Search Fleet....");
        cardPanel = new RoundedUI.RoundedPanel(15);
        dashboardPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        statsTotalVehiclePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel15 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        statsDisplayTotalVehicles = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        statsAvailableVehiclePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel16 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        statsDisplayAvailable = new javax.swing.JLabel();
        statsReservedVehiclePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel17 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        statsDisplayReserved = new javax.swing.JLabel();
        statsOnRentVehiclePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel18 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        statsDisplayOnRent = new javax.swing.JLabel();
        statsInMaintenancePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel19 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        statsDisplayInMaintenance = new javax.swing.JLabel();
        statsInQueuePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel20 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        statsDisplayRegularQueue = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        statsDisplayVipQueue = new javax.swing.JLabel();
        statsConfirmedPanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel21 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        statsDisplayConfirmed = new javax.swing.JLabel();
        statsActivePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel22 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        statsDisplayActive = new javax.swing.JLabel();
        statsCompletedPanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel23 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        statsDisplayCompleted = new javax.swing.JLabel();
        statsRejectedPanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel24 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        statsDisplayRejected = new javax.swing.JLabel();
        statsRevenuePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel25 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        statsDisplayRevenue = new javax.swing.JLabel();
        statsAveRatePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel26 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        statsDisplayAveRate = new javax.swing.JLabel();
        statsQueueLengthPanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel27 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        statsDisplayQueueLength = new javax.swing.JLabel();
        statsLongestWaitTimePanel = new RoundedUI.RoundedPanel(10)
        ;
        jLabel28 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        statsDisplayWaitTime = new javax.swing.JLabel();
        queuePanel = new javax.swing.JPanel();
        queueTablePane = new javax.swing.JScrollPane();
        queueTable = new javax.swing.JTable();
        btnNextQueue = new RoundedUI.RoundedButton(12);
        historyPanel = new javax.swing.JPanel();
        historyTablePane = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        rentalPanel = new javax.swing.JPanel();
        rentalTabMiddlePanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        btnStatusPickedUp = new RoundedUI.RoundedButton(8);
        btnStatusReturned = new RoundedUI.RoundedButton(8);
        btnStatusExtend = new RoundedUI.RoundedButton(8)
        ;
        awaitingPanel = new javax.swing.JPanel();
        awaitingScroll = new javax.swing.JScrollPane();
        awaitingTable = new javax.swing.JTable();
        onRentPanel = new javax.swing.JPanel();
        onRentScroll = new javax.swing.JScrollPane();
        onRentTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        fleetPanel = new javax.swing.JPanel();
        fleetTablePane = new javax.swing.JScrollPane();
        fleetTable = new javax.swing.JTable();
        btnDeleteFleet = new RoundedUI.RoundedButton(12)
        ;
        btnEditFleet = new RoundedUI.RoundedButton(12);
        btnFleetStatus = new RoundedUI.RoundedButton(12);
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logo gamay.png"))); // NOI18N
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 60, 70));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Staff Console");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 20, 150, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Your central hub for managing bookings, vehicles, and rental transactions.");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 50, 480, 20));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Staff:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1003, 40, 40, 20));

        signOutButton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        signOutButton.setForeground(new java.awt.Color(204, 0, 51));
        signOutButton.setText("Sign out");
        signOutButton.setBorder(null);
        signOutButton.addActionListener(this::signOutButtonActionPerformed);
        jPanel1.add(signOutButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 30, 80, 40));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("System Admin -");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 40, 120, 20));
        jPanel1.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 16, -1, 728));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setOpaque(false);

        buttonGroup2.add(tabDashboard);
        tabDashboard.setText("Dashboard");
        tabDashboard.addActionListener(this::tabDashboardActionPerformed);

        buttonGroup2.add(tabRentals);
        tabRentals.setText("Rentals");
        tabRentals.addActionListener(this::tabRentalsActionPerformed);

        buttonGroup2.add(tabQueue);
        tabQueue.setText("Booking Queue");
        tabQueue.addActionListener(this::tabQueueActionPerformed);

        buttonGroup2.add(tabHistory);
        tabHistory.setText("History");
        tabHistory.addActionListener(this::tabHistoryActionPerformed);

        buttonGroup2.add(tabFleet);
        tabFleet.setText("Fleet");
        tabFleet.addActionListener(this::tabFleetActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabQueue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabRentals, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabFleet, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tabRentals, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabFleet, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, 600, 50));

        cardHeaderPanel.setLayout(new java.awt.CardLayout());

        dashboardHeaderPanel.setBackground(new java.awt.Color(20, 9, 11));
        dashboardHeaderPanel.setOpaque(false);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("System statistics");

        refreshButton.setBackground(new java.awt.Color(204, 0, 51));
        refreshButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("Refresh statistics");
        refreshButton.setBorder(null);
        refreshButton.addActionListener(this::refreshButtonActionPerformed);

        javax.swing.GroupLayout dashboardHeaderPanelLayout = new javax.swing.GroupLayout(dashboardHeaderPanel);
        dashboardHeaderPanel.setLayout(dashboardHeaderPanelLayout);
        dashboardHeaderPanelLayout.setHorizontalGroup(
            dashboardHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardHeaderPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );
        dashboardHeaderPanelLayout.setVerticalGroup(
            dashboardHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dashboardHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                .addContainerGap())
        );

        cardHeaderPanel.add(dashboardHeaderPanel, "dashboardHeader");

        rentalsHeaderPanel.setOpaque(false);
        rentalsHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel45.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("Awaiting on collection");
        rentalsHeaderPanel.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("Confirmed bookings the customers has not pick up yet.");
        rentalsHeaderPanel.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, 28));

        btnRentalsRefresh.setText("Refresh");
        btnRentalsRefresh.setPreferredSize(new java.awt.Dimension(76, 30));
        rentalsHeaderPanel.add(btnRentalsRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 20, 100, 40));

        cardHeaderPanel.add(rentalsHeaderPanel, "rentalsHeader");

        queueHeaderPanel.setOpaque(false);
        queueHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("Pending booking queue");
        queueHeaderPanel.add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, 30));

        jLabel44.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("No bookings are waiting");
        queueHeaderPanel.add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 160, 20));

        btnrefreshQueue.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnrefreshQueue.setForeground(new java.awt.Color(204, 0, 51));
        btnrefreshQueue.setText("Refresh Queue");
        btnrefreshQueue.setBorder(null);
        btnrefreshQueue.addActionListener(this::btnrefreshQueueActionPerformed);
        queueHeaderPanel.add(btnrefreshQueue, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 10, 120, 50));

        statsDisplayTotalVehicles1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        statsDisplayTotalVehicles1.setForeground(new java.awt.Color(255, 255, 255));
        statsDisplayTotalVehicles1.setText("0 Waiting");
        queueHeaderPanel.add(statsDisplayTotalVehicles1, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 20, 90, 30));

        queueSearch.setForeground(new java.awt.Color(255, 255, 255));
        queueSearch.setBorder(null);
        queueSearch.setOpaque(true);
        queueHeaderPanel.add(queueSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 20, 300, 36));

        cardHeaderPanel.add(queueHeaderPanel, "queueHeader");

        historyHeaderPanel.setOpaque(false);
        historyHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel47.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("Booking History");
        historyHeaderPanel.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        historySearch.setForeground(new java.awt.Color(255, 255, 255));
        historySearch.setBorder(null);
        historySearch.setOpaque(true);
        historySearch.addActionListener(this::historySearchActionPerformed);
        historyHeaderPanel.add(historySearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 27, 300, 36));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("0 of 0 bookings");
        historyHeaderPanel.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 40, 110, 20));

        btnHistoryTableRefresh.setText("Refresh");
        btnHistoryTableRefresh.setPreferredSize(new java.awt.Dimension(76, 30));
        historyHeaderPanel.add(btnHistoryTableRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 10, 80, 30));

        cmbFleetStatus1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All ", "Available", "Rented", "Maintenance" }));
        historyHeaderPanel.add(cmbFleetStatus1, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, 80, 30));

        jLabel51.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setText("Filter by: ");
        historyHeaderPanel.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 0, -1, 30));

        jLabel52.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("Date range: From - To");
        historyHeaderPanel.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 0, 150, 30));
        historyHeaderPanel.add(spnTo, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 30, 120, -1));
        historyHeaderPanel.add(spnFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, 120, -1));

        jLabel53.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel53.setText("Status");
        historyHeaderPanel.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 70, 30));

        btnClearDate.setText("Clear");
        historyHeaderPanel.add(btnClearDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 30, 70, -1));

        vipOnlyFilter.setForeground(new java.awt.Color(255, 255, 255));
        vipOnlyFilter.setText("VIP only");
        historyHeaderPanel.add(vipOnlyFilter, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 30, 100, 30));

        cardHeaderPanel.add(historyHeaderPanel, "historyHeader");

        fleetHeaderPanel.setOpaque(false);
        fleetHeaderPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel48.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("Fleet Management");
        fleetHeaderPanel.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 210, -1));

        btnfleetAddVehicle.setText("Add Vehicle");
        btnfleetAddVehicle.setPreferredSize(new java.awt.Dimension(76, 30));
        fleetHeaderPanel.add(btnfleetAddVehicle, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 10, 100, 50));

        btnFleetTableRefresh.setText("Refresh");
        btnFleetTableRefresh.setPreferredSize(new java.awt.Dimension(76, 30));
        fleetHeaderPanel.add(btnFleetTableRefresh, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 10, 100, 50));

        cmbCarType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "SUV", "Pickup", "Van", "MPV", "Hatchback", "Sedan", " " }));
        fleetHeaderPanel.add(cmbCarType, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 30, 90, 30));

        cmbFleetStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All ", "Available", "Rented", "Maintenance" }));
        fleetHeaderPanel.add(cmbFleetStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 30, 90, 30));

        jLabel50.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setText("Status");
        fleetHeaderPanel.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 10, 70, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Car Type");
        fleetHeaderPanel.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 10, 60, -1));

        jLabel49.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel49.setText("Filter by: ");
        fleetHeaderPanel.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 30, -1, -1));

        fleetSearch.setForeground(new java.awt.Color(255, 255, 255));
        fleetSearch.setBorder(null);
        fleetSearch.setOpaque(true);
        fleetHeaderPanel.add(fleetSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(225, 20, 300, 36));

        cardHeaderPanel.add(fleetHeaderPanel, "fleetHeader");

        jPanel1.add(cardHeaderPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, 1070, 70));

        cardPanel.setBackground(new java.awt.Color(255, 255, 255));
        cardPanel.setLayout(new java.awt.CardLayout());

        dashboardPanel.setBackground(new java.awt.Color(255, 255, 255));
        dashboardPanel.setOpaque(false);

        jLabel7.setBackground(new java.awt.Color(240, 240, 240));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(102, 102, 102));
        jLabel7.setText("Fleet at a glance");

        statsTotalVehiclePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsTotalVehiclePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/vehicle (2).png"))); // NOI18N
        jLabel15.setText("1");
        statsTotalVehiclePanel.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 55, 50));

        jLabel29.setBackground(new java.awt.Color(200, 200, 205));
        jLabel29.setForeground(new java.awt.Color(200, 200, 205));
        jLabel29.setText("Total Vehicles");
        statsTotalVehiclePanel.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayTotalVehicles.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayTotalVehicles.setForeground(new java.awt.Color(255, 255, 255));
        statsDisplayTotalVehicles.setText("10");
        statsTotalVehiclePanel.add(statsDisplayTotalVehicles, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        jLabel12.setBackground(new java.awt.Color(240, 240, 240));
        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 102, 102));
        jLabel12.setText("Booking pipeline");

        jLabel13.setBackground(new java.awt.Color(240, 240, 240));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(102, 102, 102));
        jLabel13.setText("Revenue and Service level");

        statsAvailableVehiclePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsAvailableVehiclePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/checkGreen.png"))); // NOI18N
        jLabel16.setText("jLabel16");
        statsAvailableVehiclePanel.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 57, 47));

        jLabel30.setBackground(new java.awt.Color(200, 200, 205));
        jLabel30.setForeground(new java.awt.Color(200, 200, 205));
        jLabel30.setText("Available");
        statsAvailableVehiclePanel.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, -1, -1));

        statsDisplayAvailable.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayAvailable.setForeground(new java.awt.Color(255, 255, 255));
        statsAvailableVehiclePanel.add(statsDisplayAvailable, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsReservedVehiclePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsReservedVehiclePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/calendar (1) - Copy.png"))); // NOI18N
        jLabel17.setText("jLabel17");
        statsReservedVehiclePanel.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 18, 54, 50));

        jLabel31.setBackground(new java.awt.Color(200, 200, 205));
        jLabel31.setForeground(new java.awt.Color(200, 200, 205));
        jLabel31.setText("Reserved");
        statsReservedVehiclePanel.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, -1, -1));

        statsDisplayReserved.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayReserved.setForeground(new java.awt.Color(255, 255, 255));
        statsReservedVehiclePanel.add(statsDisplayReserved, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsOnRentVehiclePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsOnRentVehiclePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/time (1) - Copy.png"))); // NOI18N
        jLabel18.setText("jLabel18");
        statsOnRentVehiclePanel.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 60, 40));

        jLabel32.setBackground(new java.awt.Color(200, 200, 205));
        jLabel32.setForeground(new java.awt.Color(200, 200, 205));
        jLabel32.setText("On Rent");
        statsOnRentVehiclePanel.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, 50, -1));

        statsDisplayOnRent.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayOnRent.setForeground(new java.awt.Color(255, 255, 255));
        statsOnRentVehiclePanel.add(statsDisplayOnRent, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsInMaintenancePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsInMaintenancePanel.setPreferredSize(new java.awt.Dimension(190, 132));
        statsInMaintenancePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/repair (1) - Copy.png"))); // NOI18N
        statsInMaintenancePanel.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, -1, 40));

        jLabel33.setBackground(new java.awt.Color(200, 200, 205));
        jLabel33.setForeground(new java.awt.Color(200, 200, 205));
        jLabel33.setText("In Maintenance");
        statsInMaintenancePanel.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, -1, -1));

        statsDisplayInMaintenance.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayInMaintenance.setForeground(new java.awt.Color(255, 255, 255));
        statsInMaintenancePanel.add(statsDisplayInMaintenance, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsInQueuePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsInQueuePanel.setPreferredSize(new java.awt.Dimension(190, 132));
        statsInQueuePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/person.png"))); // NOI18N
        statsInQueuePanel.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, -1, 40));

        jLabel34.setBackground(new java.awt.Color(200, 200, 205));
        jLabel34.setForeground(new java.awt.Color(200, 200, 205));
        jLabel34.setText("VIP Queue");
        statsInQueuePanel.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, -1, 20));

        statsDisplayRegularQueue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayRegularQueue.setForeground(new java.awt.Color(255, 255, 255));
        statsInQueuePanel.add(statsDisplayRegularQueue, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, 40, 40));

        jLabel54.setBackground(new java.awt.Color(200, 200, 205));
        jLabel54.setForeground(new java.awt.Color(200, 200, 205));
        jLabel54.setText("Regular Queue");
        statsInQueuePanel.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, 20));

        statsDisplayVipQueue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayVipQueue.setForeground(new java.awt.Color(255, 255, 255));
        statsInQueuePanel.add(statsDisplayVipQueue, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 40, 40));

        statsConfirmedPanel.setBackground(new java.awt.Color(255, 255, 255));
        statsConfirmedPanel.setPreferredSize(new java.awt.Dimension(190, 132));
        statsConfirmedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/checkYellow (1).png"))); // NOI18N
        statsConfirmedPanel.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 60, 40));

        jLabel35.setBackground(new java.awt.Color(200, 200, 205));
        jLabel35.setForeground(new java.awt.Color(200, 200, 205));
        jLabel35.setText("Confirmed");
        statsConfirmedPanel.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 66, -1, 20));

        statsDisplayConfirmed.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayConfirmed.setForeground(new java.awt.Color(255, 255, 255));
        statsConfirmedPanel.add(statsDisplayConfirmed, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsActivePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsActivePanel.setPreferredSize(new java.awt.Dimension(190, 132));
        statsActivePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/play (1).png"))); // NOI18N
        statsActivePanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 60, 40));

        jLabel36.setBackground(new java.awt.Color(200, 200, 205));
        jLabel36.setForeground(new java.awt.Color(200, 200, 205));
        jLabel36.setText("Active");
        statsActivePanel.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 66, -1, 20));

        statsDisplayActive.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayActive.setForeground(new java.awt.Color(255, 255, 255));
        statsActivePanel.add(statsDisplayActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsCompletedPanel.setBackground(new java.awt.Color(255, 255, 255));
        statsCompletedPanel.setPreferredSize(new java.awt.Dimension(190, 132));
        statsCompletedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/redCheck (1).png"))); // NOI18N
        statsCompletedPanel.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, -1, 40));

        jLabel37.setBackground(new java.awt.Color(200, 200, 205));
        jLabel37.setForeground(new java.awt.Color(200, 200, 205));
        jLabel37.setText("Completed");
        statsCompletedPanel.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayCompleted.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayCompleted.setForeground(new java.awt.Color(255, 255, 255));
        statsCompletedPanel.add(statsDisplayCompleted, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsRejectedPanel.setBackground(new java.awt.Color(255, 255, 255));
        statsRejectedPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/X (1).png"))); // NOI18N
        statsRejectedPanel.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, -1, 40));

        jLabel38.setBackground(new java.awt.Color(200, 200, 205));
        jLabel38.setForeground(new java.awt.Color(200, 200, 205));
        jLabel38.setText("Cancelled or Rejected");
        statsRejectedPanel.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayRejected.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayRejected.setForeground(new java.awt.Color(255, 255, 255));
        statsRejectedPanel.add(statsDisplayRejected, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsRevenuePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsRevenuePanel.setPreferredSize(new java.awt.Dimension(230, 132));
        statsRevenuePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/peso (1).png"))); // NOI18N
        statsRevenuePanel.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 22, 56, 43));

        jLabel39.setBackground(new java.awt.Color(200, 200, 205));
        jLabel39.setForeground(new java.awt.Color(200, 200, 205));
        jLabel39.setText("Revenue Booked");
        statsRevenuePanel.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayRevenue.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayRevenue.setForeground(new java.awt.Color(255, 255, 255));
        statsRevenuePanel.add(statsDisplayRevenue, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsAveRatePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsAveRatePanel.setPreferredSize(new java.awt.Dimension(230, 132));
        statsAveRatePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/statistic (1).png"))); // NOI18N
        statsAveRatePanel.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 54, 46));

        jLabel40.setBackground(new java.awt.Color(200, 200, 205));
        jLabel40.setForeground(new java.awt.Color(200, 200, 205));
        jLabel40.setText("Average Daily Rate");
        statsAveRatePanel.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayAveRate.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayAveRate.setForeground(new java.awt.Color(255, 255, 255));
        statsAveRatePanel.add(statsDisplayAveRate, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsQueueLengthPanel.setBackground(new java.awt.Color(255, 255, 255));
        statsQueueLengthPanel.setPreferredSize(new java.awt.Dimension(230, 132));
        statsQueueLengthPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/person.png"))); // NOI18N
        statsQueueLengthPanel.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 17, -1, 47));

        jLabel41.setBackground(new java.awt.Color(200, 200, 205));
        jLabel41.setForeground(new java.awt.Color(200, 200, 205));
        jLabel41.setText("Queue Length");
        statsQueueLengthPanel.add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        statsDisplayQueueLength.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayQueueLength.setForeground(new java.awt.Color(255, 255, 255));
        statsQueueLengthPanel.add(statsDisplayQueueLength, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        statsLongestWaitTimePanel.setBackground(new java.awt.Color(255, 255, 255));
        statsLongestWaitTimePanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/hourglass (1).png"))); // NOI18N
        statsLongestWaitTimePanel.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 10, 60, 50));

        jLabel42.setBackground(new java.awt.Color(200, 200, 205));
        jLabel42.setForeground(new java.awt.Color(200, 200, 205));
        jLabel42.setText("Longest Wait");
        statsLongestWaitTimePanel.add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, -1, 30));

        statsDisplayWaitTime.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        statsDisplayWaitTime.setForeground(new java.awt.Color(255, 255, 255));
        statsLongestWaitTimePanel.add(statsDisplayWaitTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 20, 60, 40));

        javax.swing.GroupLayout dashboardPanelLayout = new javax.swing.GroupLayout(dashboardPanel);
        dashboardPanel.setLayout(dashboardPanelLayout);
        dashboardPanelLayout.setHorizontalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(96, 96, 96))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dashboardPanelLayout.createSequentialGroup()
                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(dashboardPanelLayout.createSequentialGroup()
                                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(dashboardPanelLayout.createSequentialGroup()
                                                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(statsInQueuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                                                    .addComponent(statsTotalVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(18, 18, 18)
                                                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(statsConfirmedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                                                    .addComponent(statsAvailableVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                            .addComponent(jLabel13))
                                        .addGap(18, 18, 18)
                                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(statsReservedVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(statsActivePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))))
                                .addGap(18, 18, 18)
                                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(statsOnRentVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(statsCompletedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(statsRejectedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                                    .addComponent(statsInMaintenancePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                            .addGroup(dashboardPanelLayout.createSequentialGroup()
                                .addComponent(statsRevenuePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(statsAveRatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(statsQueueLengthPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(statsLongestWaitTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(25, 25, 25))))
        );
        dashboardPanelLayout.setVerticalGroup(
            dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(statsOnRentVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsAvailableVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsReservedVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsTotalVehiclePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statsInMaintenancePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardPanelLayout.createSequentialGroup()
                        .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(statsConfirmedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statsInQueuePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(statsRejectedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statsCompletedPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statsActivePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(dashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(statsQueueLengthPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statsAveRatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statsRevenuePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(statsLongestWaitTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        cardPanel.add(dashboardPanel, "dashboard");

        queuePanel.setBackground(new java.awt.Color(255, 255, 255));
        queuePanel.setOpaque(false);

        queueTablePane.setBorder(null);

        queueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Queue #", "Booking ID", "Customer name", "Vehicle", "Dates", "Total", "Waiting", "Status", "Tier"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        queueTable.setFillsViewportHeight(true);
        queueTable.setRowHeight(36);
        queueTablePane.setViewportView(queueTable);

        btnNextQueue.setText("Process next");
        btnNextQueue.addActionListener(this::btnNextQueueActionPerformed);

        javax.swing.GroupLayout queuePanelLayout = new javax.swing.GroupLayout(queuePanel);
        queuePanel.setLayout(queuePanelLayout);
        queuePanelLayout.setHorizontalGroup(
            queuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(queueTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 1070, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, queuePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNextQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        queuePanelLayout.setVerticalGroup(
            queuePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queuePanelLayout.createSequentialGroup()
                .addComponent(queueTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNextQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cardPanel.add(queuePanel, "queue");

        historyPanel.setBackground(new java.awt.Color(255, 255, 255));
        historyPanel.setOpaque(false);

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Booking ID", "Customer", "Car", "Period", "Total", "Status", "Handled by", "Note"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        historyTablePane.setViewportView(historyTable);

        javax.swing.GroupLayout historyPanelLayout = new javax.swing.GroupLayout(historyPanel);
        historyPanel.setLayout(historyPanelLayout);
        historyPanelLayout.setHorizontalGroup(
            historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(historyTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 1070, Short.MAX_VALUE)
        );
        historyPanelLayout.setVerticalGroup(
            historyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(historyTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );

        cardPanel.add(historyPanel, "history");

        rentalPanel.setBackground(new java.awt.Color(255, 255, 255));
        rentalPanel.setOpaque(false);
        rentalPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Vehicles that are out with the customers");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Currently On rent -");

        btnStatusPickedUp.setText("Mark picked up");

        btnStatusReturned.setText("Mark returned");
        btnStatusReturned.addActionListener(this::btnStatusReturnedActionPerformed);

        btnStatusExtend.setText("Extend Days");

        javax.swing.GroupLayout rentalTabMiddlePanelLayout = new javax.swing.GroupLayout(rentalTabMiddlePanel);
        rentalTabMiddlePanel.setLayout(rentalTabMiddlePanelLayout);
        rentalTabMiddlePanelLayout.setHorizontalGroup(
            rentalTabMiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rentalTabMiddlePanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 322, Short.MAX_VALUE)
                .addComponent(btnStatusReturned)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStatusPickedUp)
                .addGap(56, 56, 56)
                .addComponent(btnStatusExtend)
                .addContainerGap())
        );
        rentalTabMiddlePanelLayout.setVerticalGroup(
            rentalTabMiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rentalTabMiddlePanelLayout.createSequentialGroup()
                .addGroup(rentalTabMiddlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStatusPickedUp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStatusReturned, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStatusExtend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        rentalPanel.add(rentalTabMiddlePanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 240, 1070, 40));

        awaitingPanel.setLayout(new java.awt.GridLayout(1, 0));

        awaitingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Booking ID", "Customer", "is VIP?", "Car ", "Pickup", "Return", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        awaitingScroll.setViewportView(awaitingTable);

        awaitingPanel.add(awaitingScroll);

        rentalPanel.add(awaitingPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1068, 236));

        onRentPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        onRentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Booking ID", "Customer", "is VIP?", "Car ", "Pickup", "Return", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        onRentScroll.setViewportView(onRentTable);

        onRentPanel.add(onRentScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1070, 260));

        rentalPanel.add(onRentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 281, -1, 250));

        jPanel3.setBackground(new java.awt.Color(204, 0, 0));
        jPanel3.setPreferredSize(new java.awt.Dimension(1070, 2));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1070, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        rentalPanel.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 232, 1070, 10));

        cardPanel.add(rentalPanel, "rental");

        fleetPanel.setBackground(new java.awt.Color(255, 255, 255));
        fleetPanel.setOpaque(false);
        fleetPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        fleetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Plate", "Model", "Category", "Year", "Daily Rate", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fleetTablePane.setViewportView(fleetTable);

        fleetPanel.add(fleetTablePane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1070, 486));

        btnDeleteFleet.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDeleteFleet.setForeground(new java.awt.Color(204, 0, 51));
        btnDeleteFleet.setText("Delete");
        btnDeleteFleet.setBorder(null);
        btnDeleteFleet.setPreferredSize(new java.awt.Dimension(36, 30));
        btnDeleteFleet.addActionListener(this::btnDeleteFleetActionPerformed);
        fleetPanel.add(btnDeleteFleet, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 490, 80, 35));

        btnEditFleet.setText("Edit");
        fleetPanel.add(btnEditFleet, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 490, 90, 35));

        btnFleetStatus.setText("View status");
        fleetPanel.add(btnFleetStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 490, 100, 35));

        cardPanel.add(fleetPanel, "fleet");

        jPanel1.add(cardPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 210, 1070, 530));

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/CrimsonBg.png"))); // NOI18N
        jLabel14.setText("jLabel14");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1620, 760));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tabDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabDashboardActionPerformed
       ((CardLayout)cardPanel.getLayout()).show(cardPanel,"dashboard");
       ((CardLayout)cardHeaderPanel.getLayout()).show(cardHeaderPanel,"dashboardHeader");
       loadDashboard();
    }//GEN-LAST:event_tabDashboardActionPerformed

    private void tabQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabQueueActionPerformed
        ((CardLayout)cardPanel.getLayout()).show(cardPanel,"queue");
        ((CardLayout)cardHeaderPanel.getLayout()).show(cardHeaderPanel,"queueHeader");
    }//GEN-LAST:event_tabQueueActionPerformed

    private void tabRentalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabRentalsActionPerformed
        ((CardLayout)cardPanel.getLayout()).show(cardPanel,"rental");
        ((CardLayout)cardHeaderPanel.getLayout()).show(cardHeaderPanel,"rentalsHeader");
        loadRentals();
    }//GEN-LAST:event_tabRentalsActionPerformed

    private void tabHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabHistoryActionPerformed
        ((CardLayout)cardPanel.getLayout()).show(cardPanel,"history");
        ((CardLayout)cardHeaderPanel.getLayout()).show(cardHeaderPanel,"historyHeader");
        loadHistory();
    }//GEN-LAST:event_tabHistoryActionPerformed

    private void tabFleetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabFleetActionPerformed
        ((CardLayout)cardPanel.getLayout()).show(cardPanel,"fleet");
        ((CardLayout)cardHeaderPanel.getLayout()).show(cardHeaderPanel,"fleetHeader");
    }//GEN-LAST:event_tabFleetActionPerformed

    private void signOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signOutButtonActionPerformed
       this.dispose();
        StaffLogin login = new StaffLogin();
        login.pack();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }//GEN-LAST:event_signOutButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
       loadDashboard();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void btnrefreshQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrefreshQueueActionPerformed
       loadQueue();
    }//GEN-LAST:event_btnrefreshQueueActionPerformed

    private void btnDeleteFleetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteFleetActionPerformed
        deleteSelectedCar();
    }//GEN-LAST:event_btnDeleteFleetActionPerformed

    private void btnNextQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextQueueActionPerformed
        Booking next = queue.peek();                       // look at the head, don't remove yet
if (next == null) {
    JOptionPane.showMessageDialog(this, "No pending bookings in the queue.",
            "Queue empty", JOptionPane.INFORMATION_MESSAGE);
    return;
}
BookingDetailsDialog dlg = new BookingDetailsDialog(this, true);
dlg.setStaff(currentStaff);
dlg.setBooking(next);
dlg.setLocationRelativeTo(this);
dlg.setVisible(true);                              // waits here until the dialog closes

if (dlg.isProcessed()) {
    Booking done = queue.dequeue();                // DB already saved, so now remove it
    if (done == null || done.getDbId() != next.getDbId()) loadQueue();   // safety resync
    else redrawQueue();
} else {
    loadQueue();                                   // cancelled or stale: just resync
}
    }//GEN-LAST:event_btnNextQueueActionPerformed

    private void btnStatusReturnedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatusReturnedActionPerformed
       Booking b = selectedBooking(onRentTable, onRentRows, "Currently on rent");
    if (b == null) return;
    int ok = JOptionPane.showConfirmDialog(this,
            "Has " + b.getCustomer().getFullName() + " returned " + b.getVehicle()
            + "?\nMark booking " + b.getId() + " as returned?",
            "Mark returned", JOptionPane.YES_NO_OPTION);
    if (ok != JOptionPane.YES_OPTION) return;
    try {
        BookingDAO.ReturnResult r = bookingDAO.markReturned(b.getDbId());
        if (r.problem != null) {
            JOptionPane.showMessageDialog(this, r.problem, "Cannot mark returned",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            String name = b.getCustomer().getFullName();
            String msg = "Booking " + b.getId() + " completed. " + name + " now has "
                    + r.completedRentals + " completed rental" + (r.completedRentals == 1 ? "" : "s") + ".";
            if (r.becameVip) msg += "\n★ " + name + " is now a VIP!";
            JOptionPane.showMessageDialog(this, msg, "Rental completed",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(this, "Could not save:\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
    loadRentals();
    loadQueue(); 
    }//GEN-LAST:event_btnStatusReturnedActionPerformed

    private void historySearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historySearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_historySearchActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel awaitingPanel;
    private javax.swing.JScrollPane awaitingScroll;
    private javax.swing.JTable awaitingTable;
    private javax.swing.JButton btnClearDate;
    private javax.swing.JButton btnDeleteFleet;
    private javax.swing.JButton btnEditFleet;
    private javax.swing.JButton btnFleetStatus;
    private javax.swing.JButton btnFleetTableRefresh;
    private javax.swing.JButton btnHistoryTableRefresh;
    private javax.swing.JButton btnNextQueue;
    private javax.swing.JButton btnRentalsRefresh;
    private javax.swing.JButton btnStatusExtend;
    private javax.swing.JButton btnStatusPickedUp;
    private javax.swing.JButton btnStatusReturned;
    private javax.swing.JButton btnfleetAddVehicle;
    private javax.swing.JButton btnrefreshQueue;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JPanel cardHeaderPanel;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JComboBox<String> cmbCarType;
    private javax.swing.JComboBox<String> cmbFleetStatus;
    private javax.swing.JComboBox<String> cmbFleetStatus1;
    private javax.swing.JPanel dashboardHeaderPanel;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JPanel fleetHeaderPanel;
    private javax.swing.JPanel fleetPanel;
    private javax.swing.JTextField fleetSearch;
    private javax.swing.JTable fleetTable;
    private javax.swing.JScrollPane fleetTablePane;
    private javax.swing.JPanel historyHeaderPanel;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JTextField historySearch;
    private javax.swing.JTable historyTable;
    private javax.swing.JScrollPane historyTablePane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel onRentPanel;
    private javax.swing.JScrollPane onRentScroll;
    private javax.swing.JTable onRentTable;
    private javax.swing.JPanel queueHeaderPanel;
    private javax.swing.JPanel queuePanel;
    private javax.swing.JTextField queueSearch;
    private javax.swing.JTable queueTable;
    private javax.swing.JScrollPane queueTablePane;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel rentalPanel;
    private javax.swing.JPanel rentalTabMiddlePanel;
    private javax.swing.JPanel rentalsHeaderPanel;
    private javax.swing.JButton signOutButton;
    private javax.swing.JSpinner spnFrom;
    private javax.swing.JSpinner spnTo;
    private javax.swing.JPanel statsActivePanel;
    private javax.swing.JPanel statsAvailableVehiclePanel;
    private javax.swing.JPanel statsAveRatePanel;
    private javax.swing.JPanel statsCompletedPanel;
    private javax.swing.JPanel statsConfirmedPanel;
    private javax.swing.JLabel statsDisplayActive;
    private javax.swing.JLabel statsDisplayAvailable;
    private javax.swing.JLabel statsDisplayAveRate;
    private javax.swing.JLabel statsDisplayCompleted;
    private javax.swing.JLabel statsDisplayConfirmed;
    private javax.swing.JLabel statsDisplayInMaintenance;
    private javax.swing.JLabel statsDisplayOnRent;
    private javax.swing.JLabel statsDisplayQueueLength;
    private javax.swing.JLabel statsDisplayRegularQueue;
    private javax.swing.JLabel statsDisplayRejected;
    private javax.swing.JLabel statsDisplayReserved;
    private javax.swing.JLabel statsDisplayRevenue;
    private javax.swing.JLabel statsDisplayTotalVehicles;
    private javax.swing.JLabel statsDisplayTotalVehicles1;
    private javax.swing.JLabel statsDisplayVipQueue;
    private javax.swing.JLabel statsDisplayWaitTime;
    private javax.swing.JPanel statsInMaintenancePanel;
    private javax.swing.JPanel statsInQueuePanel;
    private javax.swing.JPanel statsLongestWaitTimePanel;
    private javax.swing.JPanel statsOnRentVehiclePanel;
    private javax.swing.JPanel statsQueueLengthPanel;
    private javax.swing.JPanel statsRejectedPanel;
    private javax.swing.JPanel statsReservedVehiclePanel;
    private javax.swing.JPanel statsRevenuePanel;
    private javax.swing.JPanel statsTotalVehiclePanel;
    private javax.swing.JToggleButton tabDashboard;
    private javax.swing.JToggleButton tabFleet;
    private javax.swing.JToggleButton tabHistory;
    private javax.swing.JToggleButton tabQueue;
    private javax.swing.JToggleButton tabRentals;
    private javax.swing.JCheckBox vipOnlyFilter;
    // End of variables declaration//GEN-END:variables
}

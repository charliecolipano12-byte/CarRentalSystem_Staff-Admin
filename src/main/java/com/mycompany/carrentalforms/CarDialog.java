package com.mycompany.carrentalforms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Small add/edit form for a car. Pass null to add, or an existing Car to edit. */
public class CarDialog extends JDialog {

    /** What the staff member typed (already checked to be a number for the rate). */
    public static final class Result {
        public final String plate, model, type;
        public final double rate;
        Result(String plate, String model, String type, double rate) {
            this.plate = plate; this.model = model; this.type = type; this.rate = rate;
        }
    }

    private final JTextField txtPlate = new JTextField(16);
    private final JTextField txtModel = new JTextField(16);
    private final JComboBox<String> cmbType =
            new JComboBox<>(new String[] { "Sedan", "SUV", "Pickup", "Van", "Hatchback" });
    private final JTextField txtRate = new JTextField(16);
    private Result result;

    private CarDialog(Window owner, Car existing) {
        super(owner, existing == null ? "Add vehicle" : "Edit vehicle", ModalityType.APPLICATION_MODAL);
        cmbType.setEditable(true);
        if (existing != null) {
            txtPlate.setText(existing.getPlate());
            txtModel.setText(existing.getModel());
            cmbType.setSelectedItem(existing.getType());
            txtRate.setText(String.valueOf(existing.getDailyRate()));
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Plate"));          form.add(txtPlate);
        form.add(new JLabel("Model"));          form.add(txtModel);
        form.add(new JLabel("Category"));       form.add(cmbType);
        form.add(new JLabel("Daily rate (PHP)")); form.add(txtRate);

        JButton ok = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(ok);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
    }

    private void onSave() {
        double rate;
        try {
            rate = Double.parseDouble(txtRate.getText().trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Daily rate must be a number, e.g. 2500.",
                    "Invalid rate", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object type = cmbType.getSelectedItem();
        result = new Result(txtPlate.getText(), txtModel.getText(),
                type == null ? "" : type.toString(), rate);
        dispose();   // the DAO does the rest of the validation and shows its message
    }

    /** @return what was entered, or null if the staff member cancelled */
    public static Result show(Component parent, Car existing) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (owner == null && parent instanceof Window) owner = (Window) parent;
        CarDialog d = new CarDialog(owner, existing);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);          // blocks until closed
        return d.result;
    }
}

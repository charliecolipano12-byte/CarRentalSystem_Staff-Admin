package com.mycompany.carrentalforms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class RoundedUI {
        
    private static final Color BORDER_NORMAL = new Color(200, 200, 200);

private static void paintRounded(Graphics g, JComponent c, int radius, boolean border) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, c.getWidth() - 1, c.getHeight() - 1, radius, radius);
    if (border) {
        Object custom = c.getClientProperty("borderColor");
        g2.setColor(custom instanceof Color ? (Color) custom : BORDER_NORMAL);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(shape);
    } else {
        g2.setColor(c.getBackground());
        g2.fill(shape);
    }
    g2.dispose();
}

    public static class RoundedTextField extends JTextField {
        private final int radius;

        public RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        }

        @Override protected void paintComponent(Graphics g) {
            paintRounded(g, this, radius, false);
            super.paintComponent(g);
        }

        @Override protected void paintBorder(Graphics g) {
            paintRounded(g, this, radius, true);
        }
    }

    public static class RoundedPasswordField extends JPasswordField {
        private final int radius;

        public RoundedPasswordField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        }

        @Override protected void paintComponent(Graphics g) {
            paintRounded(g, this, radius, false);
            super.paintComponent(g);
        }

        @Override protected void paintBorder(Graphics g) {
            paintRounded(g, this, radius, true);
        }
    }

    public static class RoundedButton extends JButton {
        private final int radius;
        private Color outlineColor  = null;  
        private Color fillColor     = new Color(204, 0, 51);
        private Color hoverColor    = new Color(170, 0, 43);
        private float outlineWidth  = 1.5f;
        private boolean hovering = false;
        
        public RoundedButton(int radius) {
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                
                Color original;
                @Override public void mouseEntered(MouseEvent e) {
                    original = getBackground();
                    setBackground(original.darker());
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (original != null) setBackground(original);
                }
            });
        }
        
         public void setColors(Color fill, Color hover, Color outline) {
        fillColor = fill;
        hoverColor = hover;
        outlineColor = outline;
        repaint();
    }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            float half = outlineWidth / 2f;
            RoundRectangle2D shape = new RoundRectangle2D.Double(
            half,half, getWidth()-outlineWidth, getHeight() -outlineWidth, radius,radius);
            g2.setColor(hovering? hoverColor : fillColor);
            g2.fill(shape);
            
            if(outlineColor !=null){
               g2.setColor(outlineColor);
               g2.setStroke(new BasicStroke(outlineWidth));
               g2.draw(shape);
            }
            
           
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
    
    public static class RoundedPanel extends JPanel {
    private final int radius;
    private Color borderColor = new Color(220, 224, 230);
    private float borderWidth = 1.5f;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    public void setBorderColor(Color c) { this.borderColor = c; repaint(); }
    public void setBorderWidth(float w) { this.borderWidth = w; repaint(); }

    private RoundRectangle2D shape() {
        float half = borderWidth / 2f;
        return new RoundRectangle2D.Double(half, half,
                getWidth() - borderWidth, getHeight() - borderWidth, radius, radius);
    }

    @Override
    protected void paintComponent(Graphics g) {      // background fill
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(shape());
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintChildren(Graphics g) {       // children first, then the border on top
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.draw(shape());
        g2.dispose();
    }
}
    
    public static class RoundedToggleButton extends JToggleButton{
        private final int radius;
        private Color borderNormal = new Color(215,219,226);
        private Color borderSelected = new Color(37,99,235);
        private Color fillNormal = Color.WHITE;
        private Color fillSelected = new Color(232,240,254);
        private Color textNormal = new Color(60,64,72);
        private Color textSelected = new Color(37,99,235);
        
        RoundedToggleButton(int radius){
            this.radius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(new Font("Sogue UI", Font.BOLD, 14));
        }
        
        protected void paintComponent(Graphics g){
            Graphics2D g2 =(Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            float w = 1.5f;
            RoundRectangle2D shape = new RoundRectangle2D.Double(
            w/2,w/2,getWidth() -w, getHeight()-w,radius,radius);
            
            g2.setColor(isSelected() ? fillSelected : fillNormal);
            g2.fill(shape);
            
            g2.setColor(isSelected()? borderSelected : borderNormal);
            g2.setStroke(new BasicStroke(w));
            g2.draw(shape);
            g2.dispose();
            
            setForeground(isSelected()? textSelected : textNormal);
            super.paintComponent(g);
        }
        
        public void setSelectedColors(Color border, Color fill, Color text){
            borderSelected = border;
            fillSelected = fill;
            textSelected = text;
            repaint();
        }
        
        public void setNormalColors(Color border,Color fill,Color text){
            borderNormal =border;
            fillNormal = fill;
            textNormal = text;
            repaint();
        }
        
    }
    
    public static void styleTable(JTable table, JScrollPane scroll) {
    final Color rowColor   = new Color(24, 10, 13);
    final Color altColor   = new Color(30, 12, 16);
    final Color selColor   = new Color(110, 22, 30);
    final Color headerBg   = new Color(40, 10, 14);
    final Color headerLine = new Color(215, 25, 40);
    final Color textColor  = new Color(240, 240, 240);

    table.setRowHeight(36);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setFillsViewportHeight(true);
    table.setBackground(rowColor);
    table.setForeground(textColor);
    table.setSelectionBackground(selColor);
    table.setSelectionForeground(Color.WHITE);
    table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    table.getTableHeader().setReorderingAllowed(false);

    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(rowColor);

    javax.swing.table.JTableHeader header = table.getTableHeader();
    header.setPreferredSize(new Dimension(0, 38));
    header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, col);
            l.setOpaque(true);
            l.setBackground(headerBg);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, headerLine),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            l.setHorizontalAlignment(SwingConstants.LEFT);
            return l;
        }
    });

    table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, false, row, col);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (!sel) {
                l.setBackground(row % 2 == 0 ? rowColor : altColor);
                l.setForeground(textColor);
            }
            return l;
        }
    });
}

// Colors the text of one column by status (QUEUED, CONFIRMED, REJECTED)
public static void styleStatusColumn(JTable table, int columnIndex) {
    table.getColumnModel().getColumn(columnIndex).setCellRenderer(
        new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, sel, false, row, col);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!sel) l.setBackground(row % 2 == 0 ? new Color(24, 10, 13) : new Color(30, 12, 16));

                String s = String.valueOf(value);
                if (s.equals("QUEUED"))         l.setForeground(new Color(245, 170, 20));
                else if (s.equals("CONFIRMED")) l.setForeground(new Color(34, 197, 94));
                else if (s.equals("REJECTED"))  l.setForeground(new Color(225, 60, 70));
                else                            l.setForeground(Color.WHITE);
                return l;
            }
        });
}

public static void attachSearch(JTextField field, JTable table) {
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
    table.setRowSorter(sorter);

    field.getDocument().addDocumentListener(new DocumentListener() {
        private void filter() {
            String t = field.getText().trim();
            sorter.setRowFilter(t.isEmpty() ? null
                    : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(t)));
        }
        @Override
        public void insertUpdate(DocumentEvent e)  { filter(); }
        @Override
        public void removeUpdate(DocumentEvent e)  { filter(); }
        @Override
        public void changedUpdate(DocumentEvent e) { filter(); }
    });
}

public static class SearchField extends JTextField {
    private static final Color BG     = new Color(26, 10, 14);
    private static final Color BORDER = new Color(120, 25, 35);
    private static final Color FOCUS  = new Color(220, 25, 40);
    private static final Color HINT   = new Color(150, 120, 125);
    private final String placeholder;

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setSelectionColor(FOCUS);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new EmptyBorder(8, 38, 8, 16));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e)   { repaint(); }
        });
    }
    
            @Override
        public boolean isOpaque() {
            return false;               // never let the UI paint a white background
        }

        @Override
        public void setBorder(javax.swing.border.Border b) {
            // always keep our padding (room for the icon), ignore outside borders
            super.setBorder(new javax.swing.border.EmptyBorder(8, 38, 8, 16));
        }
        
                @Override
        public void setBackground(Color bg) {
            super.setBackground(new Color(0, 0, 0, 0));   // always fully transparent
        }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean focused = isFocusOwner();

        // background + border
        g2.setColor(BG);
        g2.fillRoundRect(1, 1, w - 2, h - 2, h, h);
        g2.setColor(focused ? FOCUS : BORDER);
        g2.setStroke(new BasicStroke(focused ? 2f : 1f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);

        // magnifier icon
        g2.setColor(focused ? FOCUS : HINT);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cy = h / 2;
        g2.drawOval(15, cy - 7, 10, 10);
        g2.drawLine(24, cy + 2, 29, cy + 7);

        super.paintComponent(g);

        // placeholder text
        if (getText().isEmpty() && !focused) {
            g2.setFont(getFont());
            g2.setColor(HINT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, getInsets().left,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
        }
        g2.dispose();
    }
}

    public static void styleComboBox(JComboBox<String> combo) {
    final Color BG      = new Color(28, 28, 28);
    final Color BORDER  = new Color(150, 20, 30);
    final Color ARROW   = new Color(200, 50, 60);
    final Color SELECT  = new Color(130, 15, 25);
    final int ARC = 16;

    combo.setOpaque(false);
    combo.setForeground(Color.WHITE);
    combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    combo.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
    combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Dropdown items
    combo.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            setForeground(Color.WHITE);
            setBackground(isSelected ? SELECT : BG);
            setOpaque(index != -1); // index -1 = the box itself, keep corners transparent
            return this;
        }
    });

    combo.setUI(new BasicComboBoxUI() {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override
                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ARROW);
                    int w = getWidth(), h = getHeight();
                    g2.fillPolygon(new int[]{w/2 - 5, w/2 + 5, w/2},
                                   new int[]{h/2 - 2, h/2 - 2, h/2 + 3}, 3);
                    g2.dispose();
                }
            };
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            return btn;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // leave empty so the rounded background shows
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG);
            g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, ARC, ARC);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(BORDER));
            popup.getList().setBackground(BG);
            popup.getList().setSelectionBackground(SELECT);
            return popup;
        }
    });
}
    
    private static final Color SP_BG     = new Color(26, 26, 26);
    private static final Color SP_ACCENT = new Color(139, 0, 0);
    private static final Color SP_HOVER  = new Color(178, 34, 34);
    private static final Color SP_FG     = new Color(240, 240, 240);

    public static void styleSpinner(JSpinner spinner) {
        spinner.setUI(new BasicSpinnerUI() {
            @Override protected Component createNextButton() {
                JButton b = arrowButton("\u25B2");
                b.addActionListener(e -> {
                    Object v = spinner.getNextValue();
                    if (v != null) spinner.setValue(v);
                });
                return b;
            }
            @Override protected Component createPreviousButton() {
                JButton b = arrowButton("\u25BC");
                b.addActionListener(e -> {
                    Object v = spinner.getPreviousValue();
                    if (v != null) spinner.setValue(v);
                });
                return b;
            }
        });

                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            JFormattedTextField tf = editor.getTextField();

            tf.setUI(new javax.swing.plaf.basic.BasicTextFieldUI()); // stops the L&F from painting over your colors
            tf.setOpaque(true);
            tf.setBackground(SP_BG);
            tf.setForeground(SP_FG);
            tf.setCaretColor(SP_FG);
            tf.setSelectionColor(SP_ACCENT);
            tf.setSelectedTextColor(Color.WHITE);
            tf.setDisabledTextColor(SP_FG);
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            editor.setOpaque(true);
            editor.setBackground(SP_BG);

            spinner.setOpaque(true);
            spinner.setBackground(SP_BG);
        spinner.setPreferredSize(new Dimension(130, 32));
    }

    private static JButton arrowButton(String symbol) {
        JButton b = new JButton(symbol);
        b.setFont(new Font("SansSerif", Font.PLAIN, 8));
        b.setForeground(SP_FG);
        b.setBackground(SP_ACCENT);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(22, 14));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(SP_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(SP_ACCENT); }
        });
        return b;
    }

    // rounded outline border (skip this if you already have one)
    private static class SpinnerBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        SpinnerBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
    
}

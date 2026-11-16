package studenttaskmanager.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Frame;
import java.text.ParseException;
import java.util.Locale;

public final class SettingsDialog {
    private SettingsDialog() {
    }

    public static Double showDialog(Frame owner, double currentHoursPerDay) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                currentHoursPerDay, 0.25, 24.0, 0.5));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.0"));

        JLabel description = new JLabel(
                "<html>Set how many hours you can realistically study each day.<br>"
                        + "This value drives suggested start dates and workload warnings.</html>");
        JLabel weekly = new JLabel(weeklyText(currentHoursPerDay));

        spinner.addChangeListener(event -> {
            double hours = ((Number) spinner.getValue()).doubleValue();
            weekly.setText(weeklyText(hours));
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(description);
        panel.add(javax.swing.Box.createVerticalStrut(10));
        panel.add(new JLabel("Study hours per day:"));
        panel.add(spinner);
        panel.add(javax.swing.Box.createVerticalStrut(8));
        panel.add(weekly);

        int option = JOptionPane.showConfirmDialog(owner, panel, "Study settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            spinner.commitEdit();
        } catch (ParseException exception) {
            JOptionPane.showMessageDialog(owner, "Please enter a valid number of hours.",
                    "Invalid settings", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return ((Number) spinner.getValue()).doubleValue();
    }

    private static String weeklyText(double hoursPerDay) {
        return String.format(Locale.ENGLISH,
                "Weekly study limit: %.1f hours", hoursPerDay * 7.0);
    }
}

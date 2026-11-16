package studenttaskmanager.ui;

import studenttaskmanager.service.ReportService;
import studenttaskmanager.service.TaskService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;

public final class ReportsDialog extends JDialog {
    private final ReportService reportService = new ReportService();
    private final String reportText;
    private final JTextArea reportArea = new JTextArea();

    public ReportsDialog(Frame owner, TaskService service) {
        super(owner, "Reports", true);
        reportText = reportService.generateReport(service.getTasks(), service.getHoursPerDay());

        setLayout(new BorderLayout());
        JLabel heading = new JLabel(
                "Weekly summary, completion rate, and tasks per subject");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14.0f));
        heading.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        add(heading, BorderLayout.NORTH);

        reportArea.setText(reportText);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 0, 16),
                BorderFactory.createLineBorder(new Color(226, 232, 240))));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dispose());
        JButton exportButton = new JButton("Export as text file");
        exportButton.setFont(exportButton.getFont().deriveFont(Font.BOLD));
        exportButton.addActionListener(event -> exportReport());
        buttons.add(closeButton);
        buttons.add(exportButton);
        add(buttons, BorderLayout.SOUTH);

        setSize(900, 700);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export report");
        chooser.setSelectedFile(new File("task-report-" + LocalDate.now() + ".txt"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            selectedFile = new File(selectedFile.getParentFile(),
                    selectedFile.getName() + ".txt");
        }
        if (selectedFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    "The file already exists. Overwrite it?", "Confirm overwrite",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Path path = selectedFile.toPath();
            reportService.exportReport(reportText, path);
            JOptionPane.showMessageDialog(this,
                    "Report exported to:\n" + path.toAbsolutePath(),
                    "Report exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this,
                    "Could not export the report.\n" + exception.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

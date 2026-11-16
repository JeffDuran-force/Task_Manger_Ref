package studenttaskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import studenttaskmanager.exception.TaskValidationException;
import studenttaskmanager.model.Difficulty;
import studenttaskmanager.model.Task;
import studenttaskmanager.model.TaskStatus;
import studenttaskmanager.model.TaskType;

public final class TaskDialog extends JDialog {
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color MUTED = new Color(100, 116, 139);

    private final Task existing;
    private final double hoursPerDay;
    private final JTextField titleField = new JTextField(30);
    private final JComboBox<String> subjectBox;
    private final JComboBox<TaskType> typeBox = new JComboBox<>(TaskType.values());
    private final JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());
    private final JSpinner hoursSpinner;
    private final JSpinner deadlineSpinner;
    private final JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());
    private final JTextArea notesArea = new JTextArea(4, 32);
    private Task result;

    private TaskDialog(Frame owner, Task existing, double hoursPerDay, List<String> subjects) {
        super(owner, existing == null ? "Add task" : "Edit task", true);
        this.existing = existing;
        this.hoursPerDay = hoursPerDay;

        subjectBox = new JComboBox<>();
        subjectBox.setEditable(true);
        for (String subject : subjects) {
            subjectBox.addItem(subject);
        }
        subjectBox.setSelectedItem(existing == null ? "" : existing.getSubject());

        double initialHours = existing == null ? 2.0 : existing.getEstimatedHours();
        hoursSpinner = new JSpinner(new SpinnerNumberModel(initialHours, 0.25, 200.0, 0.5));
        hoursSpinner.setEditor(new JSpinner.NumberEditor(hoursSpinner, "0.0"));

        LocalDate initialDeadline = existing == null
                ? LocalDate.now().plusDays(7)
                : existing.getDeadline();
        deadlineSpinner = new JSpinner(new SpinnerDateModel(
                toDate(initialDeadline), null, null, Calendar.DAY_OF_MONTH));
        deadlineSpinner.setEditor(new JSpinner.DateEditor(deadlineSpinner, "dd MMM yyyy"));

        if (existing != null) {
            titleField.setText(existing.getTitle());
            typeBox.setSelectedItem(existing.getType());
            difficultyBox.setSelectedItem(existing.getDifficulty());
            statusBox.setSelectedItem(existing.getStatus());
            notesArea.setText(existing.getNotes());
        } else {
            typeBox.setSelectedItem(TaskType.defaultType());
            difficultyBox.setSelectedItem(Difficulty.DEFAULT);
            statusBox.setSelectedItem(TaskStatus.NOT_STARTED);
        }
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        setLayout(new BorderLayout());
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(event -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setMinimumSize(new Dimension(620, 470));
        setLocationRelativeTo(owner);
        titleField.requestFocusInWindow();
    }

    public static Task showDialog(Frame owner, Task existing, double hoursPerDay,
                                  List<String> subjects) {
        TaskDialog dialog = new TaskDialog(owner, existing, hoursPerDay, subjects);
        dialog.setVisible(true);
        return dialog.result;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 8, 16));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addField(form, constraints, 0, 0, 3, "Title", titleField);
        addField(form, constraints, 1, 0, 1, "Subject", subjectBox);
        addField(form, constraints, 1, 2, 1, "Type", typeBox);
        addField(form, constraints, 2, 0, 1, "Difficulty", difficultyBox);
        addField(form, constraints, 2, 2, 1, "Estimated hours", hoursSpinner);
        addField(form, constraints, 3, 0, 1, "Deadline", deadlineSpinner);
        addField(form, constraints, 3, 2, 1, "Status", statusBox);

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(420, 90));
        addField(form, constraints, 4, 0, 3, "Notes / keywords", notesScroll);

        JLabel hint = new JLabel(String.format(Locale.ENGLISH,
                "Suggested start dates use %.1f study hours per day.", hoursPerDay));
        hint.setForeground(MUTED);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 4;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(2, 6, 4, 6);
        form.add(hint, constraints);
        return form;
    }

    private void addField(JPanel form, GridBagConstraints constraints, int row,
                          int labelColumn, int fieldWidth, String labelText,
                          JComponent field) {
        constraints.gridx = labelColumn;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        form.add(label, constraints);

        constraints.gridx = labelColumn + 1;
        constraints.gridwidth = fieldWidth;
        constraints.weightx = 1.0;
        form.add(field, constraints);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        JButton saveButton = new JButton("Save task");
        saveButton.setBackground(ACCENT);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
        saveButton.setOpaque(true);
        saveButton.addActionListener(event -> saveTask());
        panel.add(cancelButton);
        panel.add(saveButton);
        getRootPane().setDefaultButton(saveButton);
        return panel;
    }

    private void saveTask() {
        try {
            hoursSpinner.commitEdit();
            deadlineSpinner.commitEdit();
        } catch (ParseException exception) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number of hours and a valid deadline.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showValidation("Title is required.");
            titleField.requestFocusInWindow();
            return;
        }

        Object subjectValue = subjectBox.isEditable()
                ? subjectBox.getEditor().getItem()
                : subjectBox.getSelectedItem();
        String subject = subjectValue == null ? "" : subjectValue.toString().trim();
        if (subject.isEmpty()) {
            showValidation("Subject is required.");
            subjectBox.requestFocusInWindow();
            return;
        }

        double estimatedHours;
        try {
            estimatedHours = ((Number) hoursSpinner.getValue()).doubleValue();
        } catch (RuntimeException exception) {
            showValidation("Estimated hours must be a number.");
            return;
        }

        Date deadlineValue = (Date) deadlineSpinner.getValue();
        LocalDate deadline = deadlineValue.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        TaskType type = (TaskType) typeBox.getSelectedItem();
        Difficulty difficulty = (Difficulty) difficultyBox.getSelectedItem();
        TaskStatus status = (TaskStatus) statusBox.getSelectedItem();

        LocalDate completedDate = null;
        if (status != null && status.isDone()) {
            completedDate = existing != null && existing.getCompletedDate() != null
                    ? existing.getCompletedDate()
                    : LocalDate.now();
        }

        try {
            UUID id = existing == null ? UUID.randomUUID() : existing.getId();
            result = new Task(id, title, subject, type, difficulty, estimatedHours,
                    deadline, status, notesArea.getText(), completedDate);
            dispose();
        } catch (TaskValidationException exception) {
            showValidation(exception.getMessage());
        }
    }

    private void showValidation(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid task",
                JOptionPane.WARNING_MESSAGE);
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}

package studenttaskmanager.ui;

import studenttaskmanager.model.Difficulty;
import studenttaskmanager.model.Task;
import studenttaskmanager.model.TaskStatus;
import studenttaskmanager.model.TaskType;
import studenttaskmanager.model.WorkloadWarning;
import studenttaskmanager.service.ReportService;
import studenttaskmanager.service.SortMode;
import studenttaskmanager.service.TaskService;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainFrame extends JFrame {
    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color ACCENT = new Color(37, 99, 235);
    private static final Color DANGER = new Color(185, 28, 28);
    private static final Color SUCCESS = new Color(22, 101, 52);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final Color ROW_ALT = new Color(248, 250, 252);

    private final TaskService service;
    private final ReportService reportService = new ReportService();
    private final TaskTableModel tableModel = new TaskTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> subjectFilter = new JComboBox<>();
    private final JComboBox<String> typeFilter = new JComboBox<>();
    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final JComboBox<String> difficultyFilter = new JComboBox<>();
    private final JComboBox<SortMode> sortBox = new JComboBox<>(SortMode.values());
    private final JLabel capacityLabel = new JLabel();
    private final JLabel summaryLabel = new JLabel();
    private final JTextArea warningArea = new JTextArea(4, 60);
    private JButton editButton;
    private JButton deleteButton;
    private JButton doneButton;
    private boolean updatingFilters;

    public MainFrame(TaskService service) {
        this.service = service;
        setTitle("Student Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1260, 800);
        setLocationRelativeTo(null);

        configureTable();
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        wireEvents();
        refreshAll(null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Student Task Manager");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26.0f));
        JLabel subtitle = new JLabel(
                "Prioritize smarter, start earlier, and catch workload overloads.");
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13.0f));
        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(5));
        textBlock.add(subtitle);

        updateCapacityLabel();
        capacityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(textBlock, BorderLayout.CENTER);
        header.add(capacityLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(SURFACE);
        main.setBorder(new EmptyBorder(12, 12, 0, 12));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.add(createFiltersPanel(), BorderLayout.NORTH);
        top.add(createActionsPanel(), BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        main.add(card, BorderLayout.CENTER);
        return main;
    }

    private JPanel createFiltersPanel() {
        populateFixedFilters();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(2, 8, 2, 8)));

        searchField.setPreferredSize(new Dimension(220, 30));
        searchField.setToolTipText("Search title, subject, notes, type, or status");
        panel.add(filterLabel("Search"));
        panel.add(searchField);

        subjectFilter.setPreferredSize(new Dimension(145, 30));
        typeFilter.setPreferredSize(new Dimension(125, 30));
        statusFilter.setPreferredSize(new Dimension(130, 30));
        difficultyFilter.setPreferredSize(new Dimension(110, 30));
        sortBox.setPreferredSize(new Dimension(145, 30));

        panel.add(filterLabel("Subject"));
        panel.add(subjectFilter);
        panel.add(filterLabel("Type"));
        panel.add(typeFilter);
        panel.add(filterLabel("Status"));
        panel.add(statusFilter);
        panel.add(filterLabel("Difficulty"));
        panel.add(difficultyFilter);
        panel.add(filterLabel("Sort"));
        panel.add(sortBox);
        return panel;
    }

    private void populateFixedFilters() {
        typeFilter.addItem("All types");
        for (TaskType type : TaskType.values()) {
            typeFilter.addItem(type.getLabel());
        }

        statusFilter.addItem("All statuses");
        for (TaskStatus status : TaskStatus.values()) {
            statusFilter.addItem(status.getLabel());
        }

        difficultyFilter.addItem("All difficulties");
        for (Difficulty difficulty : Difficulty.values()) {
            difficultyFilter.addItem(difficulty.getLabel());
        }

        sortBox.setSelectedItem(SortMode.SMART_PRIORITY);
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton addButton = primaryButton("Add task");
        addButton.setToolTipText("Create a new task");
        addButton.addActionListener(event -> onAdd());
        editButton = secondaryButton("Edit");
        editButton.addActionListener(event -> onEdit());
        deleteButton = secondaryButton("Delete");
        deleteButton.setForeground(DANGER);
        deleteButton.addActionListener(event -> onDelete());
        doneButton = successButton("Mark done");
        doneButton.addActionListener(event -> onMarkDone());
        left.add(addButton);
        left.add(editButton);
        left.add(deleteButton);
        left.add(doneButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton reportsButton = secondaryButton("Reports");
        reportsButton.addActionListener(event -> onReports());
        JButton settingsButton = secondaryButton("Settings");
        settingsButton.addActionListener(event -> onSettings());
        right.add(reportsButton);
        right.add(settingsButton);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void configureTable() {
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(false);
        table.setDefaultRenderer(Object.class, new TaskCellRenderer());

        int[] widths = {240, 135, 100, 95, 115, 70, 110, 80, 165};
        for (int index = 0; index < widths.length; index++) {
            table.getColumnModel().getColumn(index).setPreferredWidth(widths[index]);
        }

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateActionState();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onEdit();
                }
            }
        });

        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        table.getActionMap().put("deleteTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editTask");
        table.getActionMap().put("editTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onEdit();
            }
        });
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(0, 5));
        footer.setBackground(SURFACE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(8, 16, 10, 16)));

        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD, 12.0f));
        summaryLabel.setForeground(new Color(51, 65, 85));
        footer.add(summaryLabel, BorderLayout.NORTH);

        warningArea.setEditable(false);
        warningArea.setFocusable(false);
        warningArea.setOpaque(false);
        warningArea.setLineWrap(true);
        warningArea.setWrapStyleWord(true);
        warningArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        warningArea.setBorder(null);
        footer.add(warningArea, BorderLayout.CENTER);

        JLabel saveLabel = new JLabel(
                "Tasks autosave to data/tasks.csv. Settings autosave to data/settings.csv.");
        saveLabel.setForeground(MUTED);
        saveLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.add(saveLabel, BorderLayout.SOUTH);
        return footer;
    }

    private void wireEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshTable(null);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshTable(null);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshTable(null);
            }
        });

        ActionListener filterListener = event -> {
            if (!updatingFilters) {
                refreshTable(null);
            }
        };
        JComboBox<?>[] filterBoxes = {
                subjectFilter, typeFilter, statusFilter, difficultyFilter, sortBox
        };
        for (JComboBox<?> comboBox : filterBoxes) {
            comboBox.addActionListener(filterListener);
        }
    }

    private void refreshAll(UUID taskToSelect) {
        updatingFilters = true;
        try {
            refreshSubjectOptions();
        } finally {
            updatingFilters = false;
        }
        updateCapacityLabel();
        refreshTable(taskToSelect);
    }

    private void refreshSubjectOptions() {
        Object previous = subjectFilter.getSelectedItem();
        subjectFilter.removeAllItems();
        subjectFilter.addItem("All subjects");
        for (String subject : service.getSubjects()) {
            subjectFilter.addItem(subject);
        }
        if (previous != null && !"All subjects".equals(previous)) {
            for (int index = 1; index < subjectFilter.getItemCount(); index++) {
                String item = subjectFilter.getItemAt(index);
                if (item.equalsIgnoreCase(previous.toString())) {
                    subjectFilter.setSelectedItem(item);
                    return;
                }
            }
        }
        subjectFilter.setSelectedItem("All subjects");
    }

    private void refreshTable(UUID taskToSelect) {
        List<Task> visibleTasks = service.getVisibleTasks(
                searchField.getText(),
                selectedSubject(),
                selectedType(),
                selectedStatus(),
                selectedDifficulty(),
                selectedSortMode());
        tableModel.setTasks(visibleTasks, service.getHoursPerDay());
        table.clearSelection();
        updateSummary(visibleTasks);
        updateWorkloadWarnings();
        updateActionState();
        if (taskToSelect != null) {
            selectTask(taskToSelect);
        }
    }

    private void updateSummary(List<Task> visibleTasks) {
        List<Task> allTasks = service.getTasks();
        long total = allTasks.size();
        long done = allTasks.stream().filter(Task::isDone).count();
        long overdue = allTasks.stream().filter(Task::isOverdue).count();
        double completion = total == 0 ? 0.0 : (done * 100.0) / total;
        summaryLabel.setText(String.format(Locale.ENGLISH,
                "%d shown  |  %d total  |  %d done  |  %.0f%% complete  |  %d overdue",
                visibleTasks.size(), total, done, completion, overdue));
        summaryLabel.setForeground(overdue > 0 ? DANGER : new Color(51, 65, 85));
    }

    private void updateWorkloadWarnings() {
        List<WorkloadWarning> warnings = service.getWorkloadWarnings();
        if (warnings.isEmpty()) {
            warningArea.setForeground(SUCCESS);
            warningArea.setText(String.format(Locale.ENGLISH,
                    "Workload check: clear at %.1f study hours/day. "
                            + "No overloaded days or weeks.",
                    service.getHoursPerDay()));
            return;
        }

        StringBuilder text = new StringBuilder(String.format(Locale.ENGLISH,
                "Workload warnings at %.1f study hours/day (%d total):",
                service.getHoursPerDay(), warnings.size()));
        int shown = Math.min(3, warnings.size());
        for (int index = 0; index < shown; index++) {
            text.append("\n- ").append(warnings.get(index).describe());
        }
        warningArea.setForeground(DANGER);
        warningArea.setText(text.toString());
        warningArea.setCaretPosition(0);
    }

    private void updateCapacityLabel() {
        capacityLabel.setText(String.format(Locale.ENGLISH,
                "<html><div style='text-align:right; color:#cbd5e1;'>Study capacity<br>"
                        + "<span style='font-size:15px; color:#ffffff;'>"
                        + "<b>%.1f hours/day</b></span></div></html>",
                service.getHoursPerDay()));
    }

    private void updateActionState() {
        if (editButton == null || deleteButton == null || doneButton == null) {
            return;
        }
        Task selected = selectedTask();
        boolean hasSelection = selected != null;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        doneButton.setEnabled(hasSelection && !selected.isDone());
    }

    private Task selectedTask() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
            return null;
        }
        return tableModel.getTaskAt(modelRow);
    }

    private void selectTask(UUID id) {
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (tableModel.getTaskAt(modelRow).getId().equals(id)) {
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private String selectedSubject() {
        Object selected = subjectFilter.getSelectedItem();
        if (selected == null || selected.toString().startsWith("All ")) {
            return null;
        }
        return selected.toString();
    }

    private TaskType selectedType() {
        Object selected = typeFilter.getSelectedItem();
        if (selected == null || selected.toString().startsWith("All ")) {
            return null;
        }
        for (TaskType type : TaskType.values()) {
            if (type.getLabel().equals(selected.toString())) {
                return type;
            }
        }
        return null;
    }

    private TaskStatus selectedStatus() {
        Object selected = statusFilter.getSelectedItem();
        if (selected == null || selected.toString().startsWith("All ")) {
            return null;
        }
        for (TaskStatus status : TaskStatus.values()) {
            if (status.getLabel().equals(selected.toString())) {
                return status;
            }
        }
        return null;
    }

    private Difficulty selectedDifficulty() {
        Object selected = difficultyFilter.getSelectedItem();
        if (selected == null || selected.toString().startsWith("All ")) {
            return null;
        }
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty.getLabel().equals(selected.toString())) {
                return difficulty;
            }
        }
        return null;
    }

    private SortMode selectedSortMode() {
        Object selected = sortBox.getSelectedItem();
        return selected instanceof SortMode mode ? mode : SortMode.SMART_PRIORITY;
    }

    private void onAdd() {
        Task task = TaskDialog.showDialog(this, null, service.getHoursPerDay(),
                service.getSubjects());
        if (task == null) {
            return;
        }
        try {
            service.addTask(task);
            refreshAll(task.getId());
        } catch (RuntimeException exception) {
            showError("Could not add the task.", exception);
        }
    }

    private void onEdit() {
        Task selected = selectedTask();
        if (selected == null) {
            return;
        }
        Task edited = TaskDialog.showDialog(this, selected, service.getHoursPerDay(),
                service.getSubjects());
        if (edited == null) {
            return;
        }
        try {
            service.updateTask(edited);
            refreshAll(edited.getId());
        } catch (RuntimeException exception) {
            showError("Could not update the task.", exception);
        }
    }

    private void onDelete() {
        Task selected = selectedTask();
        if (selected == null) {
            return;
        }
        int option = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getTitle() + "\"?",
                "Delete task", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteTask(selected);
            refreshAll(null);
        } catch (RuntimeException exception) {
            showError("Could not delete the task.", exception);
        }
    }

    private void onMarkDone() {
        Task selected = selectedTask();
        if (selected == null || selected.isDone()) {
            return;
        }
        try {
            service.markDone(selected);
            refreshAll(selected.getId());
        } catch (RuntimeException exception) {
            showError("Could not mark the task as done.", exception);
        }
    }

    private void onReports() {
        new ReportsDialog(this, service).setVisible(true);
    }

    private void onSettings() {
        Double hoursPerDay = SettingsDialog.showDialog(this, service.getHoursPerDay());
        if (hoursPerDay == null) {
            return;
        }
        try {
            service.setHoursPerDay(hoursPerDay);
            updateCapacityLabel();
            refreshTable(null);
        } catch (RuntimeException exception) {
            showError("Could not save the settings.", exception);
        }
    }

    private void showError(String message, RuntimeException exception) {
        String detail = exception.getMessage() == null
                ? exception.toString()
                : exception.getMessage();
        JOptionPane.showMessageDialog(this, message + "\n" + detail,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JLabel filterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(71, 85, 105));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12.0f));
        return label;
    }

    private JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        return button;
    }

    private JButton successButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(22, 101, 52));
        button.setForeground(Color.WHITE);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(241, 245, 249));
        button.setForeground(NAVY);
        return button;
    }

    private JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.0f));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    private final class TaskCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable sourceTable, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    sourceTable, value, isSelected, hasFocus, row, column);
            int modelRow = sourceTable.convertRowIndexToModel(row);
            Task task = tableModel.getTaskAt(modelRow);

            setBorder(new EmptyBorder(0, 8, 0, 8));
            setHorizontalAlignment(column == 5 || column == 7
                    ? SwingConstants.RIGHT
                    : SwingConstants.LEFT);

            if (task.isOverdue()) {
                setBackground(isSelected ? new Color(254, 202, 202) : new Color(254, 242, 242));
                setForeground(new Color(153, 27, 27));
                setFont(sourceTable.getFont().deriveFont(Font.BOLD));
            } else if (task.isDone()) {
                setBackground(isSelected
                        ? sourceTable.getSelectionBackground()
                        : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                setForeground(isSelected ? sourceTable.getSelectionForeground() : MUTED);
                setFont(sourceTable.getFont().deriveFont(Font.ITALIC));
            } else {
                setBackground(isSelected
                        ? sourceTable.getSelectionBackground()
                        : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                setForeground(isSelected
                        ? sourceTable.getSelectionForeground()
                        : new Color(30, 41, 59));
                setFont(sourceTable.getFont());
            }
            return component;
        }
    }
}

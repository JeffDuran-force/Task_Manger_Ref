package studenttaskmanager.ui;

import studenttaskmanager.model.Task;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "Title", "Subject", "Type", "Difficulty", "Due date",
            "Hours", "Status", "Priority", "Start by"
    };
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private List<Task> tasks = new ArrayList<>();
    private double hoursPerDay = 3.0;

    public void setTasks(List<Task> tasks, double hoursPerDay) {
        this.tasks = new ArrayList<>(tasks);
        this.hoursPerDay = hoursPerDay;
        fireTableDataChanged();
    }

    public Task getTaskAt(int row) {
        return tasks.get(row);
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> task.getTitle();
            case 1 -> task.getSubject();
            case 2 -> task.getType().getLabel();
            case 3 -> task.getDifficulty().getLabel();
            case 4 -> DATE_FORMAT.format(task.getDeadline());
            case 5 -> String.format(Locale.ENGLISH, "%.1f", task.getEstimatedHours());
            case 6 -> task.isOverdue() ? "OVERDUE" : task.getStatus().getLabel();
            case 7 -> task.isDone()
                    ? "-"
                    : String.format(Locale.ENGLISH, "%.1f", task.priorityScore());
            case 8 -> suggestedStart(task);
            default -> "";
        };
    }

    private String suggestedStart(Task task) {
        if (task.isDone()) {
            return "-";
        }
        LocalDate suggested = task.suggestedStartDate(hoursPerDay);
        if (suggested.isBefore(LocalDate.now())) {
            return "Start now (" + DATE_FORMAT.format(suggested) + ")";
        }
        return DATE_FORMAT.format(suggested);
    }
}

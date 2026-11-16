package studenttaskmanager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import studenttaskmanager.data.CsvTaskRepository;
import studenttaskmanager.data.SettingsRepository;
import studenttaskmanager.service.TaskService;
import studenttaskmanager.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startApplication);
    }

    private static void startApplication() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Path dataDirectory = Paths.get("data");
            Files.createDirectories(dataDirectory);
            CsvTaskRepository taskRepository =
                    new CsvTaskRepository(dataDirectory.resolve("tasks.csv"));
            SettingsRepository settingsRepository =
                    new SettingsRepository(dataDirectory.resolve("settings.csv"));
            TaskService service = new TaskService(taskRepository, settingsRepository);
            new MainFrame(service).setVisible(true);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null,
                    "Could not start Student Task Manager.\n" + exception.getMessage(),
                    "Startup error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

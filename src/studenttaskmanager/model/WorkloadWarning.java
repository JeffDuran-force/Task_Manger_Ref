package studenttaskmanager.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class WorkloadWarning implements Reportable {
    public enum Scope {
        DAY,
        WEEK
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private final Scope scope;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final double totalHours;
    private final double limitHours;

    public WorkloadWarning(Scope scope, LocalDate periodStart, LocalDate periodEnd,
                           double totalHours, double limitHours) {
        this.scope = scope;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalHours = totalHours;
        this.limitHours = limitHours;
    }

    public Scope getScope() {
        return scope;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getLimitHours() {
        return limitHours;
    }

    @Override
    public String describe() {
        if (scope == Scope.DAY) {
            return String.format(Locale.ENGLISH,
                    "Daily overload on %s: %.1f h due (limit %.1f h).",
                    periodStart.format(DATE_FORMAT), totalHours, limitHours);
        }
        return String.format(Locale.ENGLISH,
                "Weekly overload %s to %s: %.1f h due (limit %.1f h).",
                periodStart.format(DATE_FORMAT), periodEnd.format(DATE_FORMAT),
                totalHours, limitHours);
    }
}

package ecs.systems;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class MyFormatter extends SimpleFormatter {
    private final String name;

    public MyFormatter(String n) {
        name = n;
    }

    @Override
    public String format(LogRecord record) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy -- HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return name + " :: " + record.getMessage() + " :: " + dtf.format(now) + "\n";
    }
}

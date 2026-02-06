package ton_core.shared;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String formatTime(Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("hh:mm a")
                .withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}

package ton_core.ui.models;

import java.time.Instant;

public class AIHistoryModel {
    public int id;
    public String message;
    public int type;
    public Instant time;
    public String result;
    public boolean isExpand;

    public AIHistoryModel(int id, String message, int type, Instant time, String result) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.time = time;
        this.result = result;
        this.isExpand = false;
    }
}

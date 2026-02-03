package ton_core.shared;

public class Constants {
    public static String TONGRAM_CONFIG = "TONGRAM_CONFIG";
    public static String TARGET_LANG_CODE_KEY = "TARGET_LANG_CODE_KEY";
    public static String TARGET_LANG_NAME_KEY = "TARGET_LANG_NAME_KEY";
    public static String OUT_MESSAGE_LANG_CODE_KEY = "OUT_MESSAGE_LANG_CODE_KEY";
    public static String OUT_MESSAGE_LANG_NAME_KEY = "OUT_MESSAGE_LANG_NAME_KEY";
    public static String IS_ENABLE_AI_TRANSLATION_KEY = "IS_ENABLE_AI_TRANSLATION_KEY";

    public enum AITypeId {
        TRANSLATION(0),
        TEMPLATE(1),
        IMPROVE(2),
        SUMMARY(3);

        public final int id;
        AITypeId(int id) {
            this.id = id;
        }
    }

    public enum AITemplateId {
        SET_MEETING(0),
        SAY_HI(1),
        SAY_THANKS(2),
        WRITE_EMAIL(3);

        public final int id;
        AITemplateId(int id) {
            this.id = id;
        }
    }

    public enum AIImproveId {
        MAKE_FORMAL(4),
        MAKE_FRIENDLY(5),
        FIX_GRAMMAR(6),
        MAKE_POLITE(7);

        public final int id;

        AIImproveId(int id) {
            this.id = id;
        }
    }

    public enum ToneKey {
        MAKE_FORMAL("formal"),
        MAKE_FRIENDLY("friendly"),
        MAKE_POLITE("polite");

        public final String key;

        ToneKey(String key) {
            this.key = key;
        }
    }
}

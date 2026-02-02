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
        MEETING_REQUEST(0),
        WRITE_GREETING(1),
        THANK_FOR_NOTE(2),
        WRITE_EMAIL(3);

        public final int id;

        AITemplateId(int id) {
            this.id = id;
        }
    }

    public enum AIImproveId {
        MAKE_PROFESSIONAL(0),
        MAKE_CASUAL(1),
        MAKE_POLITE(2),
        FIX_GRAMMAR_AND_SPELLING(3);

        public final int id;

        AIImproveId(int id) {
            this.id = id;
        }
    }

    public enum WriteAssistantType {
        MAKE_PROFESSIONAL("professional"),
        MAKE_CASUAL("casual"),
        MAKE_POLITE("polite");

        public final String key;

        WriteAssistantType(String key) {
            this.key = key;
        }
    }
}

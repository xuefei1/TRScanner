package trlabs.trscanner.utils;

/**
 * Created by intern2 on 11/12/2014.
 */
public final class Constants {
    private Constants() {
    }
    public static int MAX_JOURNAL_WORDS = 256;

    public static class Config {
        public static final boolean DEVELOPER_MODE = false;


        public static final int GUARD_THREAD_SLEEP_CYCLE = 3000;
    }

    public static class Extra {
        /** request from albulm */
        public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
        /** request from camera */
        public static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 1;
        /** crop request */
        public static final int REQUEST_CODE_GETIMAGE_BYCROP = 2;
        // change plus button icon in user fragment
        public static final int REQUEST_CODE_CHANGE_PLUS_BUTTON = 3;

        // Strings for intent msg
        public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
        public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
        public static final String IMAGE_DELETED = "com.nostra13.example.universalimageloader.IMAGE_DELETED";
        public static final String MEDIA_DIR = "com.nostra13.example.universalimageloader.MEDIA_DIR";
        public static final String REQUEST_CODE = "com.nostra13.example.universalimageloader.REQUEST_CODE";
        public static final int FROM_GRID = 1;
        public static final int FROM_LIST = 2;
        public static final String FILE_DESCRIPTOR = "file:///";
        public static final String NO_IMAGES_AVAILABLE = "Gallery Empty";
    }

    public static class FileTypes {
        public static final String Jpg = "jpg";
        public static final String txt = "txt";
        public static final String JournalType = "journal";
    }



}

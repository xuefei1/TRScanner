package trlabs.trscanner.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.webkit.MimeTypeMap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
/**
 * Created by jinxin on 2014-11-03.
 */
public final class FileTools {
    public static final String IMAGE_DIR_NAME = "TRSamples";
    /**
     * Private constructor.
     */
    private static final String LOG_TAG = FileTools.class.getSimpleName();

    private FileTools() {
    }

    public static File createFolder(String path){
        File folder = new File(path, getTimeStamp());
        //Create the storage directory if it does not exist
        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                Log.d("createFolder", "failed to create directory for storing matrix file");
                return null;
            }
        }
        return folder;
    }

    public static String createFile(String path, String fileName)  {
        // if folder exists
        File folder = new File(path);
        if (!folder.exists()) {
            createFolder(path);
        }
        File noMedia = new File(path + File.separator + fileName);
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
        return noMedia.getAbsolutePath();
    }

    public static File createRootPath(String path, String name) {
        File rootFolder = new File(path, name);
        //Create the storage directory if it does not exist
        if(!rootFolder.exists()) {
            if(!rootFolder.mkdirs()) {
                Log.d("createRootFolder", "failed to create root directory !");
                return null;
            }
        }
        return rootFolder;
    }

    public static boolean isEmptyDirectory(String path) {
        return ((new File(path)).list().length > 0);
    }

    public static String getFileName(String filePath) {
        if (StringUtils.isEmpty(filePath))
            return "";
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    public static String getAppDirectory(ContextWrapper cw){
        File mediaStorageDir;
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY); // works
        if (isSDPresent) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TRSamples");
            //Create the storage directory if it does not exist
            if(!mediaStorageDir.exists()) {
                if(!mediaStorageDir.mkdirs()) {
                Log.d("Samples", "failed to create directory for storing photos on SD card");
            }
        }
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "TRSamples";  // sd card dir
        } else {
            mediaStorageDir = cw.getDir("TRSamples", Context.MODE_PRIVATE);  // get path
            return  mediaStorageDir.getPath();
        }
    }
    public static List<String> getFilesNamesInDirectoryToList(String directoryPath) {
        List<String> filesNames = new LinkedList<String>();

        File directory = new File(directoryPath + File.separator);   // do not forget separator, otherwise NULL
        File[] files = directory.listFiles();
        Log.i("file size d is    ", Integer.toString(files.length));
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                // only add jpg files
                if (!files[i].isHidden() && getFileExtension(files[i].getName()).equals(Constants.FileTypes.Jpg )) {
                    filesNames.add(directoryPath + File.separator + files[i].getName());
                    //Log.i(LOG_TAG, filesNames.get(i));
                }
            }

        }
        return filesNames;
    }

    public static String[] getFilesNamesInDirectoryToArray(String directoryPath) {
        String [] filesNames = null;
        File directory = new File(directoryPath + File.separator);
        File[] files = directory.listFiles();


        if (files != null) {
            filesNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isHidden()) {
                    if (!files[i].isHidden() && getFileExtension(files[i].getName()).equals(Constants.FileTypes.Jpg )) {
                        filesNames[i] = directoryPath + File.separator + files[i].getName();
                        //Log.i(LOG_TAG, filesNames[i]);
                    }
                }
            }
        }
        return filesNames;
    }

    public static void delete(File folder) {
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }


    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    /* create an file for storage and return the path */
    public static File getOutputMediaFile(Context context) {
        // return unite file name using date time stamp

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY); // works
        File mediaStorageDir = null;

        if (isSDPresent)
        {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIR_NAME);
            //Create the storage directory if it does not exist
            if(!mediaStorageDir.exists()) {
                if(!mediaStorageDir.mkdirs()) {
                    Log.d("Samples", "failed to create directory for storing photos on SD card");
                    return null;
                }
            }

            return mediaStorageDir;

        } else
        {
            // context.mode_append, world_readable, world_writable. private
            mediaStorageDir = context.getDir(IMAGE_DIR_NAME, context.MODE_PRIVATE);  // get path
            return mediaStorageDir;    // if both storage failed
        }
    }



    public static String getTimeStamp(){
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return timestamp;
    }

    /* create a file with unique time stamp */
    public static File getFileStamped(File mediaStorage){
        if (mediaStorage != null) {
            return new File(mediaStorage.getPath() + File.separator + "IMG_"+ FileTools.getTimeStamp() + ".jpg");
        }
        return mediaStorage;
    }


    public static String getFileExtension(String fileName) {
        String encoded;
        try { encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"); }
        catch(UnsupportedEncodingException e) { encoded = fileName; }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }


    public void saveImage(Mat image, String filename, String directory) {
        String _filename = "nc_" + filename + ".jpg";
        File file = new File( directory, _filename);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException fnfe) {
            // TODO Auto-generated catch block
            fnfe.printStackTrace();
        }
        try {
            if (image.width() > 0) {
                Bitmap b = Bitmap.createBitmap(image.width(), image.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(image, b);
                b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
        } catch (IllegalArgumentException iex) {
            iex.printStackTrace();
        }

        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("lol","image saved!");
    }


    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            ex.printStackTrace();
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }


}

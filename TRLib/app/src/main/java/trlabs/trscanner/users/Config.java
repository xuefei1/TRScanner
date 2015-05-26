package trlabs.trscanner.users;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import trlabs.trscanner.utils.StringUtils;

// login state, guide page ...
public class Config {
    private final static String APP_CONFIG = "config";
    public final static String CONF_ACCESSTOKEN = "accessToken";
    public final static String CONF_ACCESSSECRET = "accessSecret";
    public final static String CONF_EXPIRESIN = "expiresIn";

    private Context mContext;
    private AccessInfo accessInfo = null;
    private static Config config;

    public static Config getAppConfig(Context context) {
        if (config == null) {
            config = new Config();
            config.mContext = context;
        }
        return config;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public AccessInfo getAccessInfo() {
        if (accessInfo == null && !StringUtils.isEmpty(getAccessToken())
                && !StringUtils.isEmpty(getAccessSecret())) {
            accessInfo = new AccessInfo();
            accessInfo.setAccessToken(getAccessToken());
            accessInfo.setAccessSecret(getAccessSecret());
            accessInfo.setExpiresIn(getExpiresIn());
        }
        return accessInfo;
    }
    public void setAccessInfo(String accessToken, String accessSecret,
                              long expiresIn) {
        if (accessInfo == null)
            accessInfo = new AccessInfo();
        accessInfo.setAccessToken(accessToken);
        accessInfo.setAccessSecret(accessSecret);
        accessInfo.setExpiresIn(expiresIn);
        this.setAccessToken(accessToken);
        this.setAccessSecret(accessSecret);
        this.setExpiresIn(expiresIn);
    }

    // Class Properties http://blog.csdn.net/u011819464/article/details/37593399
    public Properties get() {
        FileInputStream fis = null;
        Properties props = new Properties();
        try {
            File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            fis = new FileInputStream(dirConf.getPath() + File.separator
                    + APP_CONFIG);

            props.load(fis);
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return props;
    }

    public String get(String key) {
        Properties props = get();
        return (props != null) ? props.getProperty(key) : null;
    }


    private void setProps(Properties p) {
        FileOutputStream fos = null;
        try {
            File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
            File conf = new File(dirConf, APP_CONFIG);
            fos = new FileOutputStream(conf);

            p.store(fos, null);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }


    public void set(Properties ps) {
        Properties props = get();
        props.putAll(ps);
        setProps(props);
    }

    public void set(String key, String value) {
        Properties props = get();
        props.setProperty(key, value);
        setProps(props);
    }

    public void setAccessToken(String accessToken) {
        set(CONF_ACCESSTOKEN, accessToken);
    }

    public String getAccessToken() {
        return get(CONF_ACCESSTOKEN);
    }

    public void setAccessSecret(String accessSecret) {
        set(CONF_ACCESSSECRET, accessSecret);
    }

    public String getAccessSecret() {
        return get(CONF_ACCESSSECRET);
    }

    public void setExpiresIn(long expiresIn) {
        set(CONF_EXPIRESIN, String.valueOf(expiresIn));
    }

    public long getExpiresIn() {
        return StringUtils.toLong(get(CONF_EXPIRESIN));
    }
}

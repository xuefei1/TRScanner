package trlabs.trscanner.users;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import trlabs.trscanner.R;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.AppManager;
import trlabs.trscanner.utils.CircularImageView;
import trlabs.trscanner.utils.CryptoUtils;
import trlabs.trscanner.utils.FileTools;

public class User {
    Context mContext;
    private boolean login; // login state
    private int uid;
    private String name;
    private String address;
    private String city;
    private String province;
    private String country;
    private String phone;
    private int age;
    private String email;
    private String account;
    private String pwd;
    private String jointime;
    private String latestonline;
    private String gender;
    private String userIMGPath;
    private boolean isRememberMe;
    private SharedPreferences.Editor editor;   // save user preference
    private Activity mActivity;
    private boolean isLogout;
    public User(Context context){
        mContext = context;
        login = false;
        uid = 0;
    }

    public boolean getLoginState() {
        return this.login;
    }
    public void setLoginState(boolean state) {
        this.login = state;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getCity(){
        return city;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getProvince(){
        return province;
    }
    public void setProvince(String province){
        this.province = province;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail( String email) {
        this.email = email;
    }
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAccount(String account) {this.account = account; }

    public String getAccount() { return this.account; }
    public String getPwd() {
        return CryptoUtils.decode(GlobalConsts.SEED, pwd);
    }
    public void setPwd(String pwd) {
        this.pwd = CryptoUtils.encode(GlobalConsts.SEED, pwd);
    }
    public String getJointime() {
        return jointime;
    }
    public void setJointime(String jointime) {
        this.jointime = FileTools.getTimeStamp();
    }

    public String getLatestonline() {
        return latestonline;
    }
    public void setLatestonline(String latestonline) {
        this.latestonline = latestonline;
    }

    public void setGender(String gender){
        this.gender = gender;
    }
    public String getGender() {
        return this.gender;
    }
    public void setUserIMGPath(String userIMGPath) {
        this.userIMGPath = userIMGPath;
    }
    public String getUserIMGPath() {
        return this.userIMGPath;
    }
    public boolean getIsRememberMe() { return this.isRememberMe; }
    public void setIsRememberMe(boolean isRememberMe) { this.isRememberMe = isRememberMe; }
    public void setIsLogout(boolean isLogout){
        this.isLogout = isLogout;
    }
    public boolean getIsLogout() {
        return this.isLogout;
    }

    public void Logout() {
        this.editor.remove(GlobalConsts.USER_PREF_NAME);
        this.login = false;
        this.uid = 0;
        this.pwd = "";
        this.account = "";
        this.isLogout = true;
        setIsRememberMe(false);
    }

    public void Exit(){
        AppManager.getAppManager().AppExit(mContext);
    }

    public void setUserPreference(){
         this.editor = mContext.getSharedPreferences(GlobalConsts.USER_PREF_NAME, Context.MODE_PRIVATE).edit();
         editor.putInt("uid", this.getUid());
         editor.putString("name", this.getName());
         editor.putString("address", this.getAddress());
         editor.putString("email", this.getEmail());
         editor.putString("city", this.getCity());
         editor.putString("province", this.getProvince());
         editor.putString("country", this.getCountry());
         editor.putString("phone", this.getPhone());
         editor.putInt("age", this.getAge());
         editor.putString("gender", this.getGender());
         editor.putString("account", this.getAccount());
         editor.putString("pwd", this.pwd);
         editor.putBoolean("isRememberMe", this.getIsRememberMe());
         editor.putString("Lastestonline", this.getLatestonline());
         editor.commit();
    }

    public void getUserFromPreference() {
         SharedPreferences userPref = mContext.getSharedPreferences(GlobalConsts.USER_PREF_NAME, Context.MODE_PRIVATE);
         setUid(userPref.getInt("uid", 0));
         setName(userPref.getString("name", null));
         setAddress(userPref.getString("address", null));
         setEmail(userPref.getString("email", null));
         setCity(userPref.getString("city", null));
         setProvince(userPref.getString("province", null));
         setCountry(userPref.getString("country", null));
         setPhone(userPref.getString("phone", null));
         setAge(userPref.getInt("age", 0));
         setGender(userPref.getString("gender", null));
         setAccount(userPref.getString("account", null));
         pwd = userPref.getString("pwd", null);
         setLatestonline(userPref.getString("Lastestonline", null));
         setIsRememberMe(userPref.getBoolean("isRememberMe", true));
    }

    public boolean isUserPrefExist(){
        File pref = new File("/data/data/" + mContext.getPackageName() + "/shared_prefs/" + GlobalConsts.USER_PREF_NAME + ".xml" );
        return pref.exists();
    }

    public void saveUserprofileIMG(Bitmap bitmap){
        String path = UserProfileIMGPath();
        File pictureFile = new File(path + File.separator + "userIMG.png");
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("User", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("User", "Error accessing file: " + e.getMessage());
        }
    }

    public void setUserIMG(CircularImageView user_icon) {
        String path = UserProfileIMGPath();
        File image = new File(path + File.separator + "userIMG.png");
        if (image.exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            user_icon.setImageBitmap(bitmap);
        }
    }


    public Bitmap getUserIMG() {
        String path = UserProfileIMGPath();
        File image = new File(path + File.separator + "userIMG.png");
        if (image.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            return bitmap;
        }
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.qa_user);
    }

    private String UserProfileIMGPath(){
        ContextWrapper cw = new ContextWrapper(this.mContext);
        File directory = cw.getDir(GlobalConsts.UserProfileDir, Context.MODE_PRIVATE);
        if(!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.d("Error", "failed to create user directory");
            }
        }
        this.setUserIMGPath(directory.getAbsolutePath());
        return directory.getAbsolutePath();
    }

}

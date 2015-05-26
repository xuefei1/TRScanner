package trlabs.trscanner.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.config.GlobalConsts;
import trlabs.trscanner.ui.greendroid.QuickActionWidget;
import trlabs.trscanner.users.User;
import trlabs.trscanner.utils.FileTools;
import trlabs.trscanner.utils.StringUtils;
import trlabs.trscanner.utils.ToastUtil;
import trlabs.trscanner.utils.WiFiUtil;


// Android CLIENT PORT: partly consider the post http://blog.csdn.net/davidluo001/article/details/42460167
public class LoginDialog extends BaseActivity {

    private ViewSwitcher mViewSwitcher;
    private ImageButton btn_close;
    private Button btn_login;
    private AutoCompleteTextView mAccount;
    private EditText mPwd;
    private AnimationDrawable loadingAnimation;
    private View loginLoading;
    private int curLoginType;
    private InputMethodManager imm;
    private CheckBox cb_rememberMe;
    LoadingDialog pDialog;
    ScrollView scrollView;
    private Context mContext = this;

    public final static int LOGIN_OTHER = 0x00;
    public final static int LOGIN_MAIN = 0x01;
    public final static int LOGIN_SETTING = 0x02;


    private QuickActionWidget mGrid;
    TRScanner account;
    JSONObject jsonObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logindialog);
        account = (TRScanner) getApplication();
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        curLoginType = getIntent().getIntExtra("LOGINTYPE", LOGIN_OTHER);
        scrollView = (ScrollView) findViewById(R.id.login_scrollview);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.logindialog_view_switcher);
        loginLoading = findViewById(R.id.login_loading);
        mAccount = (AutoCompleteTextView) findViewById(R.id.login_account);
        mPwd = (EditText) findViewById(R.id.login_password);
        cb_rememberMe = (CheckBox)findViewById(R.id.login_checkbox_rememberMe);

        btn_close = (ImageButton) findViewById(R.id.login_close_button);
        btn_close.setOnClickListener(UIHelper.finish(this));
        btn_login = (Button) findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playWavId(R.raw.wavclick);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // user login entity
                String ac = mAccount.getText().toString();
                String pwd = mPwd.getText().toString();    // pin is encrypted AES128 when login succeeded
                boolean isRemmeberMe = cb_rememberMe.isChecked();

                if (StringUtils.isEmpty(ac)) {
                    Toast.makeText(v.getContext(), getString(R.string.msg_login_email_null), Toast.LENGTH_SHORT).show();
                    ViewShaker(scrollView);
                    return;
                }
                if (StringUtils.isEmpty(pwd)) {
                    Toast.makeText(v.getContext(), getString(R.string.msg_login_pwd_null), Toast.LENGTH_SHORT).show();
                    ViewShaker(scrollView);
                    return;
                }

                if (!WiFiUtil.isNetworkConnected(mContext)){
                    showAlert("Internet Disconnected", "please connect your device to internet");
                    return;
                }

                //mViewSwitcher.showNext();

                try {
                    new getUserLoginInfo().execute(ac, pwd, isRemmeberMe);   // login validation
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //DialogUtil.showSimpleAlertDialog(mContext,"id is","id " + account.getLoginUid(), DialogUtil.TYPE_UNDER_TEXT);
            }
        });


        //hide login info if user login info is already set
        if (account.user.isUserPrefExist()) {
            account.user.getUserFromPreference();
            if (account.user.getIsRememberMe() && !account.user.getIsLogout()) {
                try {
                    new getUserLoginInfo().
                            execute(account.user.getAccount(),
                                    account.user.getPwd(),
                                    account.user.getIsRememberMe());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        UIHelper.finish(this);
    }



    //login validation
    private void login(final String account, final String pwd) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    User user = (User) msg.obj;
                    if (user != null) {
                        //UIHelper.sendBroadCast(LoginDialog.this, user.getNotice());
                        ToastUtil.ToastMessage(LoginDialog.this, R.string.msg_login_success);

                        finish();
                    }
                } else if (msg.what == 0) {
                    mViewSwitcher.showPrevious();
                    btn_close.setVisibility(View.VISIBLE);
                    ToastUtil.ToastMessage(LoginDialog.this, getString(R.string.msg_login_fail) + msg.obj);
                } else if (msg.what == -1) {
                    mViewSwitcher.showPrevious();
                    btn_close.setVisibility(View.VISIBLE);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                /*
                try {
                    TRScanner ac = (TRScanner) getApplication();
                    User user = ac.loginVerify(account, pwd);
                    user.setAccount(account);
                    user.setPwd(pwd);
                    Result res = user.getValidate();
                    if(res.OK()){
                        ac.saveLoginInfo(user);
                        msg.what = 1;
                        msg.obj = user;
                    }else{
                        msg.what = 0;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                */
                handler.sendMessage(msg);
            }
        }.start();
    }


    // backspace key handler
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.onDestroy();
        }
        return super.onKeyDown(keyCode, event);
    }

    /* THIS PART CONTAINS JSON CONSTRUCTOR PHP FRAGMENT
      USERNAME  only check if the username exists
                    if(!$results){
                    //better error code can go in here
                    $rv["success"] = false;
                    $rv["error"] = "The username entered is not recognized";
                    echo json_encode($rv);
                    return;
                }
      PASSWORD
                   $rv["success"] = true;
                    $rv["id"] = $p[DB_COL_HEALTHCARE_NO];
                    echo json_encode($rv);
                    return;
                    exit;
                }else{
                    $rv["success"] = false;
                    $rv["error"] = "The password is not correct";
                    echo json_encode($rv);
                    return;
    */
    public void showAlert(final String title, final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(title);
                builder.setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }


    private void initLogin(String username, String password, boolean isRememberMe) {

        HttpPost httppost;
        StringBuffer buffer;
        HttpResponse httpResponse;
        HttpClient httpclient;
        List<NameValuePair> nameValuePairs;
        int timeoutConnection = 10*1000;
        int timeoutSocket = 10*1000;


        // http://www.coderzheaven.com/2012/04/22/create-simple-login-form-php-android-connect-php-android/
        try{
            // verify host and ID, verify site certificate, build secure connection SSL
            // http://stackoverflow.com/questions/2012497/accepting-a-certificate-for-https-on-android
            // Set the timeout in milliseconds until a connection is established.
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            // in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

            httpclient = new DefaultHttpClient(httpParams);
            httppost = new HttpPost(GlobalConsts.SERVER_ADDR + GlobalConsts.PHP_LOGIN);
            nameValuePairs = new ArrayList<NameValuePair>(2);

            // Always use the same variable name for posting i.e the android side variable name and php side variable name should be similar,
            nameValuePairs.add(new BasicNameValuePair("username", username.trim()));  // $Edittext_value = $_POST['Edittext_value'];
            nameValuePairs.add(new BasicNameValuePair("password", password.trim()));
            nameValuePairs.add(new BasicNameValuePair("action", "android-login"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            //Execute HTTP Post Request
            httpResponse = httpclient.execute(httppost);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String response = httpclient.execute(httppost, responseHandler);
            System.out.println("Response : " + response);

            runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("SERVER", "Response from PHP : " + response);
                }
            });

            jsonObject = new JSONObject(response).getJSONObject("Android");


            try {
                if (jsonObject.getBoolean("success")) {
                    account.user.setLoginState(true);
                    account.user.setUid(Integer.parseInt(jsonObject.getString("id")));
                    account.user.setAddress(jsonObject.getString("address"));
                    account.user.setCity(jsonObject.getString("city"));
                    account.user.setName(jsonObject.getString("name"));
                    account.user.setGender(jsonObject.getString("gender"));
                    account.user.setProvince(jsonObject.getString("province"));
                    account.user.setCountry(jsonObject.getString("country"));
                    account.user.setPhone(jsonObject.getString("phone"));
                    account.user.setAge(jsonObject.getInt("age"));
                    account.user.setEmail(jsonObject.getString("email"));
                    account.user.setAccount(username);
                    account.user.setPwd(password);
                    account.user.setIsRememberMe(isRememberMe);
                    account.user.setLatestonline(FileTools.getTimeStamp());
                    account.user.setIsLogout(false);
                    account.user.setUserPreference();
                } else{
                    ViewShaker(scrollView);
                    showAlert("Login Failed",jsonObject.getString("error"));
                }
            } catch (JSONException e) {
                Log.e("LoginDialog", "Json parser error");
                e.printStackTrace();
            }

            if(account.user.getLoginState()){
                runOnUiThread(new Runnable() {
                    public void run() {
                        GlobalConsts.isNewsFeeed = true;
                    }
                });
                finish();
            }

        }catch(IOException e){
            ViewShaker(scrollView);
            showAlert("Login Failed", e.getMessage());
            Log.d("Exception", e.getMessage());
        }catch (JSONException jex){
            ViewShaker(scrollView);
            showAlert("Login Failed", jex.getMessage());
            Log.d("Exception", jex.getMessage());
        }

    }

    private void ViewShaker(final ScrollView scrollView) {
        runOnUiThread(new Runnable() {
            public void run() {
                Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
                scrollView.startAnimation(shake);
            }
        });
    }



    private class getUserLoginInfo extends AsyncTask<Object, Void, Void> {

        boolean isLoginSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new LoadingDialog(LoginDialog.this);
            pDialog.setLoadText("Validating...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Object... arg0) {
            // parse server returned json file
            initLogin((String)arg0[0], (String)arg0[1], (Boolean)arg0[2]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (isLoginSuccess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // if login success, fetching user info run progress bar


                    }
                });
            }

        }
    }
}



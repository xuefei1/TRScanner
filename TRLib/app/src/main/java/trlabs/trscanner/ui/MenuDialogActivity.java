package trlabs.trscanner.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import trlabs.trscanner.R;
import trlabs.trscanner.TRScanner;
import trlabs.trscanner.trtabs.UserFragment;


public class MenuDialogActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout btn_return;
    private Button btn_logout;
    private Button btn_exit;
    TRScanner ac;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ac = (TRScanner) getApplication();
        setContentView(R.layout.menu_dialog);
        btn_return = (RelativeLayout)findViewById(R.id.menu_dialog_layout);
        btn_logout = (Button) findViewById(R.id.btn_logout);
        btn_exit = (Button) findViewById(R.id.btn_shutdown);
        btn_return.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_LONG).show();
                UserFragment.button.setImageDrawable(this.getResources().getDrawable(R.drawable.user_operation_more));
                ac.user.Logout();
                break;
            case R.id.menu_dialog_layout:
                UserFragment.button.setImageDrawable(getResources().getDrawable(R.drawable.user_operation_more));
                break;
            case R.id.btn_shutdown:
                Toast.makeText(this, "exit", Toast.LENGTH_LONG).show();
                ac.user.Exit();
                break;
        }
        playWavId(R.raw.wavclick);
        finish();
    }
}

package trlabs.trscanner.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import trlabs.trscanner.R;
import trlabs.trscanner.utils.StringUtils;
import trlabs.trscanner.utils.ToastUtil;

// add php file to submit feedback to trscanner server or insert it into feedback db mannually
// or set up an TRScanner email to collect feedback
public class Feedback extends BaseActivity {
    private ImageButton mClose;
    private EditText mEditer;
    private Button mSubmit;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.feedback);
        initView();
    }

    private void initView()
    {
        mClose = (ImageButton)findViewById(R.id.feedback_close_button);
        mEditer = (EditText)findViewById(R.id.feedback_content);
        mSubmit = (Button)findViewById(R.id.feedback_publish);

        mClose.setOnClickListener(UIHelper.finish(this));
        mSubmit.setOnClickListener(submitClickListener);
    }

    private View.OnClickListener submitClickListener = new View.OnClickListener() {
        public void onClick(final View v){
            playWavId(R.raw.wavclick);
            String content = mEditer.getText().toString();
            if (StringUtils.isEmpty(content)) {
                ToastUtil.showToast(v.getContext(), "feedback cannot be empty");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Animation shake = AnimationUtils.loadAnimation(v.getContext(), R.anim.shake);
                        v.startAnimation(shake);
                    }
                });
            } else {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("feedback");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@trtech.ca"});
                i.putExtra(Intent.EXTRA_SUBJECT, "TRScanner FeedBack -Android");
                i.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(i, "Sending mail...."));
                finish();
            }
        }
    };

}

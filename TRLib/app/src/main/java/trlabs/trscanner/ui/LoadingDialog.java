package trlabs.trscanner.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import trlabs.trscanner.R;

/**
 * Created by intern2 on 25/03/2015.
 */
public class LoadingDialog extends Dialog {
    private Context mContext;
    private LayoutInflater inflater;
    private WindowManager.LayoutParams lp;
    private TextView loadtext;

    public LoadingDialog(Context context) {
        super(context, R.style.Dialog);

        this.mContext = context;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.loading, null);
        loadtext = (TextView) layout.findViewById(R.id.loading_text);
        setContentView(layout);

        lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0;
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);

    }

    public void setLoadText(String content){
        loadtext.setText(content);
    }
}
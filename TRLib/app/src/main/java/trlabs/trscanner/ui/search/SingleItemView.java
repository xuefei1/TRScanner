package trlabs.trscanner.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import trlabs.trscanner.R;
import trlabs.trscanner.ui.BaseActivity;

public class SingleItemView extends BaseActivity {
    TextView txtDate;
    TextView txtContent;
    String date;
    String content;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.searchview_item);
        Intent i = getIntent();

        content = i.getStringExtra("content");
        date = i.getStringExtra("date");

        txtContent = (TextView) findViewById(R.id.search_content);
        txtDate = (TextView) findViewById(R.id.searchdate);

        txtContent.setText(content);
        txtDate.setText(date);
    }

}

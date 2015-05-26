package trlabs.trscanner.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import trlabs.trscanner.R;
import trlabs.trscanner.ui.search.JournalItem;
import trlabs.trscanner.ui.search.SearchViewAdapter;
import trlabs.trscanner.users.journals.JournalDB;


public class JournalSearch extends Activity{
    ListView list;
    SearchViewAdapter adapter;
    EditText searchtxt;
    JournalDB journalDB;


    ArrayList<JournalItem> arrayList = new ArrayList<JournalItem>();

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.search_main);
        journalDB = new JournalDB(this);
        List<Pair<String, String>> journalList;
        journalList = journalDB.getAllJournals();


        list = (ListView) findViewById(R.id.searchview);
        for (Pair<String, String> journal : journalList) {
            JournalItem ji = new JournalItem( journal.first, journal.second);
            arrayList.add(ji);
        }

        adapter = new SearchViewAdapter(this, arrayList);

        list.setAdapter(adapter);

        searchtxt = (EditText) findViewById(R.id.search);
        searchtxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                String text = searchtxt.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


}

package trlabs.trscanner.ui.search;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import trlabs.trscanner.R;

public class SearchViewAdapter extends BaseAdapter {
    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<JournalItem> journalList = null;
    private ArrayList<JournalItem> arraylist;

    public SearchViewAdapter(Context context, List<JournalItem> journalItems) {
        mContext = context;
        this.journalList = journalItems;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<JournalItem>();
        this.arraylist.addAll(journalList);
    }

    public class ViewHolder {
        TextView content;
        TextView date;
    }

    @Override
    public int getCount() {
        return journalList.size();
    }

    @Override
    public JournalItem getItem(int position) {
        return journalList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.searchview_item, null);
            holder.content = (TextView) view.findViewById(R.id.search_content);
            holder.date = (TextView) view.findViewById(R.id.searchdate);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.content.setText(journalList.get(position).getContent());
        holder.date.setText(journalList.get(position).getDate());

        // Listen for ListView Item Click
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Send single item click data to SingleItemView Class
                Intent intent = new Intent(mContext, SingleItemView.class);
                // Pass all data content
                intent.putExtra("content",(journalList.get(position).getContent()));
                // Pass all data date
                intent.putExtra("date",(journalList.get(position).getDate()));
                mContext.startActivity(intent);
            }
        });

        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        journalList.clear();
        if (charText.length() == 0) {
            journalList.addAll(arraylist);
        }
        else
        {
            for (JournalItem wp : arraylist)
            {
                if (wp.getContent().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    journalList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}

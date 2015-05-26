package trlabs.trscanner.users.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import trlabs.trscanner.R;


public class RssListAdapter extends ArrayAdapter<JSONObject> {

    TextView textView;
    ImageView imageView;
    JSONObject jsonImageText;
    View rowView;

    public RssListAdapter(Activity activity, List<JSONObject> imageAndTexts) {
        super(activity, 0, imageAndTexts);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Activity activity = (Activity) getContext();
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate the views from XML
        rowView = inflater.inflate(R.layout.news_element, parent, false);
        jsonImageText = getItem(position);
        new networkOps().execute();
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        //The next section we update at runtime the text - as provided by the JSON from our REST call
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        textView = (TextView) rowView.findViewById(R.id.newstitle);
        try {
            Spanned text = (Spanned) jsonImageText.get("text");
            textView.setText(text);

        }catch(JSONException e){
            e.printStackTrace();
        }
        return rowView;
    }

    class networkOps extends AsyncTask<Void,Void,Void>{
        Bitmap img;
        @Override
        protected Void doInBackground(Void...params){
            try {

                if (jsonImageText.get("imageLink") != null){

                    System.out.println("XXXX Link found!");
                    String url = (String) jsonImageText.get("imageLink");
                    URL feedImage= new URL(url);

                    HttpURLConnection conn= (HttpURLConnection)feedImage.openConnection();
                    InputStream is = conn.getInputStream();
                    img = BitmapFactory.decodeStream(is);

                }
            } catch (MalformedURLException e) {
                //handle exception here - in case of invalid URL being parsed
                //from the RSS feed item
            }
            catch (IOException e) {
                //handle exception here - maybe no access to web
            }catch(JSONException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            imageView = (ImageView) rowView.findViewById(R.id.newsImg);
            imageView.setImageBitmap(img);
        }

    }

}
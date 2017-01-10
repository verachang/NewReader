package com.jet.reader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private String TAG = "VERA-ReaderActivity";

    private int mIndex = 0;
    private boolean mStopflag = false;
    private ListView mListView;
    private WebView mWebview;
    private ArrayAdapter<String> mDataList;
    private HashMap mJsonmap;
    private GetData tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listview);
        mWebview = (WebView) findViewById(R.id.webview);

        mDataList = new ArrayAdapter<String>(this, R.layout.textviewlayout);
        mListView.setAdapter(mDataList);
        mListView.setOnItemClickListener(this);

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient());

        mJsonmap = new HashMap();

        StatGetDatafromURL();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                
                mIndex = 0;
                mDataList.clear();
                mDataList.notifyDataSetChanged();
                StatGetDatafromURL();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
        mListView.setVisibility(View.INVISIBLE);
        mWebview.setVisibility(View.VISIBLE);
        mWebview.loadUrl(mJsonmap.get(mListView.getItemAtPosition(index).toString()).toString());
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(mWebview.getVisibility() == View.INVISIBLE)
            {
                return super.dispatchKeyEvent(event);
            }
            mWebview.setVisibility(View.INVISIBLE);
            mListView.setVisibility(View.VISIBLE);
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    private void StatGetDatafromURL()
    {
        try {
            String A = new GetData().execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            String sub = A.substring(A.indexOf('[')+1, A.lastIndexOf(']')-1);
            Log.i(TAG, "sub = "+sub);
            String[] id = sub.split(",");

            for(String ids: id){
                GetData tmp =  new GetData();
                tmp.execute("https://hacker-news.firebaseio.com/v0/item/"+ids.substring(1, ids.length())+".json?print=pretty");
                if(mIndex == 100){
                    break;
                }
                
                mIndex++;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return;
    }

    public class GetData extends AsyncTask<String , Integer , String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                Log.i(TAG, "url = "+urls[0]);
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.i(TAG, "onProgressUpdate");
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "onPostExecute = ");
            super.onPostExecute(result);
            if(result != null && !result.startsWith("[")) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.has("url") && jsonObject.has("title"))
                    {
                        String tmpurl = jsonObject.getString("url");
                        String tmptitle = jsonObject.getString("title");

                        mJsonmap.put(tmptitle, tmpurl);

                        mDataList.add(tmptitle);

                        mDataList.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

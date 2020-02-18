package com.recluse.xicor.wikio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWikis();
    }

    public void refreshWikis(){
        DataRepo d = new DataRepo(getApplicationContext());
        LinkedHashMap<Integer,HashMap<String,String>> data = d.getWikis();
        ((LinearLayout) findViewById(R.id.wiki_container)).removeAllViews();
        for(Map.Entry<Integer,HashMap<String,String>> e:data.entrySet()){
            int id = e.getKey();
            HashMap<String,String> sub_data = e.getValue();


            LinearLayout ll = new LinearLayout(this);
            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);

            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0,0,0,dpAsPixels(2));
            ll.setLayoutParams(llp);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setBackgroundColor(Color.parseColor("#FFFFFF"));

            llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tv1.setTextColor(Color.parseColor("#000000"));
            CalligraphyUtils.applyFontToTextView(tv1, TypefaceUtils.load(getAssets(),"JosefinSans-Bold.ttf"));
            tv1.setPadding(dpAsPixels(4),dpAsPixels(5),dpAsPixels(4),dpAsPixels(5));
            tv1.setTextSize(18);
            tv1.setText(sub_data.get("name"));
            tv1.setLayoutParams(llp);


            llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            tv2.setTextColor(Color.parseColor("#000000"));
            CalligraphyUtils.applyFontToTextView(tv2, TypefaceUtils.load(getAssets(),"JosefinSans-Regular.ttf"));
            tv2.setPadding(dpAsPixels(4),0,dpAsPixels(4),dpAsPixels(5));
            tv2.setTextSize(15);
            tv2.setText(sub_data.get("summary"));
            tv2.setLayoutParams(llp);

            ll.addView(tv1);
            ll.addView(tv2);
            ((LinearLayout) findViewById(R.id.wiki_container)).addView(ll);
        }
    }

    protected int dpAsPixels(int no){
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (no*scale + 0.5f);
        return dpAsPixels;
    }

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setSP(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences("app_data", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getSP(String key) {
        SharedPreferences prefs = getSharedPreferences("app_data", MODE_PRIVATE);
        String restoredText = prefs.getString(key, null);
        return restoredText;
    }


    public void openWikipedia(View view) {
        startActivity(new Intent(this,WebViewActivity.class));
    }
}

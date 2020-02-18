package com.recluse.xicor.wikio;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class WebViewActivity extends AppCompatActivity {
    ArrayList<String> traversed_sites = new ArrayList<>();
    int presentindex = -1;
    String present_site = "";
    boolean prev_clicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        final WebView wbb = ((WebView) findViewById(R.id.webview));
        WebSettings wbset=wbb.getSettings();
        wbset.setJavaScriptEnabled(true);
        wbset.setDomStorageEnabled(true);
        wbset.setJavaScriptCanOpenWindowsAutomatically(false);
        wbset.setDefaultTextEncodingName("utf-8");
        wbset.setSupportMultipleWindows(false);
        wbset.setSupportZoom(true);
        wbset.setAllowFileAccessFromFileURLs(true);
        wbset.setAllowFileAccess(true);
        wbset.setLoadsImagesAutomatically(true);
        wbb.setVerticalScrollBarEnabled(true);
        wbb.setHorizontalScrollBarEnabled(false);
        ((FloatingActionButton) findViewById(R.id.download_wiki)).setVisibility(View.INVISIBLE);
        ((ImageButton) findViewById(R.id.webview_prev)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (presentindex > -1) {
                        prev_clicked = true;
                        wbb.loadUrl(traversed_sites.get(presentindex - 1));
                    }
                    Toast.makeText(getApplicationContext(), presentindex + "", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
        ((ImageButton) findViewById(R.id.webview_refresh)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String site = wbb.getUrl().toString();
                wbb.loadUrl(site);
            }
        });
        ((ImageButton) findViewById(R.id.webview_home)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wbb.loadUrl("https://www.wikipedia.org/");
            }
        });
        ((ImageButton) findViewById(R.id.webview_copy_link)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String site = wbb.getUrl().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("wiki_page", site);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(),"Wiki Link copied",Toast.LENGTH_LONG).show();
            }
        });
        ((FloatingActionButton) findViewById(R.id.download_wiki)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String site = wbb.getUrl().toString();
                Intent i = new Intent(WebViewActivity.this,DownloadWikiService.class);
                i.putExtra("wiki_site",site);
                startService(i);
            }
        });
        wbb.setWebViewClient(new WebViewClient(){
            @Override
            public void onLoadResource(WebView view, String url) {
                if(url.contains("wikipedia") == false){
                    wbb.stopLoading();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.contains("wikipedia") == false){
                    wbb.stopLoading();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("wikipedia") == false){
                    return true;
                }
                else {
                    return false;
                }
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                if(prev_clicked == false){
                    if(presentindex == traversed_sites.size()-1){
                        traversed_sites.add(url);
                        presentindex += 1;
                    }
                }
                else{
                    ArrayList<String> tts = new ArrayList<String>();
                    for(int i=0;i<=presentindex-1;i++){
                        tts.add(traversed_sites.get(i));
                    }
                    traversed_sites = tts;
                    prev_clicked = false;
                    presentindex = traversed_sites.size()-1;
                }
                if(url.contains("/wiki/") && url.contains("Main_Page") == false){
                    if(new DataRepo(getApplicationContext()).isLinkThere(url) == false){
                        ((FloatingActionButton) findViewById(R.id.download_wiki)).setVisibility(View.VISIBLE);
                    }
                    else{
                        ((FloatingActionButton) findViewById(R.id.download_wiki)).setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    ((FloatingActionButton) findViewById(R.id.download_wiki)).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        wbb.loadUrl("https://www.wikipedia.org/");
    }

    public void openWVHome(View view) {
        startActivity(new Intent(this,MainActivity.class));
    }
}

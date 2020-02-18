package com.recluse.xicor.wikio;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ROJIT on 6/21/2018.
 */

public class DownloadWikiService extends Service {
    String wiki_site = "";
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent !=null && intent.getExtras()!=null){
            wiki_site = intent.getExtras().getString("wiki_site");
            if(wiki_site.equals("") == false){
                if(wiki_site.contains("/wiki/")){
                    Toast.makeText(getApplicationContext(),"Download started for "+wiki_site.replace("en.m.","en."),Toast.LENGTH_LONG).show();
                    new GetWikiPage(wiki_site.replace("en.m.","en.")).execute();
                }
            }
        }
        return Service.START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        final DownloadWikiService s = this;

    }

    public boolean isInternetAvailable() {
        return true;
    }


    public void setSP(String key,String value){
        SharedPreferences.Editor editor = getSharedPreferences("app_data", MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getSP(String key){
        SharedPreferences prefs = getSharedPreferences("app_data", MODE_PRIVATE);
        String restoredText = prefs.getString(key, null);
        return restoredText;
    }

    class GetWikiPage extends AsyncTask<Void,Void,Void> {
        String html = "";
        String error = "";
        String topic_name = "";
        String summary = "";
        String site = "";
        int r_no = 0;
        GetWikiPage(String site){
            this.site = site;
            r_no = new Random().nextInt(1000);
        }
        ArrayList<String> img_links = new ArrayList<String>();
        ArrayList<String> img_texts = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            android.support.v4.app.NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
            nb.setContentTitle("WikiDownload Started")
                    .setContentText(site)
                    .setSmallIcon(R.mipmap.ic_launcher);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(r_no, nb.build());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                Document doc = Jsoup.connect(site)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .timeout(30000)
                        .get();
                Element main_div = doc.select(".mw-parser-output").get(0);
                Elements childrens = main_div.children();
                String base_html = "";
                for(Element child:childrens){
                    if(child.tagName().equals("p")){
                        if(summary.equals("")){
                            summary = replaceQuotations(child.text());
                        }
                        base_html += "<p>"+replaceQuotations(child.text())+"</p>\n";
                    }
                    else if(child.tagName().substring(0,1).equals("h")){
                        if(replaceQuotations(child.text()).equals("References") ||replaceQuotations(child.text()).equals("See also") ){
                            break;
                        }
                        else{
                            base_html += "<"+child.tagName()+">"+replaceQuotations(child.text())+"</"+child.tagName()+">\n";
                        }
                    }
                    else if(child.tagName().equals("div") && child.attr("class").contains("thumb")){
                        String image_text = replaceQuotations(child.text());
                        Element image = child.select("img").get(0);
                        String image_link = image.attr("src");
                        try{
                            String image_srcset = image.attr("srcset");
                            if(image_srcset != null){
                                String image_resized = image_srcset.split(",")[1].substring(0,image_srcset.split(",")[1].lastIndexOf(" ")).trim();
                                image_link = image_resized;
                            }
                        }
                        catch(Exception e){image_link = image.attr("src");}
                        image_link = "http:"+image_link;
                        base_html += "<img src='"+image_link+"'/><p style='text-align:center;'><i>"+image_text+"</i></p>\n";
                    }
                    else if(child.tagName().equals("table") && child.attr("class").contains("infobox")){
                        child.attr("height","100%");
                        Elements tab_images = child.select(".image");
                        for(Element tab_image:tab_images){
                            String image_link = tab_image.attr("src");
                            if(image_link.contains(".svg") == false){
                                try{
                                    String image_srcset = tab_image.select("img").get(0).attr("srcset");
                                    if(image_srcset != null){
                                        String image_resized = image_srcset.split(",")[1].substring(0,image_srcset.split(",")[1].lastIndexOf(" ")).trim();
                                        image_link = image_resized;
                                    }
                                }
                                catch(Exception e){image_link = tab_image.select("img").get(0).attr("src");}
                                Elements parents = tab_images.parents();
                                String im_text = "";
                                for(Element parent:parents){
                                    if(parent.tagName().equals("tr")) {
                                        String imtext = parent.text().trim();
                                        if (imtext != null && imtext.equals("") == false) {
                                            im_text = imtext;
                                        }
                                        try{parent.remove();}catch(Exception e){}
                                    }
                                }
                                img_texts.add(im_text);
                                img_links.add(image_link);
                            }
                        }
                        Elements trs = child.select("tr");
                        for(Element tr:trs){
                            tr.attr("style",tr.attr("style")+";background-color:#80000000!important;");
                        }
                        trs = child.select("img");
                        for(Element tr:trs){
                            tr.remove();
                        }
                        trs = child.select("th");
                        for(Element tr:trs){
                            tr.attr("style",tr.attr("style")+";background-color:#80000000!important;");
                        }
                        trs = child.select("td");
                        for(Element tr:trs){
                            tr.attr("style",tr.attr("style")+";background-color:#80000000!important;");
                        }
                        trs = child.select("a");
                        for(Element tr:trs){
                            tr.attr("href","");
                            tr.tagName("span");
                        }
                        base_html += "<table style='width:100%;border:2px solid black;border-radius:9px;' cellpadding='10'>"+replaceQuotations(child.html())+"</table>\n";
                    }
                    else{
                        if(child.id().equals("toc") || child.className().equals("hatnote") || child.attr("role").equals("note") || child.tagName().equals("table")){

                        }
                        else{
                            Elements trs = child.select("a");
                            for(Element tr:trs){
                                tr.attr("href","");
                                tr.tagName("span");
                            }
                            trs = child.select("dt");
                            for(Element tr:trs){
                                tr.tagName("b");
                            }
                            base_html += replaceQuotations(child.html());
                        }
                    }
                }

                Element header_element = doc.getElementById("firstHeading");
                topic_name = header_element.text().trim();


                String fontFile = "file:///android_asset/Fea-Regular.otf";
                String head = "<head>" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                        "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\""+fontFile+"\")}body {font-family: MyFont;font-size: medium;text-align: justify;background-color:#FFFFFF}body>img{width:100%;}tr{background-color:transparent!important;}td{background-color:transparent!important;}th{background-color:transparent!important;}</style>"+
                        "</head>";
                String f_images = "";
                for(String img_link:img_links){
                    f_images += "<img src='http:"+img_link+"'/>\n<p style='text-align:center;'><i>"+img_texts.get(img_links.indexOf(img_link))+"</i></p>\n";
                }
                //html = "<html>\n"+head+"<body><h1>"+topic_name+"</h1></hr>\n"+f_images+base_html+"</body>\n</html>";
                html = "<body><h1>"+topic_name+"</h1></hr>\n"+f_images+base_html+"</body>\n</html>";
            }
            catch(Exception e){
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(error.equals("")){
                new DataRepo(getApplicationContext()).insertWiki(topic_name,summary,site,html);
                android.support.v4.app.NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
                nb.setContentTitle("WikiDownload Completed")
                        .setContentText(topic_name)
                        .setSmallIcon(R.mipmap.ic_launcher);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(r_no, nb.build());
            }
            else{
                Toast.makeText(getApplicationContext(),error,Toast.LENGTH_LONG).show();
                android.support.v4.app.NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
                nb.setContentTitle("WikiDownload Error")
                        .setContentText(error)
                        .setSmallIcon(R.mipmap.ic_launcher);
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(r_no, nb.build());
            }

        }
    }

    public static String replaceQuotations(String s){
        if(s.contains("[")){
            String ps = s.replace(s.substring(s.indexOf("["),s.indexOf("]")+1),"");
            if(ps.contains("[")){
                ps = replaceQuotations(ps);
                return ps;
            }
            else{
                return ps;
            }
        }
        else{
            return s;
        }
    }


}

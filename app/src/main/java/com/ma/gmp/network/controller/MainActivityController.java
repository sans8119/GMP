package com.ma.gmp.network.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.ma.gmp.GMPApplication;
import com.ma.gmp.network.NetworkHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MainActivityController {
    private final String TAG="MainActivityController";

    public interface Result {
        void setSuccessResultData(Object[] data);
        void setFailureResultData(String message);
    }

    public void getUrlData(String url, final Activity context, final Result result) {
        NetworkHandler handler = ((GMPApplication) context.getApplication()).getNetworkHandlerInstance();
        final NetworkHandler.NetworkResult networkResult = new NetworkHandler.NetworkResult() {
            String[] data;
            public void setSuccessResultData(String responseString) {
                if (responseString.length() > 0) {
                   data= parseHtmlString(responseString,context,this);
                }
            }

            public void setFailureResultData(String jsonString) {
               Log.d(TAG,"Failure response: "+jsonString);
            }

            public void setImage(Bitmap bitmap) {
                Log.d(TAG,"bitmap response: "+bitmap);
                result.setSuccessResultData(new Object[]{data[0],data[1],bitmap});

            }
        };
        handler.makeGetRequestForString(url,"cancel",context,3,networkResult);
    }

    private String[] parseHtmlString(String htmlString, Activity context,NetworkHandler.NetworkResult networkResult){
        Document doc = Jsoup.parse(htmlString);
        String title = doc.title(); doc.data();
        String body=doc.body().text();
        body=(body.length()>10)?body.substring(0,10):body;
        String imageUrl="";
        Elements pngs = doc.select("img[src$=.png]");
        if(!pngs.isEmpty() && pngs.get(0).hasAttr("src")){
               imageUrl= pngs.get(0).attr("src");
            ((GMPApplication) context.getApplication()).getNetworkHandlerInstance().getImage(imageUrl,context,title,
                    networkResult);
        }

        Log.d(TAG,"parseHtmlString: "+title+", "+body+" ,"+imageUrl);
        return new String[]{title,body};

    }

}

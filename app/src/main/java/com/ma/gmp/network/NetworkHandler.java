package com.ma.gmp.network;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ma.gmp.GMPApplication;
import com.ma.gmp.R;
import com.ma.gmp.utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class NetworkHandler {
    private final String TAG = "NetworkHandler";
    private final ImageCache imageCache;
    private RequestQueue mRequestQueue;
    private boolean jwtTokenBeingRetrieved;
    private Utils utils;


    public NetworkHandler() {
        imageCache = new ImageCache();
        utils = new Utils();
    }

    public ImageCache getImageCache() {
        return imageCache;
    }

    public void putImage(String imageName, Bitmap bitmap) {
        if (imageCache.get(imageName) != null && bitmap != null) {
            imageCache.remove(imageName);
        }
        imageCache.put(imageName, bitmap);
    }

    public Bitmap getBitmap(String url) {
        return imageCache.get(url);
    }


    private RequestQueue getRequestQueue(final Context applicationContext) {
        if (mRequestQueue == null) {
            HurlStack hurlStack = new HurlStack() {
                @Override
                protected HttpURLConnection createConnection(URL url) throws IOException {
                    Log.d(TAG, "createConnection for " + url + ", " + url.getAuthority() + ", " + url.getProtocol());
                    if (url.getProtocol().equals("http")) {
                        HttpURLConnection httpURLConnection = (HttpURLConnection)
                                super.createConnection(url);
                        return httpURLConnection;

                    } else {
                        HttpsURLConnection httpsURLConnection = (HttpsURLConnection)
                                super.createConnection(url);
                        return httpsURLConnection;
                    }
                }
            };
            mRequestQueue = Volley.newRequestQueue(applicationContext, hurlStack);
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req, String tag, Context applicationContext) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(applicationContext).add(req);
    }

    private <T> void addToRequestQueue(Request<T> req, Context applicationContext) {
        req.setTag(TAG);
        getRequestQueue(applicationContext).add(req);
    }


    public void makeGetRequestForString(final String url, final String cancelTag, final Context context,
                                        final int repeatCount, final NetworkResult networkResult) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        networkResult.setSuccessResultData(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    if (error.networkResponse != null)
                        Log.d(TAG, "Failure Response for GET: " + error.networkResponse.statusCode + ", Error Response--->" + error.getMessage() + ", " + new String(error.networkResponse.data));
                    else
                        Log.d(TAG, "Failure Response for GET: " + error.getMessage() + ", ");
                }
                if (repeatCount > 0) {
                    int repeat = repeatCount - 1;
                    makeGetRequestForString(url, cancelTag, context, repeat, networkResult);
                } else {
                    String message;
                    if (error.networkResponse == null) {
                        message = context.getResources().getString(R.string.connect_device_to_network);
                    } else {
                        String errorMsg;
                        if (error.getMessage() != null)
                            errorMsg = error.getMessage();
                        message = new String(error.networkResponse.data);
                    }
                    networkResult.setFailureResultData(message);
                    Log.d(TAG, url + "<--url,Error Response of the GET Request--->" + error.getMessage() + ", " + message);
                }
            }
        });
        addToRequestQueue(stringRequest, cancelTag, context);
    }

    public void getImage(final String url, final Activity applicationContext, final String imageName, final NetworkResult networkResult) {
        final ImageLoader imageLoader = new ImageLoader(getRequestQueue(applicationContext), imageCache);
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Image Load Error: " + error.getMessage());
                networkResult.setFailureResultData(url);
                if (error.networkResponse != null)
                    Log.d(TAG, "Failure Response while downloading image: " + error.networkResponse.statusCode + ", Error Response--->" + error.getMessage() + ", " + new String(error.networkResponse.data));
                else
                    Log.d(TAG, "Failure Response while downloading image: " + error.getMessage());
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null)
                    Log.d(TAG, bitmap.getByteCount() + "-----------onResponse of Image download-----------------" + arg1);
                if (bitmap != null) {
                    Log.d(TAG, "onResponse of getImage:" + bitmap);
                    bitmap = utils.resizeImage(bitmap, applicationContext);
                    NetworkHandler.this.putImage(imageName, bitmap);
                    imageCache.put(imageName, bitmap);
                    utils.storeDataInDb(imageName, bitmap, (GMPApplication) applicationContext.getApplication());
                    //networkResult.setSuccessResultData(imageName);
                    networkResult.setImage(bitmap);

                }
            }
        });
    }


    public interface NetworkResult {
        void setSuccessResultData(String message);

        void setFailureResultData(String message);

        void setImage(Bitmap image);
    }

}
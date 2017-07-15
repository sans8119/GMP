package com.ma.gmp;

import android.app.Application;
import android.util.Log;
import com.ma.gmp.network.NetworkHandler;
import com.ma.gmp.utils.Utils;

public class GMPApplication extends Application {
    private static final String TAG = "Application";
    private NetworkHandler networkHandlerInstance;
    private Utils utils;

    public NetworkHandler getNetworkHandlerInstance() {
        if (networkHandlerInstance == null) {
            synchronized (GMPApplication.class) {
                networkHandlerInstance = new NetworkHandler();
            }
        }
        Log.d(TAG, " NetworkHandler mobileKeysApiFacadeInstance=" + networkHandlerInstance);
        return networkHandlerInstance;
    }

    public Utils getUtilsInstance() {
        if(utils==null)
            utils=new Utils();
        return utils;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        getNetworkHandlerInstance();
    }

}

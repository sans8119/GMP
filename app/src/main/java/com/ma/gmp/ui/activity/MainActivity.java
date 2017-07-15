package com.ma.gmp.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ma.gmp.network.controller.MainActivityController;
import com.ma.gmp.R;
import com.ma.gmp.utils.Constants;

public class MainActivity extends AppCompatActivity {
    private final String TAG="MainActivity";
    private  MainActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller=new MainActivityController();
        MainActivityController.Result result=new MainActivityController.Result(){

            @Override
            public void setSuccessResultData(Object[] data) {
                Log.d(TAG,"Data obtained in View: Title="+data[Constants.INDEX_TITLE]+", body="+data[Constants.INDEX_BODY]+", image="+data[Constants.INDEX_BITMAP]);
            }

            @Override
            public void setFailureResultData(String message) {

            }
        };
        controller.getUrlData("http://www.bbc.com/",this,result);
    }
}

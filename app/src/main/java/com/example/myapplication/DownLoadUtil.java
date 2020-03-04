package com.example.myapplication;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoadUtil {
    public static void doDownLoadApk(String path){
        try{
            URL url=new URL(path);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("accept","");
            httpURLConnection.setRequestProperty("","");
            httpURLConnection.connect();
        }catch (Exception e){
            Log.e("exception",e.toString());

        }

    }
}

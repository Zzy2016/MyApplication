package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.PermissionRequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.sql.BatchUpdateException;

public class MainActivity extends AppCompatActivity {

    float totalSize;
    float currentSize;
    String path = "";
    File apkFile;
    String TAG = "CheckForUpdate";
    Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doPermission();
        doDownloadApk();

    }


    public void doPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {

        }
    }

    public void checkVersion() {

    }

    public void doDownloadApk() {
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "*/*");
            connection.connect();
            File file = getExternalFilesDir("");
            Log.e(TAG, "文件" + file.getAbsolutePath());
            apkFile = new File(file, "test.apk");
            Log.e(TAG, "文件1" + apkFile.getAbsolutePath() + "  " + apkFile.exists());
            if (!apkFile.exists()) {
                try {
                    apkFile.createNewFile();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[1024];
                int length = 0;
                totalSize = connection.getContentLength();
                FileOutputStream fileOutputStream = new FileOutputStream(apkFile);
                Message message = new Message();
                message.what = 0;
                while ((length = bufferedInputStream.read(buffer)) != 0) {
                    fileOutputStream.write(buffer, 0, length);
                    currentSize += length;
                    int percent = (int) Math.floor(currentSize / totalSize) * 100;
                    message.arg1 = percent;
                    handler.sendMessage(message);

                }

            } else {
                Log.e(TAG, "网络异常");
            }
        } catch (Exception e) {
            Log.e("异常", e.toString());
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.arg1 == 100) {
                        doInstallApk();
                    } else {

                    }
                    break;
            }
        }
    };

    public void doInstallApk(){
        Intent intent=new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.N){
            uri= FileProvider.getUriForFile(MainActivity.this,getPackageName()+".myFilePRovider",apkFile);
            intent.setDataAndType(uri,"application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            intent.setDataAndType(uri,"application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
    }

}

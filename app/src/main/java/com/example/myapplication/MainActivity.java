package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    String path = "https://huidr.oss-cn-hangzhou.aliyuncs.com/apk/DoctorApp-release.apk";
    File apkFile;
    String TAG = "CheckForUpdate";
    Uri uri;
    TextView textView;
    Dialog dialog;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textview);

        initDialog();
        doPermission();


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doDownloadApk();
                    }
                }).start();
            }
        });

    }

    public void initDialog() {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_layout, null);
        progressBar = view.findViewById(R.id.progressbar);
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (display.getWidth() * 0.8);
        window.setAttributes(lp);
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
                byte[] buffer = new byte[1024 * 8];
                int length = 0;
                totalSize = connection.getContentLength();
                FileOutputStream fileOutputStream = new FileOutputStream(apkFile);

                while ((length = bufferedInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, length);
                    currentSize += length;
                    Message message = new Message();
                    message.what = 0;
                    message.arg1 = (int) Math.floor((currentSize / totalSize) * 100);
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
                    progressBar.setProgress(msg.arg1);
                    if (msg.arg1 == 100) {
                        doInstallApk();
                        dialog.cancel();
                    }
                    break;
            }
        }
    };

    public void doInstallApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".myFileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
}

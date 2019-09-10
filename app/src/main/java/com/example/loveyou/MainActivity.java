package com.example.loveyou;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.loveyou.service.Myservice;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static DownloadManager downloadManager;
    private static long id;
    private EditText etv;
    private int time;
    private int versioncode;
    private String versionname;
    private String versioninfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etv = findViewById(R.id.input_time);
        TextView versiontv = findViewById(R.id.version);
        version();
        versiontv.setText("当前版本："+versionname);
        checkversion(0);
    }

    //开启背词服务
    public void startservice(View view) {
        Intent intent = new Intent(MainActivity.this, Myservice.class);
        String et = etv.getText().toString();
        //是否设置间隔时间
        if (null == et || "".equals(et)) {
            et = "0";
        }
        time = Integer.parseInt(et);
        intent.putExtra("time", time);
        startService(intent);
    }
    //关闭背词服务
    public void closeservice(View view) {
        Intent intent = new Intent(MainActivity.this, Myservice.class);
        stopService(intent);
    }
    // 弹窗权限列表
    public void getpower(View view) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        startActivity(intent);
    }

   //自检无更新不弹窗  isdo  1：弹出  0：不弹出
    public void checkupdata(View view) {
        checkversion(1);
    }

    //检查服务器安装包版本
    private void checkversion(final int isdo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String Url = "http://back.lovedan.xyz/api/androidupdata?versionCode="+versioncode+"&versionName="+versionname;
                try {
                    URL url = new URL(Url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if(responseCode ==200){
                        InputStream is = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = null;
                        while((line=reader.readLine()) != null){
                            stringBuilder.append(line);
                        }
                        String responseText = stringBuilder.toString();
                        //获取返回值
                        if(responseText.equals("0")){
                            if(isdo==1){
                                Looper.prepare();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("已经是最新版本！");
                                builder.create().show();
                                Looper.loop();
                            }
                        }else{
                            JSONObject jsonObject= new JSONObject(responseText);
                            versioninfo = (String) jsonObject.get("versionInfo");
                            Looper.prepare();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("发现最新版本")
                                    .setMessage(versioninfo)
                                    .setNegativeButton("忽略",null)
                                    .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            downLoadApk();
                                        }
                                    })
                                    .setCancelable(false);
                            builder.create().show();
                            Looper.loop();
                        }
                    }else {
                        //请求失败
                        Looper.prepare();
                        Toast.makeText(MainActivity.this,"网络异常！",Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //当前软件版本
    private String version() {
        try {
            PackageManager packageManager = this.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            versioncode = packInfo.versionCode;
            versionname = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //从服务器下载安装包
    private void downLoadApk() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://test.lovedan.xyz/app-release.apk"));
        request.setTitle("LOVE YOU");
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "update.apk");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        id = downloadManager.enqueue(request);
    }
}


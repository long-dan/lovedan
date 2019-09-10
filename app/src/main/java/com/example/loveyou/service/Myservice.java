package com.example.loveyou.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.example.loveyou.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Myservice extends Service {
    Handler handler = new Handler();
    private String[] jsons;
    private String word;
    private String sentence_trans;
    private String sentence;
    private String mean_cn;
    private int countfile = 0;
    private View view;
    private int time;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        getjsonfile();
        jumpwindow();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle gettime = intent.getExtras();
        time = gettime.getInt("time");
        //未设置时间默认间隔时间为10分钟
        if (time == 0){
            time = 10;
            Toast.makeText(Myservice.this,"您未设置时间，已更改为默认间隔时间10分钟",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(Myservice.this,"您设置的间隔时间为"+time+"分钟",Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

    }
//    弹出单词窗口
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            view =  View.inflate(Myservice.this,R.layout.activity_dialog,null);
            AlertDialog.Builder builder = new AlertDialog.Builder(Myservice.this);
            builder.setView(view);
            builder.setNegativeButton("一会儿再看", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(Myservice.this,"你就这样无情的抛弃了我，哼，我还会回来的！",Toast.LENGTH_LONG).show();
                    dialogInterface.dismiss();
//                    时间单位转换为分钟
                    handler.postDelayed(runnable,time*60000);
                    getword();
                }
            });
           builder.setPositiveButton("下一个",null);
            builder.setCancelable(false);  // 点击返回键dialog不消失
                                          //dialog.setCanceledOnTouchOutside(false);点击返回键消失，根据需求更改
            AlertDialog dialog = builder.create();
            if (Build.VERSION.SDK_INT > 23){//6.0
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }  else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);}
            dialog.show();
//            button事件阻断dialog消失
            if(dialog.getButton(AlertDialog.BUTTON_POSITIVE)!=null) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getword();
                        changeword();
                    }});

                }
            changeword();
        }
    };
//点击查看翻译
    private void changeword() {
        final TextView tv_title =view.findViewById(R.id.dialog_title);
        final TextView tv_msg = view.findViewById(R.id.dialog_msg);
        tv_title.setText(word);
        tv_msg.setText(sentence);
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_title.getText()==word){
                    tv_title.setText(mean_cn);
                }else{
                    tv_title.setText(word);
                }
            }
        });
        tv_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_msg.getText()==sentence){
                    tv_msg.setText(sentence_trans);
                }else{
                    tv_msg.setText(sentence);
                }
            }
        });
    }
//    读取所有单词json数据
    private void getjsonfile() {
        jsons = null;
        AssetManager assets = null;
        assets = getAssets();
        try {
            jsons = assets.list("all_json");
//            我个人使用词库来源于百词斩app词库，由于非授权获取，此处只放上三个test数据
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//弹出窗口
    private void jumpwindow() {
        getword();
        handler.postDelayed(runnable,1000);
    }
    //解析单词json数据内容
    private void getword() {
        InputStream is = null;
        try {
            if (countfile>=jsons.length){
                countfile=0;
            }
            is = getAssets().open("all_json/"+jsons[countfile]);
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                switch(name){
                    case "word":
                        word = reader.nextString();
                        break;
                    case "mean_cn":
                        mean_cn = reader.nextString();
                        break;
                    case "sentence":
                        sentence = reader.nextString();
                        break;
                    case "sentence_trans":
                        sentence_trans = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            countfile +=1;
            reader.endObject();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

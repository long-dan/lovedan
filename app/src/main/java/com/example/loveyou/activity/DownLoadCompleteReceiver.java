package com.example.loveyou.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;

public class DownLoadCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Toast.makeText(context, "安装包下载完成！", Toast.LENGTH_SHORT).show();
            DownloadManager.Query query=new DownloadManager.Query();
            DownloadManager dm= (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            query.setFilterById(id);
            Cursor c = dm.query(query);
            if (c!=null){
                try {
                    if (c.moveToFirst()){
                        String filename = null;
                        String fileUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        //获取文件下载路径
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            if (fileUri != null) {
                                filename = Uri.parse(fileUri).getPath();
                            }
                        } else {
                            //Android 7.0以上的方式：请求获取写入权限，这一步报错
                            filename= c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        }
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status==DownloadManager.STATUS_SUCCESSFUL){
                            Uri uri = Uri.fromFile(new File(filename));
                            if (uri!=null){
                                Intent install=new Intent(Intent.ACTION_INSTALL_PACKAGE);
//                                版本适配
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                } else {
                                    // 声明需要的临时的权限
                                    install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(context, com.example.loveyou.BuildConfig.APPLICATION_ID + ".fileprovider",new File(filename));
                                    install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                                }
                                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(install);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }finally {
                    c.close();

                }

            }

        }

    }

}

package com.google.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SplashActivity extends Activity {
    private static String URL = "http://192.168.43.249:8080/json.json";
    private static  String SAVE = "sdcard/Safe.apk";
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
    }

    private void initView() {
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText("版本：" + PackageUtil.getVersionName(this));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                update();
            }
        },2000);
    }

    private void update() {
        HttpUtils httpUtils = new HttpUtils(2000);
        httpUtils.send(HttpMethod.GET, URL, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                processJson(result);
                Log.e("EEEEEEEEEE","成功了");
            }
            @Override
            public void onFailure(HttpException e, String s) {
                //跳转到主界面
                Log.e("EEEEEEEE","失败了" + e.toString());
            }
        });
    }
        int mNewCode;
        String mNewUrl;
        String mNewMsg;
    protected  void processJson(String result){
        try {
            JSONObject jsonObject = new JSONObject(result);
            mNewCode = jsonObject.getInt("code");
            mNewUrl = jsonObject.getString("url");
            mNewMsg = jsonObject.getString("text");
            if (mNewCode == PackageUtil.getVersionCode(this)){
                //获得的版本和现在的一样 不下载，跳转主界面
            }else {
                showUpdateDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);

        builder.setTitle("发现最新版本:" + mNewCode)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(mNewMsg)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        //取消，会到主界面
                    }
                }).setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        download();
                    }
                }).setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                //跳回主界面
                            };
                        }.sendEmptyMessageDelayed(0,2000);
                    }
                }).show();

    }
    protected  void  download(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            showProgress();
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.download(mNewUrl, SAVE, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    progressDialog.dismiss();
                    try {
                        install();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    progressDialog.dismiss();
                    Log.e("eee","失败了");
                    //跳转主界面
                }

                public void onLoading(long total,long current,boolean isUploding){
                    try {
                        super.onLoading(total, current, isUploding);
                        progressDialog.setMax((int) total);
                        progressDialog.setProgress((int) current);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }else {
            Toast.makeText(this,"没有发现可用的SD卡",Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void install(){
       /* Intent intent = new Intent();
        intent.setAction("android.intent.action.View");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File(SAVE)) ,"application/vnd.android.package-archive");
        startActivityForResult(intent,0);*/
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 添加flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(SAVE)),"application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //不安装，回到主界面
    }
    private  void showDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}

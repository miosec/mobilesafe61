package org.miosec.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.miosec.mobilesafe.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

	protected static final int GOTO_MAIN_UI = 1;
	protected static final int SHOW_UPDATA_DIALOG = 2;
	private TextView tv_version;
	private int versionCode;
	private int serverCode;
	private String desc;
	private String update_url;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GOTO_MAIN_UI:
				Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
				startActivity(intent);
				break;
			case SHOW_UPDATA_DIALOG:
					showUpDialog();
				break;
			default:
				break;
			}
	
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_version = (TextView) findViewById(R.id.tv_version);

		try {
			// 显示版本
			versionCode = (getPackageManager().getPackageInfo(getPackageName(),
					0)).versionCode;
			tv_version.setText(versionCode + "");
			// 检查版本更新
			checkUpdata();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}


	protected void showUpDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("新版本:"+serverCode);
		builder.setMessage(desc);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				installAPK();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					gotoActivity(SplashActivity.this,"HomeActivity");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		builder.show();
	}


	protected void gotoActivity(Activity activity,String otherActivity) throws ClassNotFoundException {
		Intent intent = new Intent(activity,Class.forName(otherActivity));
		startActivity(intent);
	}


	protected void installAPK() {
		
	}


	public void downloadAPK() {
		System.out.println("downloadAPK");
			
	}
	private void checkUpdata() {

		new Thread() {
			

			private long startTime;

			public void run() {
				Message message = Message.obtain();
				try {
					startTime = SystemClock.currentThreadTimeMillis();
					URL url = new URL(getResources().getString(
							R.string.update_url));
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					int code = conn.getResponseCode();
					if (code == 200) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(conn.getInputStream()));
						StringWriter sw = new StringWriter();
						String str = null;
						while((str = br.readLine())!=null){
							sw.write(str);
						}
						sw.close();
						br.close();
						String result = sw.toString();
						JSONObject jsonObject = new JSONObject(result);
						serverCode = jsonObject.getInt("code");
						desc = jsonObject.getString("desc");
						update_url = jsonObject.getString("update_url"); 
						
						System.out.println(serverCode);
						System.out.println(desc);
						System.out.println(update_url);
						if(versionCode == serverCode){
							message.what = GOTO_MAIN_UI;
						}else{
							message.obj = serverCode;
							message.what = SHOW_UPDATA_DIALOG;
						}
					} else {
						showToast(SplashActivity.this, "错误码:2001 请与客服练习");
						message.what = GOTO_MAIN_UI;
					}
				} catch (MalformedURLException e) {
					showToast(SplashActivity.this, "错误码:2002 请与客服练习");
					message.what = GOTO_MAIN_UI;
					e.printStackTrace();
				} catch (IOException e) {
					showToast(SplashActivity.this, "错误码:2003 请与客服练习");
					message.what = GOTO_MAIN_UI;
					e.printStackTrace();
				} catch (JSONException e) {
					showToast(SplashActivity.this, "错误码:2004 请与客服练习");
					message.what = GOTO_MAIN_UI;
					e.printStackTrace();
				}finally{
					long endTime = SystemClock.currentThreadTimeMillis();
					if(endTime - startTime < 2000){
					SystemClock.sleep(startTime+2000 - endTime);
					}
					handler.sendMessage(message);
				}
				
			}
		}.start();
		
	}
	public void showToast(final Context context,final String msg){
		if("main".equals(Thread.currentThread().getName())){
			Toast.makeText(context, msg,Toast.LENGTH_SHORT).show();
		}else{
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}

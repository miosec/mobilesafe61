package org.miosec.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.miosec.mobilesafe.R;
import org.miosec.mobilesafe.R.id;
import org.miosec.mobilesafe.R.layout;
import org.miosec.mobilesafe.R.string;

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
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case GOTO_MAIN_UI:
				Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
				startActivity(intent);
				break;
			case SHOW_UPDATA_DIALOG:
				AlertDialog.Builder builder = new Builder(SplashActivity.this);
				builder.setTitle(serverCode);
				builder.setMessage(desc);
				builder.setPositiveButton("ȷ��", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
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
			// ��ʾ�汾
			versionCode = (getPackageManager().getPackageInfo(getPackageName(),
					0)).versionCode;
			tv_version.setText(versionCode + "");
			// ���汾����
			checkUpdata();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
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
							message.what = SHOW_UPDATA_DIALOG;
						}
					} else {
						showToast(SplashActivity.this, "������:2001 ����ͷ���ϰ");
						message.what = GOTO_MAIN_UI;
					}
				} catch (MalformedURLException e) {
					showToast(SplashActivity.this, "������:2002 ����ͷ���ϰ");
					message.what = GOTO_MAIN_UI;
					e.printStackTrace();
				} catch (IOException e) {
					showToast(SplashActivity.this, "������:2003 ����ͷ���ϰ");
					message.what = GOTO_MAIN_UI;
					e.printStackTrace();
				} catch (JSONException e) {
					showToast(SplashActivity.this, "������:2004 ����ͷ���ϰ");
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
			Toast.makeText(context, msg, 0).show();
		}else{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(context, msg, 0).show();
				}
			});
		}
	}
}

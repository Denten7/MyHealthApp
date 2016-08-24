package com.ihealth.devices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.example.devicelibtest.R;
import com.ihealth.communication.control.Bp550BTControl;
import com.ihealth.communication.control.Bp7sControl;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.main.BackgroundWorker;
import com.ihealth.main.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for testing Bp7 device. 
 */
public class Bp550BT extends Activity implements OnClickListener{

	private static final String TAG = "Bp550BT";
	private Bp550BTControl bp550BTControl;
	private String deviceMac;
	private int clientCallbackId;
	private TextView tv_return;
	TextView baterija;
	EditText opis;
	TextView sis;
	TextView dia;
	TextView utrip;
	Button button;
	String hightPressure;
	String lowPressure;
	String pulseWave;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bp550_bt);
		Intent intent = getIntent();
		deviceMac = intent.getStringExtra("mac");

		baterija = (TextView)findViewById(R.id.baterijaView);
		opis = (EditText)findViewById(R.id.editText);
		sis = (TextView)findViewById(R.id.sisView);
		dia = (TextView)findViewById(R.id.diaView);
		utrip = (TextView)findViewById(R.id.utripView);
		button = (Button)findViewById(R.id.button1);

		button.setOnClickListener(this);
		
		clientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(iHealthDevicesCallback);
		/* Limited wants to receive notification specified device */
		iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(clientCallbackId, iHealthDevicesManager.TYPE_550BT);
		/* Get bp7 controller */
		bp550BTControl = iHealthDevicesManager.getInstance().getBp550BTControl(deviceMac);

		if(bp550BTControl != null) {
			bp550BTControl.getBattery();
		}
		else {
			Toast.makeText(this, "bp550BTControl == null", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		iHealthDevicesManager.getInstance().unRegisterClientCallback(clientCallbackId);
	}

	private iHealthDevicesCallback iHealthDevicesCallback = new iHealthDevicesCallback() {

		@Override
		public void onDeviceConnectionStateChange(String mac,
				String deviceType, int status, int errorID) {
			Log.i(TAG, "mac: " + mac);
			Log.i(TAG, "deviceType: " + deviceType);
			Log.i(TAG, "status: " + status);
		}

		@Override
		public void onUserStatus(String username, int userStatus) {
			Log.i(TAG, "username: " + username);
			Log.i(TAG, "userState: " + userStatus);
		}

		@Override
		public void onDeviceNotify(String mac, String deviceType,
				String action, String message) {
			Log.i(TAG, "mac: " + mac);
			Log.i(TAG, "deviceType: " + deviceType);
			Log.i(TAG, "action: " + action);
			Log.i(TAG, "message: " + message);
			
			if(BpProfile.ACTION_BATTERY_BP.equals(action)){
				try {
					JSONObject info = new JSONObject(message);
					String battery =info.getString(BpProfile.BATTERY_BP);
					Message msg = new Message();
					msg.what = HANDLER_MESSAGE;
					msg.obj = "battery: " + battery;
					String bat = "Stanje baterije: "+battery+"%";
					baterija.setText(bat);
					myHandler.sendMessage(msg);
					bp550BTControl.getOfflineData();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			}else if(BpProfile.ACTION_ERROR_BP.equals(action)){
				try {
					JSONObject info = new JSONObject(message);
					String num =info.getString(BpProfile.ERROR_NUM_BP);
					Message msg = new Message();
					msg.what = HANDLER_MESSAGE;
					msg.obj = "error num: " + num;
					myHandler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}else if(BpProfile.ACTION_HISTORICAL_DATA_BP.equals(action)){
				String str = "";
				try {
					JSONObject info = new JSONObject(message);
					if (info.has(BpProfile.HISTORICAL_DATA_BP)) {
			            JSONArray array = info.getJSONArray(BpProfile.HISTORICAL_DATA_BP);
			            for (int i = 0; i < array.length(); i++) {
			            	JSONObject obj = array.getJSONObject(i);
			            	String date          = obj.getString(BpProfile.MEASUREMENT_DATE_BP);
			            	hightPressure = obj.getString(BpProfile.HIGH_BLOOD_PRESSURE_BP);
			            	lowPressure   = obj.getString(BpProfile.LOW_BLOOD_PRESSURE_BP);
			            	pulseWave     = obj.getString(BpProfile.PULSEWAVE_BP);
			            	String ahr           = obj.getString(BpProfile.MEASUREMENT_AHR_BP);
			            	String hsd           = obj.getString(BpProfile.MEASUREMENT_HSD_BP);
			            	str = "date:" + date
			            			+ "hightPressure:" + hightPressure + "\n"
			            			+ "lowPressure:" + lowPressure + "\n"
			            			+ "pulseWave" + pulseWave + "\n"
			            			+ "ahr:" + ahr + "\n"
			            			+ "hsd:" + hsd + "\n";
							String sis1 = "Sistolični: "+hightPressure;
							sis.setText(sis1);
							String dia1 = "Diastolični: "+lowPressure;
							dia.setText(dia1);
							String utrip1 = "Utrip: "+pulseWave;
							utrip.setText(utrip1);
			            }
			        }
					Message msg = new Message();
					msg.what = HANDLER_MESSAGE;
					msg.obj =  str;
					myHandler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}else if(BpProfile.ACTION_HISTORICAL_NUM_BP.equals(action)){
				try {
					JSONObject info = new JSONObject(message);
					String num = info.getString(BpProfile.HISTORICAL_NUM_BP);
					Message msg = new Message();
					msg.what = HANDLER_MESSAGE;
					msg.obj = "num: " + num;
					myHandler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		}
	};
	
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		/*case R.id.btn_getbattery:
			if(bp550BTControl != null)
				bp550BTControl.getBattery();
			else
				Toast.makeText(this, "bp550BTControl == null", Toast.LENGTH_LONG).show();
			break;
			
		case R.id.btn_getOfflineNum:
			if(bp550BTControl != null)
				bp550BTControl.getOfflineNum();
			else
				Toast.makeText(this, "bp550BTControl == null", Toast.LENGTH_LONG).show();
			break;*/
			
		case R.id.button1:
			String opis1 = opis.getText().toString();
			if(opis1.equals("")){
				Toast.makeText(this, "Prosim vpišite vaše počutje.", Toast.LENGTH_LONG).show();
			}
			else if(hightPressure == null || lowPressure == null || pulseWave == null){
				Toast.makeText(this, "Najprej opravite meritev z napravo.", Toast.LENGTH_LONG).show();
			}
			else{

				BackgroundWorker backgroundWorker = new BackgroundWorker(this, new BackgroundWorker.AsyncResponse() {
					@Override
					public void processFinish(String s, final Context ctx) {
						if(s.equals("Failed")){
							Toast.makeText(ctx, "Napaka pri prenosu podatkov!", Toast.LENGTH_LONG).show();
							return;
						}
						Toast.makeText(ctx, "Podatke ste uspešno shranili.", Toast.LENGTH_SHORT).show();

						final Intent intent = new Intent(ctx, MainActivity.class);
						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								ctx.startActivity(intent);
							}
						}, 2000);

					}
				});
				SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

				String auth = sharedPref.getString("auth", null);
				String username = sharedPref.getString("username", null);
				String urlAddr = "https://myhealthapp-denten7.c9users.io/Android/postBPData";

				backgroundWorker.execute(hightPressure, lowPressure, pulseWave, opis1, auth, username, urlAddr);
			}
			/*if(bp550BTControl != null) {
				bp550BTControl.getOfflineData();
				bp550BTControl.getBattery();
			}
			else {
				Toast.makeText(this, "bp550BTControl == null", Toast.LENGTH_LONG).show();
			}*/
			break;
			
		/*case R.id.btn_disconnect:
			if(bp550BTControl != null)
				bp550BTControl.disconnect();
			else
				Toast.makeText(this, "bp550BTControl == null", Toast.LENGTH_LONG).show();
			break;*/
		default:
			break;
		}
	}
	
	private static final int HANDLER_MESSAGE = 101;
	Handler myHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case HANDLER_MESSAGE:   
                       //tv_return.setText((String)msg.obj);
                       break;   
             }   
             super.handleMessage(msg);   
        }   
   };

	@Override
	public void onBackPressed() {
		bp550BTControl.disconnect();
		finish();
	}

}

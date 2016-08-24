/**
 * @title
 * @Description
 * @author
 * @date 2015年11月18日 下午11:07:52 
 * @version V1.0  
 */

package com.ihealth.devices;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;

import com.example.devicelibtest.R;
import com.ihealth.communication.cloud.tools.HttpPost;
import com.ihealth.communication.control.Po3Control;
import com.ihealth.communication.control.PoProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.main.BackgroundWorker;
import com.ihealth.main.MainActivity;


import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class Po3 extends Activity implements OnClickListener {
    //private TextView tv_return;
    private String deviceMac;
    private static String TAG = "Po3";
    private int clientId;
    private Po3Control mPo3Control;
    private ArrayList<Integer> spO2 = new ArrayList<>();
    private ArrayList<Integer> bpm = new ArrayList<>();

    Button button;
    Button button1;
    TextView opis;
    TextView procenti;
    ProgressBar progressBar;
    String strI;
    String strII;
    EditText opis1;
    TextView koncaj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_po3);
        initView();
        Intent intent = getIntent();
        deviceMac = intent.getStringExtra("mac");
        clientId = iHealthDevicesManager.getInstance().registerClientCallback(mIHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(clientId,
                iHealthDevicesManager.TYPE_PO3);
        /* Get po3 controller */
        mPo3Control = iHealthDevicesManager.getInstance().getPo3Control(deviceMac);
        Log.d(TAG, "deviceMac:" + deviceMac + "--mPo3Control:" + mPo3Control);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        iHealthDevicesManager.getInstance().unRegisterClientCallback(clientId);

    }

    private void initView() {

        button = (Button)findViewById(R.id.btn_startMeasure);
        button.setOnClickListener(this);
        button1 = (Button)findViewById(R.id.koncajPregled);
        button1.setOnClickListener(this);

        opis = (TextView)findViewById(R.id.textView2);
        procenti = (TextView)findViewById(R.id.textView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        opis1 = (EditText)findViewById(R.id.editText2);
        koncaj = (TextView)findViewById(R.id.textView3);


        progressBar.setMax(100);
        opis.setVisibility(View.GONE);
        procenti.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        button1.setVisibility(View.GONE);
        opis1.setVisibility(View.GONE);
        koncaj.setVisibility(View.GONE);
        //findViewById(R.id.btn_getBattery).setOnClickListener(this);
    }

    iHealthDevicesCallback mIHealthDevicesCallback = new iHealthDevicesCallback() {
        public void onScanDevice(String mac, String deviceType) {
        };

        public void onDeviceConnectionStateChange(String mac, String deviceType, int status) {
            Log.e(TAG, "mac:" + mac + "-deviceType:" + deviceType + "-status:" + status);
            noticeString = "device disconnect";
            mPo3Control = null;
            Message message2 = new Message();
            message2.what = 1;
            message2.obj = noticeString;
            mHandler.sendMessage(message2);

            switch (status) {
                case iHealthDevicesManager.DEVICE_STATE_DISCONNECTED:
                    mPo3Control=null;
                    Toast.makeText(Po3.this, "The device disconnect", Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;
            }
        };

        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.d(TAG, "mac:" + mac + "--type:" + deviceType + "--action:" + action + "--message:" + message);
            JSONTokener jsonTokener = new JSONTokener(message);
            switch (action) {
                case PoProfile.ACTION_OFFLINEDATA_PO:
                    try {
                        JSONObject object = (JSONObject) jsonTokener.nextValue();
                        JSONArray jsonArray = object.getJSONArray(PoProfile.OFFLINEDATA_PO);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            String dateString = jsonObject.getString(PoProfile.MEASURE_DATE_PO);
                            int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                            int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                            JSONArray jsonArray1 = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                            int[] wave = new int[jsonArray1.length()];
                            for (int j = 0; j < jsonArray1.length(); j++) {
                                wave[j] = jsonArray1.getInt(j);
                            }
                            Log.i(TAG, "date:" + dateString + "--oxygen:" + oxygen + "--pulseRate:" + pulseRate
                                    + "-wave1:"
                                    + wave[0]
                                    + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
                        }
                        Message message2 = new Message();
                        message2.what = 1;
                        message2.obj = message;
                        mHandler.sendMessage(message2);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_LIVEDA_PO:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                        int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                        float PI = (float) jsonObject.getDouble(PoProfile.PI_PO);
                        JSONArray jsonArray = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                        int[] wave = new int[3];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            wave[i] = jsonArray.getInt(i);
                        }
                        Log.i(TAG, "oxygen:" + oxygen + "--pulseRate:" + pulseRate + "--Pi:" + PI + "-wave1:" + wave[0]
                                + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
                        Log.i(TAG, "Pri pridobivaju sem v ACTION_LIVEDA_PO");
                        Message message3 = new Message();
                        message3.what = 1;
                        message3.obj = message;
                        mHandler.sendMessage(message3);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_RESULTDATA_PO:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        int oxygen = jsonObject.getInt(PoProfile.BLOOD_OXYGEN_PO);
                        int pulseRate = jsonObject.getInt(PoProfile.PULSE_RATE_PO);
                        float PI = (float) jsonObject.getDouble(PoProfile.PI_PO);
                        JSONArray jsonArray = jsonObject.getJSONArray(PoProfile.PULSE_WAVE_PO);
                        int[] wave = new int[3];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            wave[i] = jsonArray.getInt(i);
                        }
                        Log.i(TAG, "oxygen:" + oxygen + "--pulseRate:" + pulseRate + "--Pi:" + PI + "-wave1:" + wave[0]
                                + "-wave2:" + wave[1] + "--wave3:" + wave[2]);
                        Log.i(TAG, "Pri pridobivaju sem v ACTION_RESULTDATA_PO");
                        Message message3 = new Message();
                        message3.what = 1;
                        message3.obj = message;
                        mHandler.sendMessage(message3);

                        strI = Integer.toString(oxygen);
                        strII = Integer.toString(pulseRate);
                        //new RESTcall().execute(strI, strII);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case PoProfile.ACTION_NO_OFFLINEDATA_PO:
                    noticeString = "no history data";
                    Message message2 = new Message();
                    message2.what = 1;
                    message2.obj = noticeString;
                    mHandler.sendMessage(message2);
                    break;
                case PoProfile.ACTION_BATTERY_PO:
                    JSONObject jsonobject;
                    try {
                        jsonobject = (JSONObject) jsonTokener.nextValue();
                        int battery = jsonobject.getInt(PoProfile.BATTERY_PO);
                        Log.d(TAG, "battery:" + battery);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Message message3 = new Message();
                    message3.what = 1;
                    message3.obj = message;
                    mHandler.sendMessage(message3);
                    break;
                default:
                    break;
            }
        };
    };
    String noticeString = "";
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    //tv_return.setText((String) msg.obj);
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_startMeasure:
                if (mPo3Control == null) {
                    Log.i(TAG, "mPo3Control == null");
                    Toast.makeText(Po3.this, "mPo3Control == null", Toast.LENGTH_LONG).show();

                } else {
                    button.setVisibility(View.GONE);
                    opis.setVisibility(View.VISIBLE);
                    procenti.setVisibility(View.VISIBLE);
                    procenti.setText("0");
                    progressBar.setVisibility(View.VISIBLE);
                    mPo3Control.startMeasure();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(progressBar.getProgress() < 100){
                                int a = progressBar.getProgress();
                                progressBar.setProgress(a+1);
                                procenti.setText(Integer.toString(a+1));
                                handler.postDelayed(this, 70);
                            }
                            else{
                                mPo3Control.disconnect();
                                opis.setVisibility(View.GONE);
                                procenti.setVisibility(View.GONE);
                                progressBar.setVisibility(View.GONE);
                                opis1.setVisibility(View.VISIBLE);
                                koncaj.setVisibility(View.VISIBLE);
                                button1.setVisibility(View.VISIBLE);
                                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 1000);
                                toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 3000);
                            }
                        }
                    }, 70);

                }
                break;
            case R.id.koncajPregled:
                String opisP = opis1.getText().toString();

                if(opisP.equals("")){
                    Toast.makeText(this, "Prosim vpišite vaše počutje.", Toast.LENGTH_LONG).show();
                }
                else{
                    restCall(opisP);
                }


                break;
            case R.id.btn_disconnect:
                if (mPo3Control == null) {
                    Log.i(TAG, "mPo3Control == null");
                    Toast.makeText(Po3.this, "mPo3Control == null", Toast.LENGTH_LONG).show();

                } else {
                    mPo3Control.disconnect();
                }
                break;
            default:
                break;

        }
    }

    private void restCall(String opisP1){
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
        String urlAddr = "https://myhealthapp-denten7.c9users.io/Android/postSpO2";

        backgroundWorker.execute(strI, strII, opisP1, auth, username, urlAddr);
    }

    public void onBackPressed() {
        mPo3Control.disconnect();
        finish();
    }

}

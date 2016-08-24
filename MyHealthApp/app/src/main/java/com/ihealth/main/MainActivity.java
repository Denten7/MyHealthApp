
package com.ihealth.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.example.devicelibtest.R;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.devices.ABI;
import com.ihealth.devices.AM3;
import com.ihealth.devices.AM3S;
import com.ihealth.devices.AM4;
import com.ihealth.devices.BG1;
import com.ihealth.devices.BG5;
import com.ihealth.devices.BP3L;
import com.ihealth.devices.BP3M;
import com.ihealth.devices.BP5;
import com.ihealth.devices.BP7;
import com.ihealth.devices.Bp550BT;
import com.ihealth.devices.Bp7s;
import com.ihealth.devices.Bp926;
import com.ihealth.devices.HS3;
import com.ihealth.devices.HS4;
import com.ihealth.devices.HS4S;
import com.ihealth.devices.HS5BT;
import com.ihealth.devices.HS5wifi;
import com.ihealth.devices.HS6;
import com.ihealth.devices.Po3;
import com.ihealth.utils.MyLog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;

/**
 * Activity for scan and connect available iHealth devices.
 */
public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";
    private MyLog myLog;
    private ListView listview_scan;
    private ListView listview_connected;
    private SimpleAdapter sa_scan;
    private SimpleAdapter sa_connected;
    private TextView tv_discovery;
    private List<HashMap<String, String>> list_ScanDevices = new ArrayList<HashMap<String, String>>();
    private List<HashMap<String, String>> list_ConnectedDevices = new ArrayList<HashMap<String, String>>();
    private int callbackId;
    String userName = "liu01234345555@jiuan.com";
    String clientId = "2a8387e3f4e94407a3a767a72dfd52ea";
    String clientSecret = "fd5e845c47944a818bc511fb7edb0a77";
    ProgressBar progressBar;
    Button isci;
    HashMap<String, String> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        first_time_login();

        setContentView(R.layout.activity_main);
        myLog = new MyLog("MainActivity");

        /*
         * Initializes the iHealth devices manager. Can discovery available iHealth devices nearby
         * and connect these devices through iHealthDevicesManager.
         */
        iHealthDevicesManager.getInstance().init(this);

        isci = (Button) findViewById(R.id.btn_discorvery);
        isci.setOnClickListener(this);

        Button stopButton = (Button) findViewById(R.id.btn_stopdiscorvery);
        stopButton.setVisibility(View.GONE);
        stopButton.setOnClickListener(this);

        Button button1 = (Button) findViewById(R.id.odjava);
        button1.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        tv_discovery = (TextView) findViewById(R.id.tv_discovery);
        listview_scan = (ListView) findViewById(R.id.list_scan);

        listview_connected = (ListView) findViewById(R.id.list_connected);
        listview_connected.setVisibility(View.GONE);

        if (list_ConnectedDevices != null)
            list_ConnectedDevices.clear();
        if (list_ScanDevices != null)
            list_ScanDevices.clear();

        /*
         * Register callback to the manager. This method will return a callback Id.
         */
        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(iHealthDevicesCallback);

        //Certification
        iHealthDevicesManager.getInstance().sdkUserInAuthor(MainActivity.this, userName, clientId,
                clientSecret, callbackId);

        poisciNaprave();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
         * When the Activity is destroyed , need to call unRegisterClientCallback method to
         * unregister callback
         */
        iHealthDevicesManager.getInstance().unRegisterClientCallback(callbackId);
        /*
         * When the Activity is destroyed , need to call destroy method of iHealthDeivcesManager to
         * release resources
         */
        iHealthDevicesManager.getInstance().destroy();
    }

    private iHealthDevicesCallback iHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onScanDevice(String mac, String deviceType, int rssi) {
            Log.i(TAG, "onScanDevice - mac:" + mac + " - deviceType:" + deviceType);
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            msg.what = HANDLER_SCAN;
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            Bundle bundle = new Bundle();
            bundle.putString("mac", mac);
            bundle.putString("type", deviceType);
            Message msg = new Message();
            if (status == iHealthDevicesManager.DEVICE_STATE_CONNECTED) {
                msg.what = HANDLER_CONNECTED;
            } else if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                msg.what = HANDLER_DISCONNECT;
            }
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userstatus", userStatus + "");
            Message msg = new Message();
            msg.what = HANDLER_USER_STATUE;
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
        }

		@Override
		public void onScanFinish() {
			//tv_discovery.setText("discover finish");
            progressBar.setVisibility(View.GONE);
            isci.setVisibility(View.VISIBLE);
		}
		
    };

    /*
     * userId the identification of the user, could be the form of email address or mobile phone
     * number (mobile phone number is not supported temporarily). clientID and clientSecret, as the
     * identification of the SDK, will be issued after the iHealth SDK registration. please contact
     * lvjincan@jiuan.com for registration.
     */

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btn_discorvery:
                progressBar.setVisibility(View.VISIBLE);
                isci.setVisibility(View.GONE);

                /*
                 * discovery iHealth devices, This method can specify only to search for the devices
                 * that you want to connect
                 */
                list_ScanDevices.clear();
                updateViewForScan();
                // | iHealthDevicesManager.DISCOVERY_HS4S  | iHealthDevicesManager.DISCOVERY_BP5 | iHealthDevicesManager.DISCOVERY_BP7 | iHealthDevicesManager.DISCOVERY_BP7S
                //iHealthDevicesManager.DISCOVERY_HS4 | iHealthDevicesManager.DISCOVERY_BP3L  | iHealthDevicesManager.DISCOVERY_PO3 | iHealthDevicesManager.DISCOVERY_AM3S | iHealthDevicesManager.DISCOVERY_AM4
                iHealthDevicesManager.getInstance().startDiscovery(iHealthDevicesManager.DISCOVERY_HS4S  | iHealthDevicesManager.DISCOVERY_BP5 | iHealthDevicesManager.DISCOVERY_BP7 | iHealthDevicesManager.DISCOVERY_BP7S  | iHealthDevicesManager.DISCOVERY_PO3 | iHealthDevicesManager.DISCOVERY_BP3L | iHealthDevicesManager.DISCOVERY_BTM | iHealthDevicesManager.DISCOVERY_BP550BT | iHealthDevicesManager.DISCOVERY_KD723 | iHealthDevicesManager.DISCOVERY_KD926 | iHealthDevicesManager.DISCOVERY_ABPM | iHealthDevicesManager.DISCOVERY_CBP);
                //sem jaz zakomentiral
                //tv_discovery.setText("discovering...");
                break;

            case R.id.btn_stopdiscorvery:
                /* stop discovery iHealth devices */
                iHealthDevicesManager.getInstance().stopDiscovery();
                break;

            case R.id.odjava:
                Intent intent = new Intent(this, Login.class);
                SharedPreferences preferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                preferences.edit().clear().commit();

                startActivity(intent);


            default:
                break;
        }
    }

    private static final int HANDLER_SCAN = 101;
    private static final int HANDLER_CONNECTED = 102;
    private static final int HANDLER_DISCONNECT = 103;
    private static final int HANDLER_USER_STATUE = 104;
    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_SCAN:
                    Bundle bundle_scan = msg.getData();
                    String mac_scan = bundle_scan.getString("mac");
                    String type_scan = bundle_scan.getString("type");
                    HashMap<String, String> hm_scan = new HashMap<String, String>();
                    hm_scan.put("mac", mac_scan);
                    hm_scan.put("type", type_scan);
                    list_ScanDevices.add(hm_scan);
                    updateViewForScan();
                    break;

                case HANDLER_CONNECTED:
                    Bundle bundle_connect = msg.getData();
                    String mac_connect = bundle_connect.getString("mac");
                    String type_connect = bundle_connect.getString("type");
                    HashMap<String, String> hm_connect = new HashMap<String, String>();
                    hm_connect.put("mac", mac_connect);
                    hm_connect.put("type", type_connect);
                    list_ConnectedDevices.add(hm_connect);
                    //updateViewForConnected();

                    Iterator<HashMap<String, String>> i_scan = list_ScanDevices.listIterator();
                    int index_connected = 0;
                    while (i_scan.hasNext()) {
                        HashMap<String, String> hm_disconnect = i_scan.next();
                        String mac = hm_disconnect.get("mac");
                        if (mac.equals(mac_connect)) {
                            break;
                        } else {
                            index_connected += 1;
                        }
                    }
                    list_ScanDevices.remove(index_connected);
                    updateViewForScan();
                    goToDeviceController(hm_connect);
                    break;

                case HANDLER_DISCONNECT:
                    Bundle bundle_disconnect = msg.getData();
                    String mac_disconnect = bundle_disconnect.getString("mac");
                    String type_disconnect = bundle_disconnect.getString("type");
                    Iterator<HashMap<String, String>> i = list_ConnectedDevices.listIterator();
                    int index = 0;
                    while (i.hasNext()) {
                        HashMap<String, String> hm_disconnect = i.next();
                        String mac = hm_disconnect.get("mac");
                        if (mac.equals(mac_disconnect)) {
                            break;
                        } else {
                            index += 1;
                        }
                    }
                    if (list_ConnectedDevices.size() > 0) {
                        list_ConnectedDevices.remove(index);
                        //updateViewForConnected();
                    }

                    break;
                case HANDLER_USER_STATUE:
                    Bundle bundle_status = msg.getData();
                    String username = bundle_status.getString("username");
                    String userstatus = bundle_status.getString("userstatus");
                    String str = "username:" + username + " - userstatus:" + userstatus;
                    //Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }
    };

    private void updateViewForScan() {
        sa_scan = new SimpleAdapter(this, this.list_ScanDevices, R.layout.bp_listview_baseview,
                new String[] {
                        "type", "mac"
                },
                new int[] {
                        R.id.tv_type, R.id.tv_mac
                });

        listview_scan.setAdapter(sa_scan);

        //spinner GONE after 5 seconds
        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isci.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        }, 5000);*/

        listview_scan.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                hm = list_ScanDevices.get(position);
                String type = hm.get("type");
                String mac = hm.get("mac");
                Log.i(TAG, "mac = "+mac+" type="+type);
                boolean req = iHealthDevicesManager.getInstance().connectDevice(userName, mac);
                if (!req) {
                	Toast.makeText(MainActivity.this, "Haven’t permission to connect this device or the mac is not valid", Toast.LENGTH_LONG).show();
                }
            }
        });
        sa_scan.notifyDataSetChanged();
    }

    /*private void updateViewForConnected() {
        sa_connected = new SimpleAdapter(this, this.list_ConnectedDevices, R.layout.bp_listview_baseview,
                new String[] {
                        "type", "mac"
                },
                new int[] {
                        R.id.tv_type, R.id.tv_mac
                });

        listview_connected.setAdapter(sa_connected);
        listview_connected.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                HashMap<String, String> hm = list_ConnectedDevices.get(position);
                String type = hm.get("type");
                String mac = hm.get("mac");
                Log.i(TAG, "Connected mac = "+mac+" type="+type);
                Intent intent = new Intent();
                intent.putExtra("mac", mac);
                if (iHealthDevicesManager.TYPE_AM3.equals(type)) {
                    intent.setClass(MainActivity.this, AM3.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_AM3S.equals(type)) {
                    intent.setClass(MainActivity.this, AM3S.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_AM4.equals(type)) {
                    intent.setClass(MainActivity.this, AM4.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BG5.equals(type)) {
                    intent.setClass(MainActivity.this, BG5.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BP3L.equals(type)) {
                    intent.setClass(MainActivity.this, BP3L.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BP3M.equals(type)) {
                    intent.setClass(MainActivity.this, BP3M.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BP5.equals(type)) {
                    intent.setClass(MainActivity.this, BP5.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BP7S.equals(type)) {
                    intent.setClass(MainActivity.this, Bp7s.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_BP7.equals(type)) {
                    intent.setClass(MainActivity.this, BP7.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_550BT.equals(type)) {
                	intent.setClass(MainActivity.this, Bp550BT.class);
                    startActivity(intent);
				} else if (iHealthDevicesManager.TYPE_KD926.equals(type)) {
					intent.setClass(MainActivity.this, Bp926.class);
					startActivity(intent);
				} else if (iHealthDevicesManager.TYPE_HS3.equals(type)) {
                    intent.setClass(MainActivity.this, HS3.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_HS4.equals(type)) {
                    intent.setClass(MainActivity.this, HS4.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_HS4S.equals(type)) {
                    intent.setClass(MainActivity.this, HS4S.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_HS5.equals(type)) {
                    intent.setClass(MainActivity.this, HS5wifi.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_HS5_BT.equals(type)) {
                    intent.setClass(MainActivity.this, HS5BT.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_HS6.equals(type)) {
                    intent.setClass(MainActivity.this, BP5.class);
                    startActivity(intent);

                } else if (iHealthDevicesManager.TYPE_PO3.equals(type)) {
                    intent.setClass(MainActivity.this, Po3.class);
                    startActivity(intent);

                }else if (iHealthDevicesManager.TYPE_BP7S.equals(type)) {
                	intent.setClass(MainActivity.this, Bp7s.class);
                    startActivity(intent);
				}
            }
        });
        sa_connected.notifyDataSetChanged();
    }*/

    private void first_time_login(){
        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        String auth = sharedPref.getString("auth", null);
        String username = sharedPref.getString("username", null);

        Log.i("asd", auth+" "+username);

        if(auth == null){
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
    }

    private void poisciNaprave(){
        progressBar.setVisibility(View.VISIBLE);
        isci.setVisibility(View.GONE);

                /*
                 * discovery iHealth devices, This method can specify only to search for the devices
                 * that you want to connect
                 */
        list_ScanDevices.clear();
        updateViewForScan();
        // | iHealthDevicesManager.DISCOVERY_HS4S  | iHealthDevicesManager.DISCOVERY_BP5 | iHealthDevicesManager.DISCOVERY_BP7 | iHealthDevicesManager.DISCOVERY_BP7S
        //iHealthDevicesManager.DISCOVERY_HS4 | iHealthDevicesManager.DISCOVERY_BP3L  | iHealthDevicesManager.DISCOVERY_PO3 | iHealthDevicesManager.DISCOVERY_AM3S | iHealthDevicesManager.DISCOVERY_AM4
        iHealthDevicesManager.getInstance().startDiscovery(iHealthDevicesManager.DISCOVERY_HS4S  | iHealthDevicesManager.DISCOVERY_BP5 | iHealthDevicesManager.DISCOVERY_BP7 | iHealthDevicesManager.DISCOVERY_BP7S  | iHealthDevicesManager.DISCOVERY_PO3 | iHealthDevicesManager.DISCOVERY_BP3L | iHealthDevicesManager.DISCOVERY_BTM | iHealthDevicesManager.DISCOVERY_BP550BT | iHealthDevicesManager.DISCOVERY_KD723 | iHealthDevicesManager.DISCOVERY_KD926 | iHealthDevicesManager.DISCOVERY_ABPM | iHealthDevicesManager.DISCOVERY_CBP);

    }

    private void goToDeviceController(HashMap<String, String> hm){
        String type = hm.get("type");
        String mac = hm.get("mac");

        Intent intent = new Intent();
        intent.putExtra("mac", mac);

        if (iHealthDevicesManager.TYPE_AM3.equals(type)) {
            intent.setClass(MainActivity.this, AM3.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_AM3S.equals(type)) {
            intent.setClass(MainActivity.this, AM3S.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_AM4.equals(type)) {
            intent.setClass(MainActivity.this, AM4.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BG5.equals(type)) {
            intent.setClass(MainActivity.this, BG5.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BP3L.equals(type)) {
            intent.setClass(MainActivity.this, BP3L.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BP3M.equals(type)) {
            intent.setClass(MainActivity.this, BP3M.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BP5.equals(type)) {
            intent.setClass(MainActivity.this, BP5.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BP7S.equals(type)) {
            intent.setClass(MainActivity.this, Bp7s.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_BP7.equals(type)) {
            intent.setClass(MainActivity.this, BP7.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_550BT.equals(type)) {
            intent.setClass(MainActivity.this, Bp550BT.class);
            startActivity(intent);
        } else if (iHealthDevicesManager.TYPE_KD926.equals(type)) {
            intent.setClass(MainActivity.this, Bp926.class);
            startActivity(intent);
        } else if (iHealthDevicesManager.TYPE_HS3.equals(type)) {
            intent.setClass(MainActivity.this, HS3.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_HS4.equals(type)) {
            intent.setClass(MainActivity.this, HS4.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_HS4S.equals(type)) {
            intent.setClass(MainActivity.this, HS4S.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_HS5.equals(type)) {
            intent.setClass(MainActivity.this, HS5wifi.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_HS5_BT.equals(type)) {
            intent.setClass(MainActivity.this, HS5BT.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_HS6.equals(type)) {
            intent.setClass(MainActivity.this, BP5.class);
            startActivity(intent);

        } else if (iHealthDevicesManager.TYPE_PO3.equals(type)) {
            intent.setClass(MainActivity.this, Po3.class);
            startActivity(intent);

        }else if (iHealthDevicesManager.TYPE_BP7S.equals(type)) {
            intent.setClass(MainActivity.this, Bp7s.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }
}

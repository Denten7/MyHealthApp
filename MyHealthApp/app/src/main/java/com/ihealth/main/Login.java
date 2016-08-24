package com.ihealth.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.devicelibtest.R;

public class Login extends Activity {
    EditText uporIme, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        uporIme = (EditText) findViewById(R.id.uporIme);
        userPassword = (EditText) findViewById(R.id.userPassword);
        //final Button login = (Button) findViewById(R.id.login);
    }

    public void onLogin(View view){
        Log.i("asd", "Klik gumba dela juhu oz ne ker potem je problem drugje");
        String username = uporIme.getText().toString();
        String password = userPassword.getText().toString();
        String urlAddr = "https://myhealthapp-denten7.c9users.io/Login/androidConfirm";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this, new BackgroundWorker.AsyncResponse() {
            @Override
            public void processFinish(String s, Context ctx) {
                if(s.equals("Failed")){
                    Toast.makeText(ctx, "Napaka pri prijavi! Prosim poskusite znova.", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                String[] parts = s.split("\\s+");
                Log.i("finish", s);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("auth", parts[0]);
                editor.putString("username", parts[1]);
                editor.apply();

                Intent intent = new Intent(ctx, MainActivity.class);
                ctx.startActivity(intent);
            }
        });

        backgroundWorker.execute(username, password, urlAddr);

    }

    @Override
    public void onBackPressed() {

        return;
    }
}

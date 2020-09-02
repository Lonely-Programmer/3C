package com.example.zhang.a3c_car;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.widget.TextView;

//查看与设置ip地址
public class ServerActivity extends Activity {

    public static TextView mTextView, textView1;
    private String IP = "";
    public static String host;
    private EditText ed;
    private Button set_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        mTextView = (TextView) findViewById(R.id.textsss);
        textView1 = (TextView) findViewById(R.id.textView1);
        set_address = (Button) findViewById(R.id.send);
        ed = (EditText) findViewById(R.id.ed1);
        IP = getlocalip();
        textView1.setText("IP addresss:" + IP);

        set_address.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                host = ed.getText().toString();
                Intent mIntent = new Intent();
                mIntent.putExtra("a",host);
                setResult(9,mIntent); //将地址传回MainActivity
                finish();
            }
        });
    }

    private String getlocalip(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }
}
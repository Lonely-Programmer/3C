package com.example.zhang.a3c_car_remote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//发送图像模块
public class SendClass {
    private boolean flag=false;
    public static ServerSocket serverSocket = null;

    public void setCamera(MainActivity camera) {
        this.camera = camera;
    }

    private MainActivity camera;
    Integer cnt_trial=0;
    byte[] zsend;

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what==0x11) {
                Bundle bundle = msg.getData();
            }
        };
    };

    public void setContent(byte[] s){
        zsend=new byte[80000];
        for(int i =0;i<s.length;++i)
        zsend[i]=s[i];
        flag=true;
    }

    public void send() {

        new Thread() {
            public void run() {
                Bundle bundle = new Bundle();
                bundle.clear();
                OutputStream output;
                int cont = 0;
                try {
                    serverSocket = new ServerSocket(3333);

                    while (true) {
                        Message msg = new Message();
                        msg.what = 0x11;

                        try {
                            Socket socket = serverSocket.accept();
                            flag=false;
                            camera.HandlePicture();
                            while(flag==false);
                            output = socket.getOutputStream();

                            output.write(zsend);
                            output.flush();
                            socket.shutdownOutput();
                            InputStream in = socket.getInputStream();
                            byte[] b = new byte[1000];
                            in.read(b);
                            cont++;
                            cnt_trial++;
                            output.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block

                    e1.printStackTrace();
                }
            };
        }.start();
    }
}

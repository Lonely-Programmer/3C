package com.example.zhang.a3c_car;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

//图像回传与人脸识别模块
public class ClientActivity extends Activity {
    Socket socket = null;
    String buffer = "";
    TextView txt1;
    Button send;
    Button zhello;
    Button front,back,left,right,stop;
    EditText ed1;
    ImageView image1;
    Bitmap zbitmap;
    Toast toast;
    public static String host;
    BlueTooth zblue;
    private boolean hasDetected = false;//标记是否检测到人脸
    private int realFaceNum = 0;//实际检测出的人脸数量
    private static final int MAX_FACE_NUM = 5;//最大可以检测出的人脸数量
    private Paint paint;//画人脸区域用到的Paint
    private int flag=0;
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                Bundle bundle = msg.getData();
                try{
                    zbitmap.getWidth();
                    image1.setImageBitmap(zbitmap);
                    if(flag==1)
                    {
                        detectFace();
                    }
                }catch(Exception e) {

                }
            }
        }

    };

    private void detectFace(){ //人脸识别
        if(zbitmap == null){
            return ;
        }
        if(hasDetected){

        }else{
            new FindFaceTask().execute();
        }
    }

    private void drawFacesArea(FaceDetector.Face[] faces){
        float eyesDistance = 0f;//两眼间距
        Bitmap mbitmap=zbitmap.copy(Bitmap.Config.RGB_565,true);
        Canvas canvas = new Canvas(mbitmap);
        for(int i = 0; i < faces.length; i++){
            FaceDetector.Face face = faces[i];
            if(face != null){
                PointF pointF = new PointF();
                face.getMidPoint(pointF);//获取人脸中心点
                eyesDistance = face.eyesDistance();//获取人脸两眼的间距
                //画出人脸的区域
                canvas.drawRect(pointF.x - eyesDistance, pointF.y - eyesDistance, pointF.x + eyesDistance, pointF.y + eyesDistance, paint);
                hasDetected = true;
            }
        }
        //画出人脸区域后要刷新ImageView
        zbitmap=mbitmap;
        image1.setImageBitmap(zbitmap);
        image1.invalidate();
        if(hasDetected)
        {
            zblue.zsend("s");
        }
    }

    /**
     * 检测图像中的人脸需要一些时间，所以放到AsyncTask中去执行
     */
    private class FindFaceTask extends AsyncTask<Void, Void, FaceDetector.Face[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected FaceDetector.Face[] doInBackground(Void... arg0) {
            //最关键的就是下面三句代码
            FaceDetector faceDetector = new FaceDetector(zbitmap.getWidth(), zbitmap.getHeight(), MAX_FACE_NUM);
            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE_NUM];
            Bitmap mBitmap = zbitmap.copy(Bitmap.Config.RGB_565, true);
            realFaceNum = faceDetector.findFaces(mBitmap, faces);
            if(realFaceNum > 0){
                return faces;
            }
            return null;
        }

        @Override
        protected void onPostExecute(FaceDetector.Face[] result) {
            super.onPostExecute(result);
            if(result == null){
            }else{
                drawFacesArea(result);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Bundle extras = getIntent().getExtras();
        host = extras.getString("a");

        txt1 = (TextView) findViewById(R.id.txt1);
        send = (Button) findViewById(R.id.send);
        front = (Button) findViewById(R.id.front1);
        back = (Button) findViewById(R.id.back1);
        left = (Button) findViewById(R.id.left1);
        right = (Button) findViewById(R.id.right1);
        stop = (Button) findViewById(R.id.stop1);
        ed1 = (EditText) findViewById(R.id.ed1);
        image1 = (ImageView) findViewById(R.id.im1);
        zhello = (Button) findViewById(R.id.send);
        zblue=new BlueTooth();
        zblue.Bluetooth();

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);//设置话出的是空心方框而不是实心方块

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new MyThread("建立连接").start();
                myHandler.sendEmptyMessage(0);
                if(flag==1)
                {
                    detectFace();
                }
            }
        },1000,2000);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag=1-flag;
                if(flag==1)
                    hasDetected = false;
                send.setText(String.valueOf(flag));
            }
        });
        front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zblue.zsend("A");
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zblue.zsend("B");
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zblue.zsend("C");
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zblue.zsend("D");
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zblue.zsend("s");
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        zblue.zend();
        super.onDestroy();
    }

    class MyThread extends Thread {

        public String txt1;
        public MyThread(String str) {
            txt1 = str;
        }
        public int bytesToInt(byte[] src) {
            int value; //传输的数据中，图像的长度在前4个byte，表示图像大小，此函数为转换函数
            value = (int) ((src[0] & 0xFF)
                    | ((src[1] & 0xFF)<<8)
                    | ((src[2] & 0xFF)<<16)
                    | ((src[3] & 0xFF)<<24));
            return value;
        }
        private void printSum(byte[] src) //校验和，用于调试
        {
            int length = bytesToInt(src);
            long sum =0;
            for(int i =4;i<length+4;i++)sum+=src[i];
            Log.d("sum:",String.valueOf(sum));
        }
        @Override
        public void run() {
            // 发送消息
            Message msg = new Message();
            msg.what=0x11;
            try {
                socket = new Socket(host, 3333);
                OutputStream ou = socket.getOutputStream();
                BufferedReader bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                buffer = "";
                InputStream reader = socket.getInputStream();
                byte[] bbuf = new byte[80000];
                int count = 0;
                byte[] buffer = new byte[80000];
                int length = 0;
                while ((count=reader.read(buffer))!=-1)
                {
                    for(int i=0;i<count;++i)bbuf[i+length]=buffer[i];
                    length+=count;
                }
                if(flag==0) {
                    zbitmap=BitmapFactory.decodeByteArray(bbuf,4,bytesToInt(bbuf));
                    printSum(bbuf);
                }
                myHandler.sendMessage(msg);
                Log.d("length:",String.valueOf(bytesToInt(bbuf)));
                ou.write("OK".getBytes());
                bff.close();
                reader.close();
                ou.close();
                socket.close();
            } catch (SocketTimeoutException aa) {
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}

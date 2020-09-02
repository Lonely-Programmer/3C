package com.example.zhang.a3c_car;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

//控制模块
public class FlipActivity extends Activity implements android.view.GestureDetector.OnGestureListener
{
    GestureDetector detector; //定义手势检测器实例
    BlueTooth zblue=null;
    Button zleft,zright,zfront,zback,zgra,zspeech;
    TextView zhello;
    int zgravity=0;
    int zcounter=0;
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private float x,y,z; //各方向加速度分量
    private Handler handler;
    private int past_gravity=0;
    SpeechRecognizerTool mSpeechRecognizerTool = new SpeechRecognizerTool(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip);
        //new一个手势检测器
        mSensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        zhello=findViewById(R.id.Hello);
        handler=new Handler();

        detector = new GestureDetector(this,this);
        zblue=new BlueTooth();
        zblue.Bluetooth();

        zleft=findViewById(R.id.Left);
        zleft.setOnClickListener(listener1);
        zright=findViewById(R.id.Right);
        zright.setOnClickListener(listener1);
        zfront=findViewById(R.id.Front);
        zfront.setOnClickListener(listener1);
        zback=findViewById(R.id.Back);
        zback.setOnClickListener(listener1);
        zgra=findViewById(R.id.Gravity);
        zgra.setOnClickListener(listener1);
        zspeech=findViewById(R.id.Speech);
        zspeech.setOnClickListener(listener1);
        mSensorManager.registerListener(lsn, mSensor, SensorManager.SENSOR_DELAY_GAME);

        ButtonListener bl = new ButtonListener();
        zleft.setOnTouchListener(bl);
        zright.setOnTouchListener(bl);
        zfront.setOnTouchListener(bl);
        zback.setOnTouchListener(bl);
        zspeech.setOnTouchListener(bl);
        ztimer();

        mSpeechRecognizerTool.createTool();
    }

    @Override
    protected void onDestroy() {
        zblue.zend();
        super.onDestroy();
    }

    SensorEventListener lsn = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[SensorManager.DATA_X];
            y = event.values[SensorManager.DATA_Y];
            z = event.values[SensorManager.DATA_Z];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    private void ztimer()
    {
        Timer tm = new Timer();
        tm.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //判断x方向加速度
                        if(zgravity>0)
                        {
                            if(x>4) //偏左
                            {
                                zhello.setText("Left "+Float.toString(x));
                                if(past_gravity!=-1)
                                {
                                    zblue.zsend("s");
                                    zblue.zsend("C");
                                }
                                past_gravity=-1; //上一次的方向
                            }
                            else if(x<-4) //偏右
                            {
                                zhello.setText("Right "+Float.toString(x));
                                if(past_gravity!=1)
                                {
                                    zblue.zsend("s");
                                    zblue.zsend("D");
                                }
                                past_gravity=1;
                            }
                            else //不偏
                            {
                                zhello.setText("Front "+Float.toString(x));
                                if(past_gravity!=0)
                                {
                                    zblue.zsend("s");
                                    zblue.zsend("A");
                                }
                                past_gravity=0;
                            }
                        }
                        zcounter++;
                    }
                });
            }
        },200,200);

    }

        //用GestureDetector处理在该activity上发生的所有触碰事件
        class ButtonListener implements View.OnTouchListener {

            public void onClick(View v) {

            }

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v.getId() == R.id.Left || v.getId() == R.id.Right || v.getId() == R.id.Front || v.getId() == R.id.Back) {
                        zblue.zsend("s");
                    } else if (v.getId() == R.id.Speech) {
                        mSpeechRecognizerTool.stopASR();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (v.getId() == R.id.Left) {
                        //按下向左，发出持续向左信号
                        zblue.zsend("C");
                    } else if (v.getId() == R.id.Right) {
                        //按下向右，发出持续向右信号
                        zblue.zsend("D");
                    } else if (v.getId() == R.id.Front) {
                        //按下向前，发出持续向前信号
                        zblue.zsend("A");
                    } else if (v.getId() == R.id.Back) {
                        //按下向后，发出持续向后信号
                        zblue.zsend("B");
                    } else if (v.getId() == R.id.Speech) {

                    }
                }

                return false;
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //获取语音识别模块传回的识别结果
        if(requestCode == 1){
            String a=data.getStringExtra("backString");
            zhello.setText(a);
            zblue.zsend(a);
        }
    }


    public boolean onTouchEvent(MotionEvent me){

        return detector.onTouchEvent(me);
    }


    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    /**
     * 滑屏监测
     *
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float minMove = 120;         //定义最小滑动距离
        float minVelocity = 0;      //定义最小滑动速度
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();
        float direction=(endY-beginY)/(endX-beginX);

        if(Math.abs(endX-beginX)+Math.abs(endY-beginY)>minMove)
        {
            if(direction>-0.6 && direction<0.6)
            {
                if(endX-beginX>0)
                {
                    //右滑
                    zblue.zsend("2");
                }
                else
                {
                    //左滑
                    zblue.zsend("1");
                }
            }
            else if(direction>0.6 && direction<1.7)
            {
                if(endX-beginX>0)
                {
                    //右下
                    zblue.zsend("h");
                }
                else
                {
                    //左上
                    zblue.zsend("f");
                }
            }
            else if(direction>1.7 || direction<-1.7)
            {
                if(endY-beginY<0)
                {
                    //上滑
                    zblue.zsend("a");
                }
                else
                {
                    //下滑
                    zblue.zsend("b");
                }
            }
            else if(direction>-1.7 && direction<-0.6)
            {
                if(endX-beginX<0)
                {
                    //左下
                    zblue.zsend("g");
                }
                else
                {
                    //右上
                    zblue.zsend("e");
                }
            }
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX,
                            float velocityY) {

        return false;
    }

    private View.OnClickListener listener1 = new View.OnClickListener() {
        public void onClick(View v) {
            Button btnButton = (Button) v;

            switch (btnButton.getId()) {

                case R.id.Speech:
                    //启动语音识别模块并等待结果
                    String str1 = new String("a");
                    Intent intent =new Intent(FlipActivity.this,SpeechActivity.class);
                    intent.putExtra("dataSend",str1);
                    startActivityForResult(intent,1);
                    break;

                case R.id.Gravity:
                    if(zgravity==0)
                    {
                        zgravity=1;
                        zgra.setText("重力：开");
                        zblue.zsend("s");
                    }
                    else
                    {
                        zgravity=0;
                        zgra.setText("重力：关");
                        zblue.zsend("s");
                    }
                    break;
            }
        }
    };
}

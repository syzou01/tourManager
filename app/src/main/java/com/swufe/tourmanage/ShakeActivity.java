package com.swufe.tourmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShakeActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Vibrator vibrater;
    private static int pics[] = {R.mipmap.img1,R.mipmap.img2,R.mipmap.img3,R.mipmap.img4,R.mipmap.img5,R.mipmap.img6,R.mipmap.img7};

    private ArrayList<String> titleList= new ArrayList<String>(){{add("");}};
    private ArrayList<String> urlList = new ArrayList<String>(){{add("");}};

    private TextView text;
    private ImageView img;

    private static final String TAG = "ShakeActivity";
    private static final int SENSOR_SHAKE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        text = (TextView)findViewById(R.id.place);
        img = (ImageView)findViewById(R.id.imageView);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        vibrater = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    //sharedPreference取出List
    public void loadList(List sKey, String dataName) {
        SharedPreferences mSharedPreference1 = getSharedPreferences(dataName, Activity.MODE_PRIVATE);
        sKey.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);
        for (int i = 0; i < size; i++) {
            sKey.add(mSharedPreference1.getString("Status_" + i, null));
        }
    }

    protected void onResume(){
        super.onResume();
        if(sensorManager != null){
            sensorManager.registerListener(sensorEventListener,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onStop(){
        super.onStop();
        if(sensorManager != null){
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    //重力感应监听
    private SensorEventListener sensorEventListener  = new SensorEventListener(){

        @Override
        public void onSensorChanged(SensorEvent event) {
            //传感器信息改变时执行该方法
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];
            Log.i(TAG, "onSensorChanged: x:"+x+"y:"+y+"z:"+z);
            int medumValue = 25;
            if(Math.abs(x)>medumValue||Math.abs(y)>medumValue||Math.abs(z)>medumValue){
                vibrater.vibrate(200);
                Message msg = new Message();
                msg.what = SENSOR_SHAKE;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENSOR_SHAKE:
                    Toast.makeText(ShakeActivity.this, "已向您随机推荐旅行地！", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "检测到摇晃，执行操作！");
                    java.util.Random r = new java.util.Random();

                    loadList(titleList,"title");
                    loadList(urlList,"url");

                    int num1 = Math.abs(r.nextInt(titleList.size()));
                    int num2 = Math.abs(r.nextInt(7))+1;
                    text.setText((String)titleList.get(num1));
                    img.setImageResource(pics[num2]);
                    break;
            }
        }
    };
}

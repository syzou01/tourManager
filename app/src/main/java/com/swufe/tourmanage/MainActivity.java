package com.swufe.tourmanage;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.swufe.tourmanage.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Runnable, AdapterView.OnItemClickListener {

    private String todayStr;
    private String logDate;
    private List titleList = Arrays.asList("");
    private List urlList = Arrays.asList("");
    private ArrayList<String> data = new ArrayList<String>(){{add("");}};
    private ListView mListView;
    private ListAdapter mAdapter;
    public static String TAG = "MainActivity:";
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //给按钮添加监听
        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(this);

        //获取data里保存的时间记录
        SharedPreferences sp = getSharedPreferences("data", Activity.MODE_PRIVATE);
        logDate = sp.getString("update_date","20200409");
        Log.i("Note","lastRateDateStr="+logDate);

        //获取当前系统时间
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//小写的m代表分钟
        todayStr = sdf.format(today);

        //判断时间
        Date logdate = null;
        try {
            logdate = new SimpleDateFormat("yyyyMMdd").parse(logDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if((today.getTime()-logdate.getTime())/(24*60*60*1000)>=7){//如果过去了大于7天，则需要更新数据
            Log.i("Note","onCreate:需要更新");
            //开启子线程
            Thread t = new Thread(this);
            t.start();
        }else{
            Log.i("Note","onCreate:不需要更新");
        }

        //从子线程中获得消息
        handler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what==1){
                    List titleList = (List) msg.obj;
                    Log.i("Note","handleMessage: get titleList");
                }
                if(msg.what==2){
                    List titleList = (List) msg.obj;
                    Log.i("Note","handleMessage: get urlList");
                }
                super.handleMessage(msg);
            }
        };//匿名类的改写

        //添加点击事件监听
        mListView = findViewById(R.id.List);
        mListView.setOnItemClickListener(this);

    }
    //列表点击事件处理
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i("Note","ClickItem id="+id);
        Log.i("Note","data="+data.get(position));
        int po = titleList.indexOf(data.get(position));
        String url = (String) urlList.get(po);
        url = "https:"+url;
        //Log.i("Note","url="+url);

        //将url传入SharedPreference
        SharedPreferences sp = getSharedPreferences("curUrl", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("curUrl",url);
        edit.commit();
        Log.i("saveUrl","传入url结束："+url);

        //打开列表页
        OpenList();

        /*//打开当前url对应网页
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);*/
    }

    @Override
    //按钮点击事件
    public void onClick(View v) {
        Log.i("Click","Click...");

        //获取输入的关键词
        EditText inp = (EditText)findViewById(R.id.editText);
        String keyWord = inp.getText().toString();
        Log.i("Note","keyWord="+keyWord);

        data.clear();
        for(int i=0;i<titleList.size();i++){
            String Str = (String) titleList.get(i);
            if(Str.indexOf(keyWord) != -1){//如果该题目包含关键词
                data.add(Str);//将题目放进列表中显示
            }
        }

        //列表
        mListView = findViewById(R.id.List);
        mAdapter = new ArrayAdapter(MainActivity.this,R.layout.adapter_list,data);
        mListView.setAdapter(mAdapter);

        //没有内容的Toast
        if(data.isEmpty()){
            Toast.makeText(MainActivity.this,"没有找到您需要的信息！",Toast.LENGTH_SHORT).show();
        }
    }


    //子线程
    @Override
    public void run() {
        Log.i("Note","run():running.....");
        for(int i=1;i<3;i++){
            Log.i("Note","run():i="+i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //获取message对象用于返回主线程
        Message msg1 = handler.obtainMessage(1);
        //msg.what = 1;//设置一个整数，用于标记message
        msg1.obj=titleList;//message的内容
        handler.sendMessage(msg1);//发送message
        Message msg2 = handler.obtainMessage(2);
        msg2.obj=urlList;//message的内容
        handler.sendMessage(msg2);//发送message

        //从网络中获取数据
        Document doc = null;
        try {
            doc = Jsoup.connect("https://you.ctrip.com/sitemap/spotdis/c0").get();
            //Log.i("Note","run:  "+ doc.title());
            Elements titles = doc.getElementsByAttributeValueEnding("title","景点");
            Elements urls = doc.getElementsByAttributeValueStarting("href","//you.ctrip.com/sight/");
            Log.i("Note","run:  article-showTitle"+ titles);
            int i = 0;
            for(Element title:titles){
                Log.i("Note","run:  Title:"+ title.text());
                ArrayList a = new ArrayList(titleList);
                a.add(i,title.text());
                titleList = a;
                i++;
            }
            i=0;
            for(Element url:urls){
                //Log.i("Note","run:  article-showUrl:"+ url.attr("href"));
                ArrayList b = new ArrayList(urlList);
                b.add(i,url.attr("href"));
                urlList = b;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //更新title和url中的数据
        saveList(titleList,"title");
        saveList(urlList,"url");
        Log.i("Note","更新title结束");
        Log.i("Note","更新url结束");

        //更新记录日期
        SharedPreferences sp = getSharedPreferences("logDate", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("logDate",todayStr);
        edit.commit();
        Log.i("runDB","更新日期结束："+todayStr);

    }


    //sharedPreference保存List
    public void saveList(List sKey, String dataName) {
        SharedPreferences sp=getSharedPreferences(dataName, Activity.MODE_PRIVATE);;
        SharedPreferences.Editor mEdit1= sp.edit();
        mEdit1.putInt("Status_size",sKey.size()); /*sKey is an array*/
        for(int i=0;i<sKey.size();i++) {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, (String) sKey.get(i));
        }
        mEdit1.commit();
    }

    //sharedPreference取出List
    public void loadList(List sKey,String dataName) {
        SharedPreferences mSharedPreference1 = getSharedPreferences(dataName, Activity.MODE_PRIVATE);
        sKey.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);
        for (int i = 0; i < size; i++) {
            sKey.add(mSharedPreference1.getString("Status_" + i, null));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_shake){
            OpenShake();
        }
        return super.onOptionsItemSelected(item);
    }

    private void OpenShake() {
        Intent shake = new Intent(this, ShakeActivity.class);
        Log.i(TAG, "openShake" );
        startActivityForResult(shake, 1);
    }

    private void OpenList() {
        Intent list = new Intent(this, ListActivity.class);
        Log.i(TAG, "openList" );
        startActivityForResult(list,2);
    }
}


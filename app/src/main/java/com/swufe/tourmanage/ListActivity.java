package com.swufe.tourmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ListActivity extends AppCompatActivity implements View.OnClickListener, Runnable, AdapterView.OnItemClickListener {

    private List titleList = Arrays.asList("");
    private List urlList = Arrays.asList("");
    private String curUrl;
    private ArrayList<String> data = new ArrayList<String>(){{add("");}};
    private ListView mListView;
    private ListAdapter mAdapter;
    public static String TAG = "ListActivity:";
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        SharedPreferences sp = getSharedPreferences("curUrl", Activity.MODE_PRIVATE);
        curUrl = sp.getString("curUrl","https://you.ctrip.com/sight/chuzhou228.html");
        Log.i("Note","current url="+curUrl);

        //开启子线程
        Thread t = new Thread(this);
        t.start();

        //从子线程中获得消息
        handler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what==1){
                    List titleList = (List) msg.obj;
                    Log.i("Note","handleMessage: get titleList"+titleList);
                }

                //将题目放入data中
                data.clear();
                Log.i("data","titleList="+titleList);
                for(int i=1;i<titleList.size();i++){
                    String Str = (String) titleList.get(i);
                    Log.i("data1111","Str="+Str);
                    data.add(Str);//将题目放进列表中显示
                }

                mListView = findViewById(R.id.List1);
                mAdapter = new ArrayAdapter(ListActivity.this,R.layout.adapter_list2, data);
                mListView.setAdapter(mAdapter);

                Log.i("data","data="+data);
                if(msg.what==2){
                    List urlList = (List) msg.obj;
                    Log.i("Note","handleMessage: get urlList"+urlList);
                }
                super.handleMessage(msg);
            }
        };//匿名类的改写

        //获取控件
        mListView = findViewById(R.id.List1);
        //添加点击事件监听
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {

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

        //打开当前url对应网页
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }


    @Override
    public void run() {
        Log.i("Note","run():running.....");


        //从网络中获取数据
        Document doc = null;
        try {
            doc = Jsoup.connect(curUrl).get();
            Log.i("Note","run:  "+ doc.title());
            ArrayList a = new ArrayList(titleList);
            ArrayList b = new ArrayList(urlList);

            Elements elements = doc.select("body > div.ttd2_background > div > div.des_wide.f_right > div > div.list_wide_mod2 > div > div.rdetailbox");
            Iterator<Element> iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                Elements aTag = element.getElementsByTag("a");
                a.add(aTag.attr("title"));
                b.add(aTag.attr("href"));
                Log.i("Note","run:  Title:"+ aTag.attr("title"));
                Log.i("Note","run:  Url:"+ aTag.attr("href"));
            }
            titleList = a;
            urlList = b;

        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取message对象用于返回主线程
        Message msg1 = handler.obtainMessage(1);
        //msg.what = 1;//设置一个整数，用于标记message
        msg1.obj=titleList;//message的内容
        handler.sendMessage(msg1);//发送message
        Message msg2 = handler.obtainMessage(2);
        msg2.obj=urlList;//message的内容
        handler.sendMessage(msg2);//发送message



    }
}
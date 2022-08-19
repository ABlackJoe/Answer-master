package com.example.answer.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.answer.R;
import com.example.answer.bean.AnSwerInfo;
import com.example.answer.database.DataBaseHelper;
import com.example.answer.database.DataBaseManager;
import com.example.answer.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.content.DialogInterface;




public class MainActivity extends Activity {

    private Button b_input;
    private Button b_rantest;

    private static final int REQUEST_CHOOSER = 1234;

    private ImageView left;
    private TextView title;
    private DataBaseHelper myDbHelper;
    private SQLiteDatabase db;
    private DataBaseManager dataBaseManager;
    private AnSwerInfo anSwerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        left = (ImageView) findViewById(R.id.left);
        title = (TextView) findViewById(R.id.title);
        b_input = (Button) findViewById(R.id.b_input);
        //拷贝数据库
        copyDB();


        left.setVisibility(View.GONE);
        title.setText("答题测试");

        //随机抽题



        //导入数据库
        b_input.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getContentIntent = FileUtils.createGetContentIntent();
                Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                startActivityForResult(intent, REQUEST_CHOOSER);

            }
        });


    }

    /**
     * 获取文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //初始化
        anSwerInfo = new AnSwerInfo();
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {
                    final Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    if (path != null && FileUtils.isLocal(path)) {
                        try {
                            int sum = 0, flag = 0;
                            String encoding = "GBK";
                            File file = new File(path);
                            if (file.isFile() && file.exists()) { //判断文件是否存在
                                InputStreamReader read = new InputStreamReader(
                                        new FileInputStream(file), encoding);//考虑到编码格式
                                BufferedReader bufferedReader = new BufferedReader(read);
                                String lineTxt = null;
                                lineTxt = bufferedReader.readLine();
                                //初始化试题编号
                                anSwerInfo.setTestNo(lineTxt);
                                while ((lineTxt = bufferedReader.readLine()) != null) {
                                    if (lineTxt.contains("单选题")) {
                                        sum = 1;
                                        anSwerInfo.setQuestionType("0");
                                        continue;
                                    } else if (lineTxt.contains("多选题")) {
                                        sum = 1;
                                        anSwerInfo.setQuestionType("1");
                                        continue;
                                    } else if (lineTxt.contains("判断题")) {
                                        sum = 1;
                                        flag = 1;
                                        anSwerInfo.setQuestionType("2");
                                        continue;
                                    } else if (lineTxt.length() == 0) {
                                        continue;
                                    }
                                    if (flag == 0) {
                                        //题目名
                                        lineTxt = filter(lineTxt, 0);
                                        anSwerInfo.setQuestionName(lineTxt);
                                        //选项A
                                        lineTxt = bufferedReader.readLine();
                                        lineTxt = filter(lineTxt, 1);
                                        anSwerInfo.setOptionA(lineTxt);
                                        //选项B
                                        lineTxt = bufferedReader.readLine();
                                        lineTxt = filter(lineTxt, 1);
                                        anSwerInfo.setOptionB(lineTxt);
                                        //选项C
                                        lineTxt = bufferedReader.readLine();
                                        lineTxt = filter(lineTxt, 1);
                                        anSwerInfo.setOptionC(lineTxt);
                                        //选项D
                                        lineTxt = bufferedReader.readLine();
                                        lineTxt = filter(lineTxt, 1);
                                        anSwerInfo.setOptionD(lineTxt);
                                        //选项E
                                        lineTxt = bufferedReader.readLine();
                                        lineTxt = filter(lineTxt, 1);
                                        anSwerInfo.setOptionE(lineTxt);
                                        //正确答案
                                        lineTxt = bufferedReader.readLine();
                                        anSwerInfo.setCorrectAnswer(lineTxt);
                                        //解析
                                        lineTxt = bufferedReader.readLine();
                                        anSwerInfo.setAnalysis(lineTxt);
                                        //获得了一个完整的数据，插入数据库
                                        anSwerInfo.setQuestionId("" + sum);
                                        sum++;
                                    } else {
                                        //题目名
                                        lineTxt = filter(lineTxt, 0);
                                        anSwerInfo.setQuestionName(lineTxt);
                                        //选项A
                                        anSwerInfo.setOptionA("对");
                                        //选项B
                                        anSwerInfo.setOptionB("错");
                                        //选项C
                                        anSwerInfo.setOptionC("");
                                        //选项D
                                        anSwerInfo.setOptionD("");
                                        //选项E
                                        anSwerInfo.setOptionE("");
                                        //正确答案
                                        lineTxt = bufferedReader.readLine();
                                        if (lineTxt.contains("√")) {
                                            anSwerInfo.setCorrectAnswer("A");
                                        } else {
                                            anSwerInfo.setCorrectAnswer("B");
                                        }
                                        //解析
                                        lineTxt = bufferedReader.readLine();
                                        anSwerInfo.setAnalysis(lineTxt);
                                        //获得了一个完整的数据，插入数据库
                                        anSwerInfo.setQuestionId("" + sum);
                                        sum++;
                                    }
                                    anSwerInfo.setVideoName(null);
                                    //数据插入数据库
                                    dataBaseManager.insertData(anSwerInfo);
                                    System.out.println(lineTxt);
                                }
                                read.close();
                            } else {
                                System.out.println("找不到指定的文件");
                            }
                        } catch (Exception e) {
                            System.out.println("读取文件内容出错");
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    /**
     * 拷贝数据库
     */
    private void copyDB() {
        //加载数据库 拷贝(第一次) 读取(第二次)
        //判断数据库是否存在
        String path = "/data/data/com.example.answer/databases/TestData.db";
        File file = new File(path);
        System.out.println(file.exists());
        //创建数据库助手类
        myDbHelper = new DataBaseHelper(this);
        if (false == file.exists()) {
            try {
                //若数据库为空,则拷贝 创建数据库
                myDbHelper.createDataBase();
            } catch (Exception e) {
                throw new Error("Unable to create database");
            }
        } else {
            System.out.println("数据库已存在!");
        }

        db = myDbHelper.getWritableDatabase();
        //初始化查询类
        dataBaseManager = new DataBaseManager(db);
    }

    /**
     * 数据筛选程序
     *
     * @param str
     * @param mode
     * @return
     */
    public String filter(String str, int mode) {
        str = str.substring(1, str.length());
        if (mode == 1) {//去除选项
            str = str.replace(" ", "");
            str = str.replace("、", "");
            str = str.replace("．", "");
            str = str.replace(".", "");
            str = str.replace("。", "");
        }
        if (mode == 0) {
            //str=str.replaceAll("\\d+","");//用于题目，去除数字
            str = str.substring(1, str.length());
            str = str.replace(" ", "");
            str = str.replace("、", "");
            str = str.replace("．", "");
            str = str.replace(".", "");
            str = str.replace("。", "");
        }
        return str;
    }


}

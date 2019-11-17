package com.hongchao.bilibilidanmaku;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

        private boolean showDanmaku;

        private DanmakuView danmakuView;

        //创建 DanmakuContext 实例，方便进行对弹幕的全局配置进行设定（如：字体，最大显示行数）
        private DanmakuContext danmakuContext;

        //弹幕解析器
        private BaseDanmakuParser parser = new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            /**
             * 获取一个 VideoView 的实例，
             * 给其设置一个视频文件的地址（事先要将视频文件置于sd卡根目录下，我用的是《华尔街之狼》），
             *，然后调用start()开始播放
             */
            VideoView videoView =  findViewById(R.id.video_view);
            videoView.setVideoPath(Environment.getExternalStorageDirectory() + "/Pixels.mp4");
            videoView.start();


            danmakuView =  findViewById(R.id.danmaku_view);
            //使用 enableDanmakuDrawingCache（） 提升绘制效率
            danmakuView.enableDanmakuDrawingCache(true);
            danmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    showDanmaku = true;
                    danmakuView.start();
                    generateSomeDanmaku();
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {

                }

                @Override
                public void drawingFinished() {

                }
            });

            //创建 DanmakuContext 实例，方便进行对弹幕的全局配置进行设定（如：字体，最大显示行数）
            danmakuContext = DanmakuContext.create();
            danmakuView.prepare(parser, danmakuContext);

            //绑定布局控件
            final LinearLayout operationLayout =  findViewById(R.id.operation_layout);
            final Button send = findViewById(R.id.send);
            final EditText editText = findViewById(R.id.edit_text);
            //设置点击事件,显示的话就隐藏,隐藏的话就显示
            danmakuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (operationLayout.getVisibility()==View.GONE){
                        operationLayout.setVisibility(View.VISIBLE);
                    }else{
                        operationLayout.setVisibility(View.GONE);
                    }
                }
            });
            //给send button 设置一个点击事件,当点击发送时,获取EditText输入内容
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = editText.getText().toString();
                    if (!TextUtils.isEmpty(content)){//content不为空
                        //添加到danmakuView中
                        addDanmaku(content,true);//true:是我发的,要加边框
                        editText.setText("");//发送之后，内容置空
                    }
                }
            });
            /**
             * 由于系统输入法弹出的时候会导致焦点丢失，从而退出沉浸式模式，
             * 因此这里还对系统全局的UI变化进行了监听，
             * 保证程序一直可以处于沉浸式模式。
             */
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility==View.SYSTEM_UI_FLAG_VISIBLE){
                        onWindowFocusChanged(true);
                    }
                }
            });
        }

    /**
     * 实现沉浸式体验
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    /**
     * 向弹幕View中添加一条弹幕
     *          弹幕的具体内容
     *          弹幕是否有边框
     */
    //withBorder参数 用于指定弹幕消息是否带有边框，用于自己与比人发的弹幕进行区分
    private void addDanmaku(String content, boolean withBorder) {
        //创建 BaseDanmaku 实例，TYPE_SCROLL_RL表示弹幕从右向左滑动
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;//弹幕内容
        danmaku.padding = 5;//弹幕之间的内边距
        danmaku.textSize = sp2px(20);//弹幕字体大小
        danmaku.textColor = Color.WHITE;//弹幕颜色
        danmaku.setTime(danmakuView.getCurrentTime());//弹幕显示时间
        if (withBorder) {//如果是自己发的，则加上边框，且边框颜色为 绿色
            danmaku.borderColor = Color.GREEN;
        }
        danmakuView.addDanmaku(danmaku);
    }

    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (danmakuView != null && danmakuView.isPrepared() && danmakuView.isPaused()) {
            danmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showDanmaku = false;
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }


    }
}

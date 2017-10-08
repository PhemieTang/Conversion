package com.scnu.language_conversion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
//import com.iflytek.cloud.thirdparty.R;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

//import com.iflytek.isvdemo.R;

//Toast是Android系统提供的一种非常好的提醒方式，在程序中可以使用它将
// 一些短小的信息通知给用户，这些信息会在一段时间后自动消失，并且不会占用任何屏幕空间。

@SuppressLint("ShowToast")
public class YuTestActivity extends AppCompatActivity implements View.OnClickListener{
    private static String TAG = YuTestActivity.class.getSimpleName();
    //用HashMap存储听写结果

    private HashMap<String,String> mIatResults = new LinkedHashMap<String,String >();
    private EditText mResultText;
    private SpeechSynthesizer mTts;//语音合成
    private SpeechRecognizer mIat;//语音听写
    private RecognizerDialog iatDialog;//听写动画

    //0 小燕 青年女声 中英文 （普通话） xiaoyan
    //1 默认 小宇 青年男声 中英文 （普通话） xiaoyu
    //2 凯瑟琳 青年女声 中英文 （普通话） catherine
    //3 亨利 青年男声 英文 henry
    //4 玛丽 青年女声 英文 vimary
    //5 小研 青年女声 中英文 （普通话）vixy
    //6 小琪 青年女声 中英文 （普通话）vixq xiaoqi
    //7 小峰 青年男声 中英文 （普通话）vixf
    //8 小梅 青年女声 中英文 （粤语）vixm xiaomei
    //9 小莉 青年女声 中英文 （台湾普通话）vixl xiaolin
    //10 小蓉 青年女声 汉语 （四川话）vixr xiaorong
    //11 小芸 青年女声 汉语 （东北话）vixyun xiaoqian
    //12 小坤 青年男声 汉语（河南话）vixk xiaokun
    //13 小强 青年男声 汉语（湖南话）vixqa xiaoqiang
    //14 小莹 青年女声 汉语（陕西话）vixying
    //15 小新 童年男声 汉语（普通话）vixx xiaoxin
    //16 楠楠 童年女声 汉语（普通话）vinn nannan
    //17 老孙 老年男声 汉语（普通话）

    private String[] voiceName={
            "xiaoyan","xiaoyu","catherine",
            "henry","vimary","vixy","xiaoqi",
            "vixf","xiaomei","xiaolin","xiaorong",
            "xiaoqian","xiaokun","xiaoqiang",
            "vixying","xiaolin","nannan","vils"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);//给当前活动加载一个布局
        mResultText=(EditText) findViewById(R.id.mResultText);
        Button bt_Synthesizer = (Button) findViewById(R.id.bt_Synthesizer);
        Button bt_Recognizer = (Button) findViewById((R.id.bt_Recognizer));
        bt_Synthesizer.setOnClickListener(this);
        bt_Recognizer.setOnClickListener(this);

        //初始化即创建语音配置对象，只有初始化后才可以使用MSC的各项服务
        SpeechUtility.createUtility(this,"appid=58fb7113");

        //语音合成1.创建SpeechSynthesizer对象，第二个参数：本地合成时传InitListener
        //mTtsInitListener是初始化监听器
        mTts=SpeechSynthesizer.createSynthesizer(YuTestActivity.this,mTtsInitListener);

        //语音听写1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat=SpeechRecognizer.createRecognizer(YuTestActivity.this,mTtsInitListener);

        //听写动画1.创建SpeechRecognizerDialog对象，第二个参数：本地听写时传InitListener
        iatDialog=new RecognizerDialog(this,mTtsInitListener);
    }
    /*
    *初始化语音合成相关数据
    *
    * @Description
     */
    public void starSynthesizer(){
        String content=mResultText.getText().toString().trim();//content就是听的结果

        //2.合成参数设置，详见《科大讯飞MSC API手册（Android）》SpeechSynthesizer类
        mTts.setParameter(SpeechConstant.VOICE_NAME,voiceName[5]);//设置发音人
        mTts.setParameter(SpeechConstant.SPEED,"50");//设置合成语速
        mTts.setParameter(SpeechConstant.VOLUME,"80");//设置合成音量，范围0~100
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置合成音调
        mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);//设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在"./sdcard/iflytek.pcm"
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,"./sdcard/iflytek.pcm");
        //3.开始合成
        mTts.startSpeaking(content,mSynListener);
        //合成监听器
        //
    }
    /**
     * 初始化参数开始听写
     *
     * @ Description
     */
    private void starRecognizer(){
        //2.设置听写参数，详见《科大讯飞MSC API手册（Android）》SpeechConstant类

        //语音识别应用领域(:iat,search,video,poi,music)
        mIat.setParameter(SpeechConstant.DOMAIN,"iat");
        //接受语言中文
        mIat.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
        //接受的语言是普通话
        mIat.setParameter(SpeechConstant.ACCENT,"madarin");
        //设置听写引擎（云端）
        mIat.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
        //设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE,"json");
        iatDialog.setListener(mRecognizerDialogListener);
        iatDialog.show();
        Toast.makeText(getApplicationContext(),"请开始说话...",Toast.LENGTH_SHORT).show();
        //3.开始听写
        //mIat.startListening(mRecoListener);
        //听写监听器
    }
    /**
     * 语言听写监听
     */
    private RecognizerListener mRecoListener=new RecognizerListener() {
        //听写结果回调接口（返回Json格式结果，用户可参见附录12.1）；
        //一般情况下会通过onResult接口多次返回结果，完整的识别内容是多次结果的累加；
        //关于解析Json的代码可参见MscDemo中JsonParser类；
        //isLast等于true是会话结束

        @Override
        public void onResult(RecognizerResult results,boolean isLast){
            Log.d(TAG,results.getResultString());
            printResult(results);
        }

        @Override
        //会话发生错误回调接口
        public void onError(SpeechError error){
            //错误码：10118（您没有说话），可能是录音机权限被禁，需要提醒用户打开应用的录音权限。
            if(error.getErrorCode()==10118){
                Toast.makeText(getApplicationContext(),"你好像没有说话哦",Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(),error.getPlainDescription(true),Toast.LENGTH_SHORT).show();

        }//获取错误码描述

        @Override
        //开始录音
        public void onBeginOfSpeech(){
            Log.d(TAG,"开始说话");
            Toast.makeText(getApplicationContext(),"开始说话",Toast.LENGTH_SHORT).show();
        }

        @Override
        //结束录音
        public void onEndOfSpeech(){
            Log.d(TAG,"说话结束");
            Toast.makeText(getApplicationContext(),"说话结束",Toast.LENGTH_SHORT).show();
        }

        @Override
        //扩展用接口
        public void onEvent(int eventType,int arg1,int arg2,Bundle obj){

        }
        @Override
        public void onVolumeChanged(int volume,byte[] data) {
            // TODO Auto-generated method stub
            Log.d(TAG,"当前说话音量大小"+volume);

        }



};


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener=new RecognizerDialogListener()
    {
        @Override
        public void onResult(RecognizerResult results, boolean isLast)
        {
            printResult(results);
        }

        /**
         *识别回调错误
         */

        @Override
        public void onError(SpeechError error) {
            Toast.makeText(getApplicationContext(),error.getPlainDescription(true),
                    Toast.LENGTH_SHORT).show();
        }

    };

    /**
     * 语音合成监听
     */
    private SynthesizerListener mSynListener=new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error){
            if(error !=null){
                Log.d("mySynthesiezer code:",error.getErrorCode() + " ");
            }
            else{
                Log.d("mySynthesiezer code:","0");
            }
        }

        @Override
        public void onSpeakBegin() {

        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息
        public void onBufferProgress(int oercent,int beginPos,int endPos,
                                     String info){

        }

        //开始播放
        public void onSpeakPaused(){

        }

        //播放进度回调
        //percent为播放进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置
        public  void onSpeakProgress(int percent,int beginPos,int endPos){

        }

        //恢复播放回调接口
        public void onSpeakResumed(){

        }

        //回话事件回调接口
        public void onEvent(int arg0,int arg1,int arg2,Bundle arg3){

        }
    };

    /**
     * 初始化语音合成监听
     */
    private InitListener mTtsInitListener=new InitListener() {
        @SuppressLint("ShowToast")
        @Override
        public void onInit(int code) {
            Log.d(TAG,"InitListener init() code=" + code);
            if(code!= ErrorCode.SUCCESS){
                //showTip("初始化失败，错误码："+code);
                Toast.makeText(getApplication(),"初始化失败，错误码：" + code,
                        Toast.LENGTH_SHORT).show();
            }
            else {
                //初始化成功，之后可以调用startSpeaking方法
                //注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                //正确的做法是将onCreate中的startSpeaking调用移至这里
                String content=mResultText.getText().toString().trim();
                mTts.startSpeaking(content,mSynListener);
            }
        }
    };
    @Override
    public void onClick(View v){
        //TODO Auto-generated method stub

        switch (v.getId()){
            case R.id.bt_Recognizer:
                starRecognizer();
                break;
            case R.id.bt_Synthesizer:
                //mResultText.setText(null);//清空显示内容
                //mIatResults.clear();
                starSynthesizer();
                break;
            default:
                break;
        }
    }

    //将语音识别的结果（json格式）转换为人类能识别的通顺的语句，显示到EditText里面。
    private void printResult(RecognizerResult results){
        String text=JsonParser.parseIatResult(results.getResultString());

        String sn=null;
        //读取json结果中的sn字段
        try{
            JSONObject resultJson=new JSONObject(results.getResultString());
            sn=resultJson.optString("sn");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        mIatResults.put(sn,text);

        StringBuffer resultBuffer=new StringBuffer();
        for(String key:mIatResults.keySet()){
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());

    }
}



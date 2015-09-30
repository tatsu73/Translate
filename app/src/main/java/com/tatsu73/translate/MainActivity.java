package com.tatsu73.translate;

import java.util.ArrayList;
import java.util.Locale;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView.BufferType;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    // 変数
    private static final int REQUEST_CODE = 0;
    public static final int MENU_SELECT_J = 0;
    public static final int MENU_SELECT_E = 1;
    public static final int MENU_SELECT_C = 2;
    public static final int MENU_SELECT_F = 3;
    private EditText et;
    private EditText et2;
    private TextToSpeech tts;
    private String to;
    private String from;
    private LanguageList lList;
    private SharedPreferences preferences;
    private boolean btnFlg = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getActionBar().hide();
        //言語設定
        getPreferences();
        // TextToSpeechオブジェクトの生成
        tts = new TextToSpeech(this, this);
        // ボタン作成
        ImageButton mic_button = (ImageButton)findViewById(R.id.imageButton);
        ImageButton trans_button = (ImageButton)findViewById(R.id.imageButton2);
        ImageButton rep_button = (ImageButton)findViewById(R.id.imageButton4);
        // ボタンにイベント設定
        mic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // インテント作成
                    Intent intent = new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "speak");

                    // インテント発行
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // このインテントに応答できるアクティビティがインストールされていない場合
                    Toast.makeText(MainActivity.this,
                            "ActivityNotFoundException", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        trans_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {
                    changeText();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        rep_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceLanguage(to,from);
            }
        });


        et2 = (EditText)findViewById(R.id.editText2);
        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(et2.getText().toString().isEmpty()){
                    deleteButton();
                } else if(!btnFlg){
                    createButton();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.add(0, MENU_SELECT_J, 0, "日本語")
                .setIcon(R.mipmap.japan);
        menu.add(0, MENU_SELECT_E, 0, "英語")
                .setIcon(R.mipmap.uk);
        menu.add(0, MENU_SELECT_F, 0, "フランス語")
                .setIcon(R.mipmap.france);
        menu.add(0, MENU_SELECT_C, 0, "中国語")
                .setIcon(R.mipmap.china);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        lList = new LanguageList();
        int id = item.getItemId();
        switch (id) {
            case MENU_SELECT_J:
                to = lList.getJ();
                break;
            case MENU_SELECT_E:
                to = lList.getE();
                break;
            case MENU_SELECT_F:
                to = lList.getF();
                break;
            case MENU_SELECT_C:
                to = lList.getC();
                break;
        }
        setHint();
        return true;
    }

    // アクティビティ終了時に呼び出される
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "";
            // 結果文字列リスト
            ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results.size() > 0) {
                resultsString += results.get(0);
                et = (EditText) findViewById(R.id.editText);
                et.setText(resultsString, BufferType.NORMAL);
            }
            // トーストを使って結果を表示
            // Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.ENGLISH;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("", "Error SetLocale");
            }
        } else {
            Log.d("", "Error Init");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences = getSharedPreferences("Languages", Context.MODE_PRIVATE);
        SharedPreferences.Editor  editor = preferences.edit();
        editor.putString("to",to);
        editor.putString("from",from);
        editor.commit();
        if (null != tts) {
            // TextToSpeechのリソースを解放する
            tts.shutdown();
        }
    }

    private void speechText() {
        String string = ((EditText) findViewById(R.id.editText2)).getText()
                .toString();
        if (0 < string.length()) {
            if (tts.isSpeaking()) {
                // 読み上げ中なら止める
                tts.stop();
            }

            // 読み上げ開始
            tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void changeText() throws Exception{
        et = (EditText)findViewById(R.id.editText);
        et2 = (EditText)findViewById(R.id.editText2);

        String trnStr = et.getText().toString();

        Translate.setClientId("tatsu73_toaking");
        Translate.setClientSecret("i3C00NxkKFp5jZ2Jz6grJ/GZA4CqtjUHJiwEQASaSQ8=");
        try {
            //String resStr = Translate.execute(trnStr, LanguageList.JAPANESE, LanguageList.ENGLISH);
            String resStr = Translate.execute(trnStr, Language.valueOf(from), Language.valueOf(to));
            et2.setText(resStr);
        }catch (Exception e){
            e.printStackTrace();
            et2.setText("error");
        }
        if(!btnFlg){
            createButton();
        }
    }

    private void replaceLanguage (String pre_to, String pre_from){
        to = pre_from;
        from = pre_to;
        setHint();
        deleteButton();
    }

    private void getPreferences (){
        preferences = this.getSharedPreferences("Languages", Context.MODE_PRIVATE);
        to = preferences.getString("to", "ENGLISH");
        from = preferences.getString("from", "JAPANESE");
        setHint();
    }

    private void setHint(){
        EditText et1 = (EditText)findViewById(R.id.editText);
        EditText et2 = (EditText)findViewById(R.id.editText2);
        et1.getEditableText().clear();
        et2.getEditableText().clear();
        et1.setHint(from);
        et2.setHint(to);
    }

    private void createButton(){
        int paddingPx = 5;  // dpの値を指定
        int marginPx = 15;
        float scale = getResources().getDisplayMetrics().density; //画面のdensityを指定。
        int marginDp = (int) (marginPx * scale + 0.5f);
        int paddingDp = (int)(paddingPx * scale + 0.5f);
        //(int) (paddingDp * scale)とした場合は端数は切り捨て
        ImageButton speak_btn = new ImageButton(this);
        speak_btn.setImageResource(R.mipmap.speak_hdpi);
        speak_btn.setBackgroundResource(R.drawable.speak_btn_style);
        LinearLayout.LayoutParams lp =new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        lp.setMargins(lp.leftMargin, marginDp, lp.rightMargin, lp.bottomMargin);
        speak_btn.setLayoutParams(lp);
        speak_btn.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        speak_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                speechText();
            }
        });
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout);
        linearLayout.addView(speak_btn);
        btnFlg = true;
        //setContentView(linearLayout);
    }

    private void deleteButton(){
        if(btnFlg) {
            LinearLayout linearLayout = (LinearLayout)findViewById(R.id.layout);
            linearLayout.removeViewAt(1);
            btnFlg = false;
        }
    }


}

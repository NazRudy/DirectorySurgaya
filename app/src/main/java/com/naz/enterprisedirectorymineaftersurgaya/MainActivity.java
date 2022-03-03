package com.naz.enterprisedirectorymineaftersurgaya;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnInitListener{
    private final String FILENAME = "spravochnik.txt";
    private final String FILENAMER = "spravochnikr.txt";
    private final String FILENAMET = "spravochnikt.txt";
    private  String spravochnik=""; // переменная для справочника произношения
    private  String spravochnikr=""; // переменная для справочника распознования
    private String spravochnikt=""; // переменная для справочника отображения
    private String[] resultWords={" ", " ", " "}; // переменная список результатов распознования
    private String marker="s"; // переменная для определения, что произносить Текст или Номер телефона
    private ArrayList<Integer> resFind=new ArrayList<>(); // переменная номера позиции в справочнике произношения
    private String resWord="start";
    private String[] spvwt={"0", "1"};
    private ArrayList<String> infoall=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    //переменная для проверки возможности
    //распознавания голоса в телефоне
    private static final int VR_REQUEST=999;

    //ListView для отображения распознанных слов
    private ListView wordList;

    //Log для вывода вспомогательной информации
    private final String LOG_TAG="SpeechRepeatActivity";
    //***здесь можно использовать собственный тег***

    //переменные для работы TTS
    //переменная для проверки данных для TTS
    private int MY_DATA_CHECK_CODE=0;
    //Text To Speech интерфейс
    private TextToSpeech repeatTTS;
    TextView root_tv;
    LinearLayout infot_tv;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repeatTTS=new TextToSpeech(this, this);

        ImageButton speechBtn=(ImageButton) findViewById(R.id.speech_btn);

        infot_tv=(LinearLayout)findViewById(R.id.infot_tv);
        TextView textViewStart = new TextView(this);
        textViewStart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textViewStart.setTextColor(Color.parseColor("#00FFFF"));
        textViewStart.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewStart.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        textViewStart.setText("Для использования справочника нажмите кнопку с микрофоном и \nПОСЛЕ СИГНАЛА\n произнесите номер телефона или его местонахождение или \nучасток или службу.");
        infot_tv.addView(textViewStart);

        root_tv=(TextView)findViewById(R.id.roottextviev);





        // загружаем файлы справочников
        spravochnik=openFileSprAssets(getAssets(), FILENAME);
        //rootTextViev.setText(spravochnik);

        spravochnikr=openFileSprAssets(getAssets(), FILENAMER);
        //rootTextViev.setText(spravochnikr);
        spravochnikt=openFileSprAssets(getAssets(), FILENAMET);


        //проверяем, поддерживается ли распознование речи
        PackageManager packManager= getPackageManager();
        List<ResolveInfo> intActivities= packManager.queryIntentActivities(new
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if(intActivities.size()!=0){
            // распознавание поддерживается, будем отслеживать событие щелчка по кнопке
            speechBtn.setOnClickListener(this);
        }
        else
        {
            // распознавание не работает. Заблокируем
            // кнопку и выведем соответствующее
            // предупреждение.
            speechBtn.setEnabled(false);
            Toast.makeText(this,"Распознование речи не потдерживается!", Toast.LENGTH_LONG).show();
        }



        //...
        //засекаем щелчок пользователя по слову из списка
        //wordList.setOnItemClickListener(new OnItemClickListener() {

        //метод вызывается в ответ на щелчок по слову
        //    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        //    {
//записываем в переменную TextView строки
        //       TextView wordView=(TextView)view;
//получаем строку с текстом
        //        String wordChosen=(String) wordView.getText();
//выводим ее в лог для отладки
        //        Log.v(LOG_TAG,"chosen: "+wordChosen);
//выводим Toast сообщение
        //        Toast.makeText(MainActivity.this,"You said: "+wordChosen,
        //                Toast.LENGTH_SHORT).show();
        //        repeatTTS.speak(wordChosen, TextToSpeech.QUEUE_FLUSH,null);
        //    }
        //});
        //...


    }

    @Override
    public void onInit(int initStatus){
        if(initStatus== TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(Locale.getDefault());//Язык
    }


    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.speech_btn){
            // отслеживаем результат
            listenToSpeech();
        }
    }

    private void listenToSpeech(){
//запускаем интент, распознающий речь и передаем ему требуемые данные
        Intent listenIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//указываем пакет
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getClass().getPackage().getName());
//В процессе распознования выводим сообщение
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a word!");
//устанавливаем модель речи
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//указываем число результатов, которые могут быть получены
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);

//начинаем прослушивание
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

//проверяем результат распознавания речи
        if(requestCode== VR_REQUEST && resultCode== RESULT_OK)
        {
//Добавляем распознанные слова в список результатов
            ArrayList<String> suggestedWords= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//Передаем список возможных слов
            if (suggestedWords.size()<1)suggestedWords.add(" ");
            if (suggestedWords.size()<2)suggestedWords.add(" ");
            if (suggestedWords.size()<3)suggestedWords.add(" ");
            for (int i=0; i<3; i++) {
                resultWords[i] = suggestedWords.get(i);
            }
            //screennastr();
        }
        // if (!resultWords[0].equals(" ")){ // слушаем кнопку, если результат есть, то ишем
        resFind.clear();
        resFind.add(-1);
        for (int i=0; i<3; i++) {
            findsprvr(spravochnikr, resultWords[i]);
            //provmasiv[0]=provstring;
            //provmasiv[1]=String.valueOf(resFind);
            //provmasiv[2]=marker;
            //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, provmasiv));
            if (resFind.get(0) >= 0) {
                i = 3; // закончить цикл если найдено совпадение
                //resWord = findsprv(spravochnik, resFind, marker);
                //}
                //repeatTTS.speak(resWord, TextToSpeech.QUEUE_FLUSH, null);
                //подготовка движка TTS для проговаривания слов
                //Intent checkTTSIntent=new Intent();
                //проверка наличия TTS
                //checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                //запуск checkTTSIntent интента
                //startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            }
        }
            resWord = findsprv(spravochnik, resFind.get(0), marker);
            infoall.clear();
            for (int j=0; j<resFind.size(); j++){
                findsprot(spravochnikt, resFind.get(j));
                infoall.add(spvwt[1].replaceAll("\r\n+$",""));
                infoall.add(spvwt[0]);
            }

            screeninfo();




            //infow_tv.setText(spvwt[0]);
            repeatTTS.speak(resWord, TextToSpeech.QUEUE_FLUSH, null);


        //repeatTTS.speak("Информация не найдена", TextToSpeech.QUEUE_FLUSH, null);
        //}

//tss код здесь
        //returned from TTS data check
        //if(requestCode== MY_DATA_CHECK_CODE)
        //{
//все необходимые приложения установлены, создаем TTS
        //if(resultCode== TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
        //repeatTTS=new TextToSpeech(this, this);
//движок не установлен, предположим пользователю установить его
        // else
        // {
//интент, перебрасывающий пользователя на страницу TSS в Google Play
        // Intent installTTSIntent=new Intent();
        //  installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        //  startActivity(installTTSIntent);
        // }
        // }

//вызываем метод родительского класса
        super.onActivityResult(requestCode, resultCode, data);
    }

    // поиск в справочнике Google
    private void findsprvr(String sprv, String words){ // передаем Справочник поиска, строку поиска
        int indexyes=0;
        marker="not";
        String[] subwords; String delimetrw=" ";
        subwords=words.split(delimetrw);  // разбиваем строку поиска на слова
        //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, subwords));
        String[] subsprv; String delimetrs="#";
        subsprv=sprv.split(delimetrs); // разбиваем справочник поиска на строки данных
        //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, subsprv));
        String[] subtspr = {"sw", "st"}; String delimetrt=";";

        for (int i=0; i<subsprv.length; i++){// цикл по количеству строк днных
            //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, subsprv));
            subtspr=subsprv[i].split(delimetrt); // разбиваем строку данных на текст и номер телефона
            //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, subtspr));
            indexyes=0;
            for (int j=0; j<subwords.length; j++) {  // цыкл по колиеству слов
                //provmasiv[0]=subwords[j]; provmasiv[1]=subtspr[0];
                //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, provmasiv));
                if (Arrays.asList(subtspr[0].split(" ")).contains(subwords[j])) {  // если слово есть в строке, увеличмваем
                    //provmasiv[0]=subwords[j]; provmasiv[1]=subtspr[0];
                    //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, provmasiv));
                    indexyes++;
                    if (indexyes == subwords.length) {  // если все слова найдены,
                        if (resFind.get(0)==-1) resFind.set(0,i);
                        else resFind.add(i);
                        j=subwords.length;
                        marker = "w"; // то это слово, номер строки для спрвочника произношения
                    }
                }
            }
            indexyes=0;
            for (int j=0; j<subwords.length; j++){
                if (Arrays.asList(subtspr[1].split(" ")).contains(subwords[j])) { // если номер есть в строке,
                    indexyes++;     // то увеличиваем
                    if (indexyes==subwords.length){ // если все цифры найдены,
                        if (resFind.get(0)==-1) resFind.set(0,i);
                        else resFind.add(i);
                        j=subwords.length;
                        marker="t"; // то этот номер телефона, номер строки для справочника произношения
                    }
                }
            }
        }

    }
    // поиск в справочнике для произношения
    private String findsprv(String sprv, int index, String marker){
        if (index==-1)return "Информация не найдена";
        String word="";
        String[] subsprv; String delimetr="#";
        subsprv=sprv.split(delimetr); // разьиваем справочник на строки

        String delimetrwt=";";
        spvwt=subsprv[index].split(delimetrwt); // разбиваем нужную строку на строку и телефон
        if (marker.equals("w")) word=spvwt[1]; // если w, то на выходе строка
        else
        if (marker.equals("t")) word=spvwt[0]; // если t, то на выходе телефон
        else
            word="Информация не найдена"; // если чтото другое, то на выходе
        return word;
    }
    private void findsprot(String sprv, int index){

        if (index==-1){
            spvwt[0]=" "; spvwt[1]="Информация не найдена!";
            return;
        }else {
            String[] subsprv; String delimetr="#";
            subsprv=sprv.split(delimetr); // разьиваем справочник на строки
            String delimetrwt=";";
            spvwt=subsprv[index].split(delimetrwt); // разбиваем нужную строку на строку и телефон
        }
    }

    private String openFileSprAssets(AssetManager manager, String sprav ) {
        byte[] buffer=null;
        InputStream is;
        try {
            is=getAssets().open(sprav);
            int size=is.available();
            buffer=new byte[size];
            is.read(buffer);
            is.close();
        }
        catch (Throwable throwable) {
            Toast.makeText(getApplicationContext(),
                    "Ошибка загрузки файла справочника", Toast.LENGTH_LONG)
                    .show();
        }
        return new String(buffer);
    }
    private void screeninfo(){
        infot_tv.removeAllViews();
        int alfatw=0;
        int alfaindex=0;
        //int redtw=0;
        //int greentw=0;
        //int bluetw=0;

        for (int i=0; i<infoall.size(); i++){
            alfaindex++;
            if (alfaindex%2==0)alfatw=80;
            else alfatw=40;
            TextView textViewT = new TextView(this);
            textViewT.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textViewT.setTextColor(Color.parseColor("#FFFF00"));
            textViewT.setBackgroundColor(Color.argb(alfatw,128, 128, 128));
            textViewT.setTypeface(Typeface.DEFAULT_BOLD);
            textViewT.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            textViewT.setText(infoall.get(i));
            infot_tv.addView(textViewT);
            i++;
            TextView textViewW = new TextView(this);
            textViewW.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textViewW.setTextColor(Color.parseColor("#00FF00"));
            textViewW.setBackgroundColor(Color.argb(alfatw,128, 128, 128));
            textViewW.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            textViewW.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            textViewW.setText(infoall.get(i));
            infot_tv.addView(textViewW);
        }
    }

    private void screennastr() {
        infot_tv.removeAllViews();
        for (int i = 0; i < resultWords.length; i++) {
            TextView textViewW = new TextView(this);
            textViewW.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textViewW.setTextColor(Color.parseColor("#00FF00"));
            textViewW.setBackgroundColor(Color.argb(100, 128, 128, 128));
            textViewW.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            textViewW.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);

            textViewW.setText(resultWords[i]);
            infot_tv.addView(textViewW);
        }
    }

}
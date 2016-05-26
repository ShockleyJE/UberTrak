package ubertrak.shockleyco.com.ubertrak;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


public class MainActivity extends AppCompatActivity implements RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";

    /* Keyword we are looking for to activate reset */
    private static final String KEYPHRASE = "reset counter ";
    /*Keyword we are looking for to acvivate reset pop*/
    private static final String KEYPHRASE_POP = "med popped ";
    /*Keyword we are looking for to acvivate reset pop*/
    private static final String KEYPHRASE_DOWN = "med down ";
    /*Keyword we are looking for to acvivate reset pop*/
    private static final String KEYPHRASE_MOM = "mom get the camera ";


    private int popWait= 8000;
    private int dropWait= 10000;

    private SpeechRecognizer recognizer;
    int conteo = 0;
    int permiso_flag=0;
    Handler a = new Handler();

    private Toolbar toolbar;

    private int uber = -69;
    private int kritz= -69;

    private static final String UBER = "-69";
    private static final String KRITZ= "-69";

    private int uberMilliDelay= 400;
    private int kritzMilliDelay= 320;

    private static final int UBERMILLIDELAY= 400;
    private static final int KRITZMILLIDELAY=320;

    private static final double UGCSTEELMULTIPLIER= 1.25;
    private static final double BABBYMULTIPLIER= 1.5;

    TextView uberCurrent;
    TextView kritzCurrent;

    private boolean changedUberBackground = false;
    private boolean changedKritzBackground = false;


    private CardView viewUber;
    private CardView viewKritz;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerUberHandler = new Handler();
    Runnable timerUberRunnable = new Runnable() {

        @Override
        public void run() {

            if(uber< 100){
                uber++;
                updateUber();
            }
            else if(uber == 100){
                pauseRunnables('u');
            }

            if( uber== 100 && changedUberBackground == false){
                viewUber.setBackgroundColor(getResources().getColor(R.color.primaryAccentBlue));
                changedUberBackground= true;
            }

            timerUberHandler.postDelayed(this, uberMilliDelay);
        }
    };

//    final CardView viewKritz = (CardView) findViewById(R.id.card_view_main_kritz);

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerKritzHandler = new Handler();
    Runnable timerKritzRunnable = new Runnable() {

        @Override
        public void run() {

            if(kritz< 100){
                kritz++;
                updateKritz();
            }
            else if(kritz == 100){
                pauseRunnables('k');
            }

            if( kritz== 100 && changedKritzBackground == false){
                viewKritz.setBackgroundColor(getResources().getColor(R.color.primaryAccentBlue));
                changedKritzBackground= true;
            }

            timerKritzHandler.postDelayed(this, kritzMilliDelay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar= (Toolbar) findViewById(R.id.main_bar);
        setSupportActionBar(toolbar);

        viewUber= (CardView) findViewById(R.id.card_view_main_uber);
        viewKritz= (CardView) findViewById(R.id.card_view_main_kritz);


        if(savedInstanceState == null){

            uber= 0;
            kritz= 0;

        } else {

            uber= savedInstanceState.getInt(UBER);
            kritz= savedInstanceState.getInt(KRITZ);

        }

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplicationContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                } else {
                    searchReset(KWS_SEARCH);
                }
            }
        }.execute();

        LinearLayout popButton = (LinearLayout) findViewById(R.id.main_linear_layout);

        popButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                reset();
            }
        });

        CardView oneButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_Hardcore);

        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= UBERMILLIDELAY;
                kritzMilliDelay=KRITZMILLIDELAY;
            }
        });

        CardView onePointTwoFiveButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_UGC_Steel);

        onePointTwoFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= (int) (UBERMILLIDELAY * UGCSTEELMULTIPLIER);
                kritzMilliDelay= (int) (KRITZMILLIDELAY * UGCSTEELMULTIPLIER);

            }
        });

        CardView onePointFiveButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_Babbies);

        onePointFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= (int) (UBERMILLIDELAY * BABBYMULTIPLIER);
                kritzMilliDelay= (int) (KRITZMILLIDELAY * BABBYMULTIPLIER);

            }
        });

        uberCurrent= (TextView) findViewById(R.id.uberPercentTextView);
        kritzCurrent= (TextView) findViewById(R.id.kritzPercentTextView);

//        timerUberHandler.postDelayed(timerUberRunnable, 0);
//        timerKritzHandler.postDelayed(timerKritzRunnable, 0);

    }

    private void searchReset(String searchName){

        recognizer.stop();

        if(searchName.equals(KWS_SEARCH)){
            recognizer.startListening(searchName);
        }

        else if(searchName.equals(KEYPHRASE)){
            reset();
            recognizer.startListening(KWS_SEARCH);

        }

        else if(searchName.equals(KEYPHRASE_POP)){
            medDelay(popWait);
            recognizer.startListening(KWS_SEARCH);

        }

        else if(searchName.equals(KEYPHRASE_DOWN)){
            medDelay(dropWait);
            recognizer.startListening(KWS_SEARCH);

        }

        else if(searchName.equals(KEYPHRASE_MOM)){
            reset();
            recognizer.startListening(KWS_SEARCH);

        }


        else {
            recognizer.startListening(KWS_SEARCH);

        }

    }

    private void medDelay(int delay){

        reset();
        pauseRunnables('k');
        pauseRunnables('u');

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                pauseRunnables('k');
                pauseRunnables('u');

                resumeRunnables('u');
                resumeRunnables('k');

            }
        },delay);
    }

    @Override
    protected void onResume(){
        super.onResume();

        resumeRunnables('k');
        resumeRunnables('u');



    }

    @Override
    protected void onPause(){
        super.onPause();
        pauseRunnables('k');
        pauseRunnables('u');


    }


    private void pauseRunnables(char c){
        if(c== 'k'){
            timerKritzHandler.removeCallbacks(timerKritzRunnable);
        }
        else if(c== 'u'){
            timerUberHandler.removeCallbacks(timerUberRunnable);
        }

    }

    private void resumeRunnables(char c){
        if(c=='k'){
            timerKritzHandler.postDelayed(timerKritzRunnable, kritzMilliDelay);
        }
        if(c=='u'){
            timerUberHandler.postDelayed(timerUberRunnable, uberMilliDelay);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    private void updateKritz() {

        kritzCurrent.setText(kritz + "%");

    }


    private void updateUber() {

        uberCurrent.setText(uber + "%");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){

        super.onSaveInstanceState(outState);

        outState.putDouble(UBER, uber);
        outState.putDouble(KRITZ, kritz);

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

        if (!recognizer.getSearchName().equals(KWS_SEARCH)) {
            searchReset(KWS_SEARCH);

        }

        searchReset("");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)){
            searchReset(text);
        }

    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            searchReset(text);
        }

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    /*
    resets the counters
     */
    public void reset(){

        uber = 0;
        kritz = 0;

        viewUber.setBackgroundColor(0xFFFFFFFF);
        viewKritz.setBackgroundColor(0xFFFFFFFF);

        changedKritzBackground= false;
        changedUberBackground= false;

        updateUber();
        updateKritz();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                        //.setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                        //changed from 1e-45f to 1e-5f with good results
                .setKeywordThreshold(1e-5f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
//        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);


        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addKeywordSearch(KWS_SEARCH, menuGrammar);
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}

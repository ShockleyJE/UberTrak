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

    /* Set wait counters for the drop and pop commands in ms*/
    private int popWait= 8000;
    private int dropWait= 10000;

    /* Initialize our speech recognizer*/
    private SpeechRecognizer recognizer;
    int conteo = 0;
    int permiso_flag=0;
    Handler a = new Handler();

    /* Initialize our toolbar */
    private Toolbar toolbar;

    /* Initialize our values for keeping up with uber %'s*/
    private int uber = -69;
    private int kritz= -69;

    /* Initialize our strings which will be displayed on their respective cards*/
    private static final String UBER = "-69";
    private static final String KRITZ= "-69";

    /* Initialize values for how often we want to update our charges*/
    /* These are variable because we options for charging slower/ faster based on skill multiplier*/
    private int uberMilliDelay= 400;
    private int kritzMilliDelay= 320;
    /* These are final because we want to have something to reset them back to if the user chooses to*/
    private static final int UBERMILLIDELAY= 400;
    private static final int KRITZMILLIDELAY=320;

    /* These are our multipliers for varying charge rate. They are rough approximations of how
    * fast someone charges at various skill levels, 1.00 being the most optimized */
    private static final double UGCSTEELMULTIPLIER= 1.25;
    private static final double BABBYMULTIPLIER= 1.5;

    /* Initialize text views so we can change them on updates*/
    TextView uberCurrent;
    TextView kritzCurrent;

    /* Initialize backgrounds to false, they will change color at 100%*/
    private boolean changedUberBackground = false;
    private boolean changedKritzBackground = false;

    /* Our cards that will house our textviews*/
    private CardView viewUber;
    private CardView viewKritz;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerUberHandler = new Handler();
    Runnable timerUberRunnable = new Runnable() {

        /* Loop for updating uber */
        /* Three conditions
        *   1) uber needs to be incremented and updated
        *   2) we are at full uber, pause the runnable
        *   3) we are at full uber and background isn't changed, we change it and record that
        *
        *   Delay the runnable by the variable delay time
        *
        * */
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

    //runs without a timer by reposting this handler at the end of the runnable
    /* This logic is the same as the uber handler above */
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

        /* Inflate our views */
        setContentView(R.layout.activity_main);

        toolbar= (Toolbar) findViewById(R.id.main_bar);
        setSupportActionBar(toolbar);

        viewUber= (CardView) findViewById(R.id.card_view_main_uber);
        viewKritz= (CardView) findViewById(R.id.card_view_main_kritz);

        /* Initialize our values to zero or pick up where we left off */
        if(savedInstanceState == null){
            uber= 0;
            kritz= 0;
        } else {
            uber= savedInstanceState.getInt(UBER);
            kritz= savedInstanceState.getInt(KRITZ);
        }

        /* This task allows us to run in the background */
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

        /* This is a backup for our recognizer not picking up on a phrase
        * the user can tap the screen to reset */
        LinearLayout popButton = (LinearLayout) findViewById(R.id.main_linear_layout);
        popButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });

        /* These are our three buttons for modifying the speed at which the counter fills
        * OnClick, they update the delay values */
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

        /* Now, we cast to text view so we can use them to update what the user sees */
        uberCurrent= (TextView) findViewById(R.id.uberPercentTextView);
        kritzCurrent= (TextView) findViewById(R.id.kritzPercentTextView);

    }

    /* This is where we set up our search logic for the keywords we defined */
    private void searchReset(String searchName){

        /* Stop the recognizer, otherwise we'd get multiple triggers if someone repeats the phrase */
        recognizer.stop();

        /* Each statement is triggered by recognizing the keyword
         * Logic to be triggered by the keyword command can be called prior to restarting the recognizer*/
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
        /* If we didn't recognize something, just restart the recognizer */
        else {
            recognizer.startListening(KWS_SEARCH);

        }

    }

    /* This is our logic for delaying the counter by a set delay represented in ms*/
    private void medDelay(int delay){

        reset();
        /* we need to pause the runnables */
        pauseRunnables('k');
        pauseRunnables('u');

        /* create a new handler and delay that by our parameter*/
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

    /* This removes all callbacks to our runnable during pauses */
    private void pauseRunnables(char c){
        if(c== 'k'){
            timerKritzHandler.removeCallbacks(timerKritzRunnable);
        }
        else if(c== 'u'){
            timerUberHandler.removeCallbacks(timerUberRunnable);
        }
    }
    /* This resumes our runnables after delaying them */
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

    /* Simple method for updating our Kritz text view, and append a % sign*/
    private void updateKritz() {
        kritzCurrent.setText(kritz + "%");
    }

    /* Simple method for updating our Uber text view, and append a % sign*/
    private void updateUber() {
        uberCurrent.setText(uber + "%");
    }

    /* This saves our state, really the only thing we need to save are current uber/ kritz values*/
    @Override
    protected void onSaveInstanceState(Bundle outState){

        super.onSaveInstanceState(outState);

        outState.putDouble(UBER, uber);
        outState.putDouble(KRITZ, kritz);

    }

    /* This takes a phrase the user has stated and implements logic to search for it's phonetic makeup*/
    /* In this case, our logic is that if we got a match for a keyphrase, to call searchReset to
    * do whatever that command the recognized keyphrase cooresponds to
    * else we just call searchReset with blank string to begin listening again */
    @Override
    public void onEndOfSpeech() {

        if (!recognizer.getSearchName().equals(KWS_SEARCH)) {
            searchReset(KWS_SEARCH);
        }
        searchReset("");
    }

    /* Here, we have the ability to re-check partial phonetic matches for keyphrases
     * This is more efficient than rechecking the entire speech phrase again */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)){
            searchReset(text);
        }
    }

    /* If we get a result, then we will show it as a toast message and pass the command to searchReset*/
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            searchReset(text);
        }

    }

    //TODO: Add exception handling
    @Override
    public void onError(Exception e) {}
    //TODO: setup timeout logic
    @Override
    public void onTimeout() {}

    /*
    logic for resetting the counters
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

                // To disable logging of raw audio comment out this call, artifact from debugging
                //.setRawLogDir(assetsDir)

                // Threshold to tune for keyphrase to balance between false alarms and misses
                //changed from 1e-45f to 1e-5f with good results
                .setKeywordThreshold(1e-5f)

                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);


        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addKeywordSearch(KWS_SEARCH, menuGrammar);
    }

}

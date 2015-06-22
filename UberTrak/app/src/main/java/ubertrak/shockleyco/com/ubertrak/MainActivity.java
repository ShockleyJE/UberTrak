package ubertrak.shockleyco.com.ubertrak;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    //instantiate that toolbar
    private Toolbar toolbar;

    //testing artifacts
    private int uber = -69;
    private int kritz= -69;

    //testing artifacts
    private static final String UBER = "-69";
    private static final String KRITZ= "-69";

    //variables for milliseconds per point charge
    private int uberMilliDelay= 400;
    private int kritzMilliDelay= 320;

    //finals for resetting adjustable rate
    private static final int UBERMILLIDELAY= 400;
    private static final int KRITZMILLIDELAY=320;

    //multipliers for 1.25x and 1.5x
    private static final double UGCSTEELMULTIPLIER= 1.25;
    private static final double BABBYMULTIPLIER= 1.5;

    //text views for updating percentages
    TextView uberCurrent;
    TextView kritzCurrent;

    //booleans to see if background has been changed
//    private boolean changedUberBackground = false;
//    private boolean changedKritzBackground = false;

    //card view for changing uber card background
//    CardView viewUber= (CardView) findViewById(R.id.card_view_main_uber);

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerUberHandler = new Handler();
    Runnable timerUberRunnable = new Runnable() {

        @Override
        public void run() {

            //don't wanna go over a hundie
            if(uber< 100){
                uber++;
                updateUber();
            }

//            else if( uber== 100 && changedUberBackground == false){
//                viewUber.setCardBackgroundColor(getResources().getColor(R.color.primaryAccentBlue));
//                changedUberBackground= true;
//            }

            //do it again mang, should probably put another if in there
            timerUberHandler.postDelayed(this, uberMilliDelay);
        }
    };

//    final CardView viewKritz = (CardView) findViewById(R.id.card_view_main_kritz);

    Handler timerKritzHandler = new Handler();
    Runnable timerKritzRunnable = new Runnable() {

        @Override
        public void run() {

            if(kritz< 100){
                kritz++;
                updateKritz();
            }

//            else if( uber== 100 && changedKritzBackground == false){
//                viewKritz.setBackgroundColor(getResources().getColor(R.color.primaryAccentBlue));
//                changedKritzBackground= true;
//            }

            timerUberHandler.postDelayed(this, kritzMilliDelay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar= (Toolbar) findViewById(R.id.main_bar);
        setSupportActionBar(toolbar);


        if(savedInstanceState == null){

            uber= 0;
            kritz= 0;
            Log.w("UberTrack","savedInstanceState equals null, starting from scratch");

        } else {

            uber= savedInstanceState.getInt(UBER);
            kritz= savedInstanceState.getInt(KRITZ);
            Log.w("UberTrack", "saveInstanceState =/= null");

        }

        //initialize layout to respond as reset button
        LinearLayout popButton = (LinearLayout) findViewById(R.id.main_linear_layout);

        //listen for reset
        popButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                uber = 0;
                kritz = 0;

//                viewUber.setBackgroundColor(0xFFFFFFFF);
//                viewKritz.setBackgroundColor(0xFFFFFFFF);
                
                updateUber();
                updateKritz();
            }
        });

        //initialize card view for resetting to max charge time
        CardView oneButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_Hardcore);

        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= UBERMILLIDELAY;
                kritzMilliDelay=KRITZMILLIDELAY;
            }
        });

        //initialize card view for going to 1.25 time
        CardView onePointTwoFiveButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_UGC_Steel);

        onePointTwoFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= (int) (UBERMILLIDELAY * UGCSTEELMULTIPLIER);
                kritzMilliDelay= (int) (KRITZMILLIDELAY * UGCSTEELMULTIPLIER);

            }
        });

        //initialize card view for going 1.5 time
        CardView onePointFiveButton = (CardView) findViewById(R.id.Card_View_Uber_Multiplier_Babbies);

        onePointFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uberMilliDelay= (int) (UBERMILLIDELAY * BABBYMULTIPLIER);
                kritzMilliDelay= (int) (KRITZMILLIDELAY * BABBYMULTIPLIER);

            }
        });

        //set Currents to their representative text views for updating
        uberCurrent= (TextView) findViewById(R.id.uberPercentTextView);
        kritzCurrent= (TextView) findViewById(R.id.kritzPercentTextView);

        //run runnables with no delay to start
        timerUberHandler.postDelayed(timerUberRunnable, 0);
        timerKritzHandler.postDelayed(timerKritzRunnable, 0);

    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.w("UberTrack","in onResume");

    }

    @Override
    protected void onPause(){
        super.onPause();

//        timerUberHandler.removeCallbacks(timerUberRunnable);
//
//        timerKritzHandler.removeCallbacks(timerKritzRunnable);

        Log.w("UberTrack","in onPause");

    }

    @Override
    protected void onStop(){
        super.onStop();

        Log.w("UberTrack","in onStop");

    }

    /**
     * update Kritz text view
     */
    private void updateKritz() {
        kritzCurrent.setText(kritz + "%");

    }

    /**
     * update Uber text view
     */
    private void updateUber() {
        uberCurrent.setText(uber+ "%");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){

        super.onSaveInstanceState(outState);

        outState.putDouble(UBER, uber);
        outState.putDouble(KRITZ, kritz);

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

package bu.edu.ec500.sshealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import bu.edu.ec500.sshealthapp.ui.main.MainFragment;
import bu.edu.ec500.sshealthapp.ui.main.MainViewModel;
import bu.edu.ec500.sshealthapp.ui.main.myDialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.Button;
//import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnticipateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.DecoDrawEffect;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.Locale;

/**
 * Sample to create an animated arc based chart in the style used by Google Fit
 * <p/>
 * This is achieved using the open source DecoView library.
 *
 * @see <a href="https://github.com/bmarrdev/android-DecoView-charting">DecoView on GitHub</a>
 */
public class MainActivity extends AppCompatActivity implements myDialog.myDialogListener{

    private boolean isUIReady = true;

    /**
     * DecoView animated arc based chart
     */
    private DecoView mDecoView;

    private TextView[] textViews = new TextView[6];

    /**
     * Maximum value for each data series in the {@link DecoView}. This can be different for each
     * data series, in this example we are applying the same all data series
     */
    private float mSeriesMax = 100f;
    private Button setGoal;

    private int mBackIndex;
    private int mSeriesIndex;
    private float[] caloris = {0, 0, 0, 0, 0, 0};
    private float totalClo = 0;
    private int[] activityCloTextViewIds = {
            R.id.walking,
            R.id.standing,
            R.id.walkupstair,
            R.id.walkdownstair,
            R.id.laying,
            R.id.sitting };

    private SensorRecognizeImpl mSensorRecogImpl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        for (int i = 0; i < activityCloTextViewIds.length; ++i) {
            textViews[i] = (TextView) findViewById(activityCloTextViewIds[i]);
        }

        mDecoView = (DecoView) findViewById(R.id.dynamicArcView);

        mSensorRecogImpl = new SensorRecognizeImpl(new MainActivity.MyInitCallback(), new MainActivity.MyCallback());

        setGoal = (Button)findViewById(R.id.setGoal);
        setGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorRecogImpl.stop();
                openDiaglog();
            }
        });

        // Create required data series on the DecoView
        createBackSeries();
        createDataSeries();

        // Setup events to be fired on a schedule
        createInitialEvents();

        //mSensorRecogImpl.init(this);
    }

    public void openDiaglog(){
        myDialog Dialog = new myDialog();
        Dialog.show(getSupportFragmentManager(),"my Dialog");
    }

    @Override
    public void applyVal(float max){
        resetText();
        mSeriesMax = max;
        createBackSeries();
        createDataSeries();
        createInitialEvents();
    }

    @Override
    public void Resume() {
        mSensorRecogImpl.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorRecogImpl.isInitialized()) {
            mSensorRecogImpl.start();
        }
    }

    @Override
    protected void onPause() {
        if (mSensorRecogImpl.isInitialized()) {
            mSensorRecogImpl.stop();
        }
        super.onPause();
    }

    private void createBackSeries() {
        SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(seriesItem);
    }

    private void createDataSeries() {
        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFFF8800"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .build();

        final TextView textPercentage = (TextView) findViewById(R.id.textPercentage);
        final TextView textGoalClo = (TextView) findViewById(R.id.textRemaining);
        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                float percentFilled = ((currentPosition - seriesItem.getMinValue()) / (seriesItem.getMaxValue() - seriesItem.getMinValue()));
                float remainingClo = seriesItem.getMaxValue() - currentPosition;
                textPercentage.setText(String.format(Locale.ENGLISH, "%.0f%%", percentFilled * 100f));
                textGoalClo.setText(String.format(Locale.ENGLISH, "%.1f Cal to goal", remainingClo));
            }

            @Override
            public void onSeriesItemDisplayProgress(float v) {

            }
        });

        mSeriesIndex = mDecoView.addSeries(seriesItem);
    }

    private void createInitialEvents() {
        mDecoView.executeReset();

        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .setDuration(3000)
                .setDelay(100)
                .build());

        mDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_OUT)
                .setIndex(mSeriesIndex)
                .setDuration(2000)
                .setDelay(500)
                .build());

        mDecoView.addEvent(new DecoEvent.Builder(totalClo)
                .setIndex(mSeriesIndex)
                .setDelay(2500)
                .setListener(new DecoEvent.ExecuteEventListener() {
                    @Override
                    public void onEventStart(DecoEvent decoEvent) {

                    }

                    @Override
                    public void onEventEnd(DecoEvent decoEvent) {
                        isUIReady = true;
                    }
                })
                .build());

        mSensorRecogImpl.init(this);
    }

    private void resetText() {
        ((TextView) findViewById(R.id.walking)).setText("0 Cal");
        ((TextView) findViewById(R.id.standing)).setText("0 Cal");
        ((TextView) findViewById(R.id.walkdownstair)).setText("0 Cal");
        ((TextView) findViewById(R.id.walkupstair)).setText("0 Cal");
        ((TextView) findViewById(R.id.laying)).setText("0 Cal");
        ((TextView) findViewById(R.id.sitting)).setText("0 Cal");
        ((TextView) findViewById(R.id.textPercentage)).setText("");
        ((TextView) findViewById(R.id.textRemaining)).setText("");
    }

    private TextView getTextViewByActivityType(int type) {
        return textViews[type];
    }

    class MyInitCallback implements AppInitCallback {
        @Override
        public void onSucceeded() {
            Toast.makeText(getApplicationContext(), "initialization succeeded", Toast.LENGTH_LONG).show();
            mSensorRecogImpl.start();
        }

        @Override
        public void onFailed(AppResult error) {
            Toast.makeText(getApplicationContext(), "initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    class MyCallback implements AppResultCallback<RecognizedActivityResult> {
        @Override
        public void onResult(@NonNull RecognizedActivityResult results) {
            final RecognizedActivity activity = results.getMostProbableActivity();
            caloris[activity.getActivityType()] += RecognizedActivity.getActivityCalorisByType(activity.getActivityType()) *
                    results.time;
            //updateCaloris();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI
                    textViews[activity.getActivityType()].setText(String.format(Locale.ENGLISH, "%.1f Cal",
                            caloris[activity.getActivityType()]));
                }
            });

            totalClo += RecognizedActivity.getActivityCalorisByType(activity.getActivityType()) *
                    results.time;
            if (isUIReady) {
                if (totalClo < mSeriesMax) {
                    mDecoView.addEvent(new DecoEvent.Builder(totalClo)
                            .setIndex(mSeriesIndex)
                            .setDelay(100)
                            .build());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setGoal.setVisibility(View.INVISIBLE);
                            mSensorRecogImpl.stop();
                            totalClo = 0;
                            for (int i = 0; i < caloris.length; ++i) {
                                caloris[i] = 0;
                            }
                            resetText();
                        }
                    });
                    mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                            .setIndex(mSeriesIndex)
                            .setDelay(100)
                            .setListener(new DecoEvent.ExecuteEventListener() {
                                @Override
                                public void onEventStart(DecoEvent decoEvent) {

                                }

                                @Override
                                public void onEventEnd(DecoEvent decoEvent) {

                                }
                            })
                            .build());

                    //isUIReady = false;
                    mDecoView.addEvent(new DecoEvent.Builder(DecoDrawEffect.EffectType.EFFECT_SPIRAL_EXPLODE)
                            .setIndex(mSeriesIndex)
                            .setDelay(3000)
                            .setDuration(3000)
                            .setDisplayText("GOAL!")
                            .setListener(new DecoEvent.ExecuteEventListener() {
                                @Override
                                public void onEventStart(DecoEvent decoEvent) {

                                }

                                @Override
                                public void onEventEnd(DecoEvent decoEvent) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            createInitialEvents();
                                            setGoal.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            })
                            .build());
                }
            }
        }
    }

    private void updateCaloris() {
        for (int i = 0; i < caloris.length; ++i) {
            textViews[i].setText(String.format(Locale.ENGLISH, "%.1f Cal", caloris[i]));
        }
    }
}

package co.filamentlabs.antdroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import co.filamentlabs.antdroid.ants.Ant;
import co.filamentlabs.antdroid.ants.AntGreedy;
import co.filamentlabs.antdroid.ants.AntInterview;
import co.filamentlabs.antdroid.ants.AntMap;
import co.filamentlabs.antdroid.ants.AntNone;
import co.filamentlabs.antdroid.ants.AntRandom;
import co.filamentlabs.antdroid.ants.AntSmart;
import co.filamentlabs.antdroid.utils.PointSetReader;
import co.filamentlabs.antdroid.view.NodeMap;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


public class AntsActivity extends ActionBarActivity {

    private static final String TAG = "AntsActivity";

    private NodeMap mNodeMap;
    private TextView mDistanceTv;
    private ImageView mMapIv;

    private AntMap mAntMap;
    private PointSetReader.TSPData mTSPData;

    public enum AntType {
        NONE,
        RANDOM,
        GREEDY,
        SMART,
        INTERVIEW
    }

    public enum Map {
        WEST_SAHARA,
        QATAR
    }

    private Map mSelectedMap = Map.WEST_SAHARA;
    private AntType mAntType = AntType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ants);

        mNodeMap = (NodeMap) findViewById(R.id.node_map);
        mDistanceTv = (TextView) findViewById(R.id.distance_tv);
        mMapIv = (ImageView) findViewById(R.id.map_img);

//        mTSPData = PointSetReader.readTSPDataFile(this, "qa194.tsp.txt");
//        mTSPData = PointSetReader.readTSPDataFile(this, "wi29.tsp.txt");
//        Log.d(TAG, "TSPDATA:" + mTSPData.toString());
//        mNodeMap.setPoints(mTSPData.getPoints());

    }

    private void setMap(Map map) {
        actionClear();
        mSelectedMap = map;
        switch (map) {
            case WEST_SAHARA: {
                mTSPData = PointSetReader.readTSPDataFile(this, "wi29.tsp.txt");
                mMapIv.setImageResource(R.drawable.wimap);
                break;
            }
            case QATAR: {
                mTSPData = PointSetReader.readTSPDataFile(this, "qa194.tsp.txt");
                mMapIv.setImageResource(R.drawable.qamap);
                break;
            }
        }
        mNodeMap.setPoints(mTSPData.getPoints());
    }

    private void actionShowMap() {
        mMapIv.setVisibility(mMapIv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void actionStop() {
        if (mAntSub != null && !mAntSub.isUnsubscribed()) {
            mAntSub.unsubscribe();
        }
    }

    private void actionClear() {
        actionStop();
        mNodeMap.setPath(new ArrayList<Pair<Double, Double>>(), Color.RED);
    }

    private void actionStart() {
        runTheAnts(250, mTSPData.getDimension());
    }

    @Override
    public void onResume() {
        super.onResume();
        setMap(mSelectedMap);
        setAntType(mAntType);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mAntSub != null && !mAntSub.isUnsubscribed()) {
            mAntSub.unsubscribe();
        }
    }

    private void logAnt(Ant ant) {
        StringBuilder builder = new StringBuilder();
        builder.append("Ant path:");
        for (Integer integer : ant.getPath()) {
            builder.append("\n" + integer);
        }
        Log.d(TAG, builder.toString());
    }


    private Subscription mAntSub;
    private Ant mBestAnt;
    private int mPasses;
    private int mCalls;
    private int mBestAntPass;
    private int mImprovements;


    private void runTheAnts(final int totalPasses, final int antsPerPass) {
        if (mAntSub != null && !mAntSub.isUnsubscribed()) {
            mAntSub.unsubscribe();
        }

        mNodeMap.setPath(new ArrayList<Pair<Double, Double>>(), Color.GREEN);
        mBestAnt = null;
        mBestAntPass = 0;
        mPasses = 0;
        mCalls = 0;
        mImprovements = 0;
        mAntMap = new AntMap(mTSPData);

        Observable<Ant> antPassObservable = getAntObservable(mAntMap, mPasses);
        Observable<Ant> bestAntForPass = antPassObservable.repeat(antsPerPass)
                .reduce(new Func2<Ant, Ant, Ant>() {
                    @Override
                    public Ant call(Ant ant, Ant ant2) {
                        if (ant.getDistance() < ant2.getDistance()) {
                            return ant;
                        }
                        return ant2;
                    }
                }).map(new Func1<Ant, Ant>() {
                    @Override
                    public Ant call(Ant ant) {
                        //This way we are on the computation thread
                        mAntMap.updateTrail(ant);
                        mAntMap.decay(ant.generateDecayFunction());
                        return ant;
                    }
                });

        mAntSub = bestAntForPass.repeat(totalPasses)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Ant>() {
                    @Override
                    public void call(Ant ant) {
                        mPasses++;
                        if (mBestAnt == null || mBestAnt.getDistance() > ant.getDistance()) {
                            mBestAntPass = mPasses;
                            mImprovements++;
                            mBestAnt = ant;
                            Log.d(TAG, "NEW BEST:" + mBestAnt.getDistance());
                        }
                        mNodeMap.addPath(mAntMap.getPathForPoints(mBestAnt.getPath()), Color.GREEN);
                        mDistanceTv.setText("Best:" + mBestAnt.getDistance() + " Pass:" + mPasses + " Best Pass:" + mBestAntPass + " Calculations:" + (mPasses * mCalls) + " Improvements:" + mImprovements);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mDistanceTv.setText("Exception");
                        Log.e(TAG, "fail", throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mDistanceTv.setText("Done! Best:" + mBestAnt.getDistance() + " Best Pass:" + mBestAntPass + " Passes:" + mPasses + " Calculations:" + (mPasses * mCalls) + " Improvements:" + mImprovements);
                    }
                });
    }


    public Observable<Ant> getAntObservable(final AntMap map, final int passes) {
        return Observable.create(new Observable.OnSubscribe<Ant>() {
            @Override
            public void call(Subscriber<? super Ant> subscriber) {
                mCalls++;
                Ant ant = null;
                switch (mAntType) {
                    case NONE: {
                        ant = new AntNone();
                        break;
                    }
                    case RANDOM: {
                        ant = new AntRandom();
                        break;
                    }
                    case GREEDY: {
                        ant = new AntGreedy();
                        break;
                    }
                    case SMART: {
                        ant = new AntSmart(mAntMap.getMaxEdgeDistance(), mAntMap.getMinEdgeDistance());
                        break;
                    }
                    case INTERVIEW: {
                        ant = new AntInterview(mAntMap.getMaxEdgeDistance(), mAntMap.getMinEdgeDistance());
                        break;
                    }
                }
                ant.walkPath(map, 0, passes);
                subscriber.onNext(ant);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ants, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_map: {
                actionShowMap();
                return true;
            }
            case R.id.action_clear: {
                actionClear();
                return true;
            }
            case R.id.action_stop: {
                actionStop();
                return true;
            }
            case R.id.action_start: {
                actionStart();
                return true;
            }
            case R.id.ant_none: {
                actionClear();
                setAntType(AntType.NONE);
                return true;
            }
            case R.id.ant_random: {
                actionClear();
                setAntType(AntType.RANDOM);
                return true;
            }
            case R.id.ant_greedy: {
                actionClear();
                setAntType(AntType.GREEDY);
                return true;
            }
            case R.id.ant_smart: {
                actionClear();
                setAntType(AntType.SMART);
                return true;
            }
            case R.id.ant_interview: {
                actionClear();
                setAntType(AntType.INTERVIEW);
                return true;
            }
            case R.id.map_western_sahara: {
                actionClear();
                setMap(Map.WEST_SAHARA);
                return true;
            }
            case R.id.map_qatar: {
                actionClear();
                setMap(Map.QATAR);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setAntType(AntType antType) {
        mAntType = antType;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(mAntType.name());
        }
    }
}

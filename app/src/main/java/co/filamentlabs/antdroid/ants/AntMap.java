package co.filamentlabs.antdroid.ants;

import android.util.Log;
import android.util.Pair;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import co.filamentlabs.antdroid.utils.PointSetReader;
import rx.functions.Func1;

/**
 * Created by jdrotos on 5/20/15.
 */
public class AntMap {

    private static final String TAG = "AntMap";

    private PointSetReader.TSPData mData;
    double[][] mPoints;

    double[][] mEdgeDistances;
    double[][] mEdgeValues;

    double mMaxEdgeDistance;
    double mMinEdgeDistance;

    public AntMap(PointSetReader.TSPData data) {
        mData = data;

        mPoints = new double[mData.getDimension()][2];
        for (int i = 0; i < mData.getPoints().size(); i++) {
            mPoints[i][0] = mData.getPoints().get(i).first;
            mPoints[i][1] = mData.getPoints().get(i).second;
        }

        mEdgeDistances = new double[mData.getDimension()][mData.getDimension()];
        mMaxEdgeDistance = -1;
        mMinEdgeDistance = Double.MAX_VALUE;
        //Distance = sqrt((x2 - x1)^2 + (y2 - y1)^2)
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints.length; j++) {
                mEdgeDistances[i][j] = Math.sqrt(Math.pow((mPoints[j][0] - mPoints[i][0]), 2) + Math
                        .pow((mPoints[j][1] - mPoints[i][1]), 2));

                if (mEdgeDistances[i][j] > mMaxEdgeDistance) {
                    mMaxEdgeDistance = mEdgeDistances[i][j];
                }
                if (mEdgeDistances[i][j] < mMinEdgeDistance) {
                    mMinEdgeDistance = mEdgeDistances[i][j];
                }
            }
        }

        mEdgeValues = new double[mData.getDimension()][mData.getDimension()];
        for (int i = 0; i < mEdgeValues.length; i++) {
            for (int j = 0; j < mEdgeValues.length; j++) {
                mEdgeValues[i][j] = 1;
            }
        }
    }

    public List<Pair<Double, Double>> getPathForPoints(List<Integer> points) {
        List<Pair<Double, Double>> pathPoints = new ArrayList<>();
        for (Integer integer : points) {
            pathPoints.add(getPoint(integer));
        }
        return pathPoints;
    }

    public Pair<Double, Double> getPoint(int pointIndex) {
        return new Pair<>(mPoints[pointIndex][0], mPoints[pointIndex][1]);
    }

    public void updateEdgeValue(int pointIndexOne, int pointIndexTwo, double value) {
        mEdgeValues[pointIndexOne][pointIndexTwo] = value;
    }

    public double getEdgeValue(int pointIndexOne, int pointIndexTwo) {
        return mEdgeValues[pointIndexOne][pointIndexTwo];
    }

    public double getDistance(int pointIndexOne, int pointIndexTwo) {
        return mEdgeDistances[pointIndexOne][pointIndexTwo];
    }

    public void updateTrail(Ant ant) {
        for (int i = 1; i < ant.getPath().size(); i++) {
            updateEdgeValue(ant.getPath().get(i - 1), ant.getPath()
                    .get(i), ant.updateEdgeValuesOnPath(getEdgeValue(ant.getPath()
                    .get(i - 1), ant.getPath()
                    .get(i))));
        }
    }

    public void decay(Func1<Double, Double> decayFunction) {
        for (int i = 0; i < mEdgeValues.length; i++) {
            for (int j = 0; j < mEdgeValues.length; j++) {
                mEdgeValues[i][j] = decayFunction.call(mEdgeValues[i][j]);
            }
        }
    }

    public double getMaxEdgeDistance() {
        return mMaxEdgeDistance;
    }

    public double getMinEdgeDistance() {
        return mMinEdgeDistance;
    }

    public double getMaxEdgeValue() {
        double max = -1;
        for (int i = 0; i < mEdgeValues.length; i++) {
            for (int j = 0; j < mEdgeValues.length; j++) {
                if (mEdgeValues[i][j] > max) {
                    max = mEdgeValues[i][j];
                }
            }
        }
        return max;
    }

    public void logMap() {
        StringBuilder builder = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.00");
        for (int i = 0; i < mEdgeValues.length; i++) {
            for (int j = 0; j < mEdgeValues.length; j++) {
                builder.append(df.format(mEdgeValues[i][j]));
                builder.append(",");
            }
            builder.append("\n");
        }
        Log.d(TAG, builder.toString());
    }
}

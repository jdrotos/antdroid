package co.filamentlabs.antdroid.ants;

import java.util.Random;

import rx.functions.Func1;

/**
 * Created by jdrotos on 5/20/15.
 */
public class AntSmart extends Ant {

    private static final int MAX_EDGE = 10;

    private Random mRandom = new Random();

    private double mMaxEdgeDist;
    private double mMinEdgeDist;
    private double mEdgeDistDiff;

    public AntSmart(double maxEdgeDist, double minEdgeDist) {
        mMaxEdgeDist = maxEdgeDist;
        mMinEdgeDist = minEdgeDist;
        mEdgeDistDiff = maxEdgeDist - minEdgeDist;
    }

    @Override
    public double scoreEdgeCandidate(double distance, double edgeValue, int passNumber) {
        double distanceRatio = mMaxEdgeDist / distance;
        double multiplier = edgeValue;

//        if (edgeValue > MAX_EDGE / 2f) {
//            if (mRandom.nextFloat() > Math.min(edgeValue/MAX_EDGE, 0.75f)) {
//                multiplier = Math.max(edgeValue / 2f, edgeValue * mRandom.nextDouble());
//            }
//        } else {
//            if (mRandom.nextFloat() > Math.max(0.25f,edgeValue/MAX_EDGE)) {
//                multiplier = mRandom.nextDouble() * MAX_EDGE;
//            }
//        }
        if (mRandom.nextFloat() > 0.9f) {
            multiplier = edgeValue + (mRandom.nextDouble() * (MAX_EDGE - edgeValue));
        }

//        float randomnessThreshold = 0.8f;
//        if(edgeValue > MAX_EDGE/2f){
//            randomnessThreshold = 0.6f;
//        }
//
//        if (mRandom.nextFloat() > randomnessThreshold) {
//            multiplier = Math.max(1, mRandom.nextDouble() * (MAX_EDGE - (edgeValue/3)) / distanceRatio);
//            //multiplier = Math.max(1, mRandom.nextDouble() * MAX_EDGE);
//        }

        return distanceRatio * multiplier;
    }

    @Override
    public double updateEdgeValuesOnPath(double oldValue) {
        //return Math.min(Math.max(oldValue * 1.5, 3), MAX_EDGE);
        //return oldValue + 1;
        return oldValue + (MAX_EDGE - oldValue) / 2;
    }

    @Override
    public Func1<Double, Double> generateDecayFunction() {
        return new Func1<Double, Double>() {
            @Override
            public Double call(Double aDouble) {
                return Math.min(Math.max(1, aDouble - (aDouble * 0.1)), MAX_EDGE);
            }
        };
    }
}

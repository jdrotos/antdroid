package co.filamentlabs.antdroid.ants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by jdrotos on 5/20/15.
 */
public abstract class Ant {

    private static final String TAG = "ANT";

    public List<Integer> mPath;
    public HashSet<Integer> mUsedNodes;
    public List<Integer> mRemainingNodes;

    public double mDistance;
    public int mRemainingEdges;
    public int mTotalEdges;

    public void walkPath(AntMap map, int startNode, int passNumber) {
        mPath = new ArrayList<>();
        mUsedNodes = new HashSet<>();
        mRemainingNodes = new ArrayList<>();

        mTotalEdges = map.mPoints.length;
        mRemainingEdges = map.mPoints.length;
        mDistance = 0;

        mPath.add(startNode);
        mUsedNodes.add(startNode);
        for (int i = 0; i < mTotalEdges; i++) {
            mRemainingNodes.add(i);
        }

        int lastNode = startNode;
        for (int i = 0; i < mTotalEdges; i++) {

            double bestScore = -1;
            Integer bestScoreIndex = -1;
            int bestScoreK = -1;
            for (int k = 0; k < mRemainingNodes.size(); k++) {
                //Log.d(TAG, "remainingNodes j:" + j);
                int j = mRemainingNodes.get(k);
                //Log.d(TAG, "k:" + k + " j:" + j);
                double score = scoreEdgeCandidate(map.getDistance(lastNode, j), map.getEdgeValue(lastNode, j), passNumber);
                if (score > bestScore) {
                    bestScore = score;
                    bestScoreIndex = j;
                    bestScoreK = k;
                }
            }

            mPath.add(bestScoreIndex);
            mUsedNodes.add(bestScoreIndex);
            mRemainingNodes.remove(bestScoreK);
            mDistance += map.getDistance(lastNode, bestScoreIndex);
            mRemainingEdges--;

            lastNode = bestScoreIndex;
        }
        mDistance += map.getDistance(lastNode, startNode);
    }

    public List<Integer> getPath() {
        return mPath;
    }

    public double getDistance() {
        return mDistance;
    }

    public abstract double scoreEdgeCandidate(double distance, double edgeValue, int passNumber);

    public abstract double updateEdgeValuesOnPath(double oldValue);

    public Func1<Double,Double> generateDecayFunction(){
        return new Func1<Double, Double>() {
            @Override
            public Double call(Double aDouble) {
                return aDouble;
            }
        };
    }


}

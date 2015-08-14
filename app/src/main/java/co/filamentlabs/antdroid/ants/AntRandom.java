package co.filamentlabs.antdroid.ants;

import java.util.Random;

/**
 * Created by jdrotos on 5/20/15.
 */
public class AntRandom extends Ant {
    private Random mRandom = new Random();

    @Override
    public double scoreEdgeCandidate(double distance, double edgeValue, int passNumber) {
        return mRandom.nextDouble();
    }

    @Override
    public double updateEdgeValuesOnPath(double oldValue) {
        return oldValue;
    }
}

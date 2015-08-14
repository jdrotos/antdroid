package co.filamentlabs.antdroid.ants;

/**
 * Created by jdrotos on 5/20/15.
 */
public class AntGreedy extends Ant {

    @Override
    public double scoreEdgeCandidate(double distance, double edgeValue, int passNumber) {
        return 1/distance;
    }

    @Override
    public double updateEdgeValuesOnPath(double oldValue) {
        return oldValue;
    }
}

package co.filamentlabs.antdroid.ants;

/**
 * Created by jdrotos on 5/20/15.
 */
public class AntNone extends Ant {

    @Override
    public double scoreEdgeCandidate(double distance, double edgeValue, int passNumber) {
        return 1;
    }

    @Override
    public double updateEdgeValuesOnPath(double oldValue) {
        return oldValue;
    }
}

package co.filamentlabs.antdroid.ants;

import java.util.Random;

/**
 * Created by jdrotos on 5/21/15.
 */
public class AntInterview extends Ant {

    private double mMaxEdgeDist;
    private double mMinEdgeDist;
    private Random mRandom = new Random();

    public AntInterview(double maxEdgeDist, double minEdgeDist) {
        mMaxEdgeDist = maxEdgeDist;
        mMinEdgeDist = minEdgeDist;
    }


    /**
     * This function determines which node the ant will travel to next.
     * Each node reamaining in the circuit will result in a call to this function.
     * <p/>
     * The node that scores the highest will be chosen.
     * <p/>
     * Higher value == better choice.
     *
     * @param distance   - the distance to the node in question
     * @param edgeValue  - 1 + n (where n is the number of winner ants that have taken this edge)
     * @param passNumber - the pass
     * @return
     */
    @Override
    public double scoreEdgeCandidate(double distance, double edgeValue, int passNumber) {
        if (passNumber == 1) {
            return 1 / distance;
        }
        if (mRandom.nextBoolean()) {
            return edgeValue * (mRandom.nextDouble() * 20) / distance;
        } else {
            return (1 / distance) * edgeValue;
        }

//        if (passNumber == 1) {
//            return 1 / distance;
//        }else{
//            double updatedEdge = edgeValue % 50;
//            return updatedEdge * 1/distance * mRandom.nextDouble();
//        }
    }

    /**
     * As the fastest ant of the pass, you get to update the pharamone trail.
     *
     * @param oldValue - the value on the pharamone trail currently
     * @return - the new value to store as the edgeValue
     */
    @Override
    public double updateEdgeValuesOnPath(double oldValue) {
        return oldValue + 1;//mRandom.nextDouble();
    }
}

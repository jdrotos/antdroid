package co.filamentlabs.antdroid.utils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jdrotos on 5/20/15.
 */
public class PointSetReader {

    /**
     * NAME : qa194
     * COMMENT : 194 locations in Qatar
     * COMMENT : Derived from National Imagery and Mapping Agency data
     * TYPE : TSP
     * DIMENSION : 194
     * EDGE_WEIGHT_TYPE : EUC_2D
     * NODE_COORD_SECTION
     * 1 24748.3333 50840.0000
     * 2 24758.8889 51211.9444
     * 3 24827.2222 51394.7222
     * 4 24904.4444 51175.0000
     * ....
     * EOF
     */


    private static final String PREFIX_NAME = "NAME";
    private static final String PREFIX_COMMENT = "COMMENT";
    private static final String PREFIX_TYPE = "TYPE";
    private static final String PREFIX_DIMENSION = "DIMENSION";
    private static final String PREFIX_EDGE_WEIGHT_TYPE = "EDGE_WEIGHT_TYPE";
    private static final String DATA_START = "NODE_COORD_SECTION";
    private static final String DATA_END = "EOF";

    public static TSPData readTSPDataFile(Context context, String fileName) {

        TSPData data = new TSPData();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

            boolean dataSectionReached = false;
            String line;

            while ((line = r.readLine()) != null) {
                if (dataSectionReached) {
                    if (line.trim().toUpperCase().startsWith(DATA_END)) {
                        break;
                    } else {
                        try {
                            String[] parts = line.split("\\s+");
                            Log.d("readTASPDataFile", "parts:" + parts.length);
                            data.getPoints()
                                    .add(new Pair<Double, Double>(Double.parseDouble(parts[1]), Double
                                            .parseDouble(parts[2])));
                        } catch (Exception ex) {
                            Log.d("readTASPDataFile", "parse data row fail", ex);
                        }
                    }
                } else {
                    if (line.trim().toUpperCase().startsWith(DATA_START)) {
                        dataSectionReached = true;
                    } else if (line.trim().toUpperCase().startsWith(DATA_END)) {
                        break;
                    } else if (line.trim().toUpperCase().startsWith(PREFIX_NAME)) {
                        data.setName(line.substring(line.indexOf(":") + 1).trim());
                    } else if (line.trim().toUpperCase().startsWith(PREFIX_COMMENT)) {
                        data.getComments().add(line.substring(line.indexOf(":") + 1).trim());
                    } else if (line.trim().toUpperCase().startsWith(PREFIX_TYPE)) {
                        data.setType(line.substring(line.indexOf(":") + 1).trim());
                    } else if (line.trim().toUpperCase().startsWith(PREFIX_DIMENSION)) {
                        data.setDimension(Integer.parseInt(line.substring(line.indexOf(":") + 1)
                                .trim()));
                    } else if (line.trim().toUpperCase().startsWith(PREFIX_EDGE_WEIGHT_TYPE)) {
                        data.setEdgeWeightType(line.substring(line.indexOf(":") + 1).trim());
                    }
                }
            }
        } catch (Exception ex) {
            Log.d("readTASPDataFile", "parse data file fail", ex);
        }
        return data;
    }


    public static class TSPData {

        private String mName;
        private List<String> mComments = new ArrayList<>();
        private String mType;
        private int mDimension;
        private String mEdgeWeightType;
        private List<Pair<Double, Double>> mPoints = new ArrayList<>();

        public TSPData() {

        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public List<String> getComments() {
            return mComments;
        }

        public void setComments(List<String> comments) {
            mComments = comments;
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            mType = type;
        }

        public int getDimension() {
            return mDimension;
        }

        public void setDimension(int dimension) {
            mDimension = dimension;
        }

        public String getEdgeWeightType() {
            return mEdgeWeightType;
        }

        public void setEdgeWeightType(String edgeWeightType) {
            mEdgeWeightType = edgeWeightType;
        }

        public List<Pair<Double, Double>> getPoints() {
            return mPoints;
        }

        public void setPoints(List<Pair<Double, Double>> points) {
            mPoints = points;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TSPData:");
            builder.append("\nName:" + mName);
            builder.append("\nSize:" + mDimension);
            if (mPoints == null || mPoints.size() <= 0) {
                builder.append("\n");
                builder.append("No Points");
            } else {
                builder.append("\n");
                builder.append("" + mPoints.size() + " points:");
                for (Pair<Double, Double> point : mPoints) {
                    builder.append("\n");
                    builder.append("(" + point.first + "," + point.second + ")");
                }
            }
            return builder.toString();
        }
    }


}

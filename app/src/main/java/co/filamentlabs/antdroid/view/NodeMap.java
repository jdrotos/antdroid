package co.filamentlabs.antdroid.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jdrotos on 5/20/15.
 */
public class NodeMap extends View {

    private static final String TAG = "NodeMap";

    private static final int POINT_COLOR = Color.BLACK;
    private static final int PATH_COLOR = Color.RED;


    public NodeMap(Context context) {
        super(context);
        init(context, null);
    }

    public NodeMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NodeMap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NodeMap(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private Paint mPointPaint;
    private Paint mPathPaint;
    private List<Pair<Double, Double>> mPoints;
    //private List<Pair<Double, Double>> mPath;
    private List<Pair<List<Pair<Double, Double>>, Integer>> mPaths = new ArrayList<>();
    private int circlRadius = 5;

    public void setPoints(List<Pair<Double, Double>> points) {
        mPoints = points;
        invalidate();
    }

    public void setPath(List<Pair<Double, Double>> path, int color) {
        mPaths.clear();
        addPath(path, color);
    }


    public void addPath(List<Pair<Double, Double>> path, int color) {
        mPaths.add(new Pair<List<Pair<Double, Double>>, Integer>(path, color));
        if(mPaths.size() > 3){
            mPaths.remove(0);
        }
        invalidate();
    }

    private int decayColor(int color, float alphaMult) {
        return Color.argb((int) (alphaMult * Color.alpha(color)), Color.red(color), Color.green(color), Color
                .blue(color));
    }

    private void init(Context context, AttributeSet attrs) {
        mPointPaint = new Paint();
        mPointPaint.setColor(POINT_COLOR);
        mPointPaint.setStrokeWidth(24);
        mPointPaint.setStyle(Paint.Style.FILL);

        mPathPaint = new Paint();
        mPathPaint.setColor(PATH_COLOR);
        mPathPaint.setStrokeWidth(10);
        mPathPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPoints != null && getWidth() > 0 && getHeight() > 0) {
            int w = getWidth() - (circlRadius * 3);
            int h = getHeight() - (circlRadius * 3);

            //Draw paths first
            int counter = 0;
            for (Pair<List<Pair<Double, Double>>, Integer> pathAndColor : mPaths) {

            //Pair<List<Pair<Double, Double>>, Integer> pathAndColor = mPaths.get(i);
                List<Pair<Double, Double>> pathPoints = pathAndColor.first;
                if(counter == mPaths.size() - 1){
                    mPathPaint.setStrokeWidth(12);
                    mPathPaint.setColor(Color.RED);
                }
                else{
                    mPathPaint.setStrokeWidth(3);
                    mPathPaint.setColor(decayColor(pathAndColor.second, (counter / (float) mPaths.size())));
                }
                if (pathPoints != null && pathPoints.size() > 0) {
                    List<Pair<Float, Float>> localPathPoints = generateLocalPoints(pathPoints, w, h);
                    Path path = new Path();
                    path.moveTo(localPathPoints.get(0).first, localPathPoints.get(0).second);
                    for (int i = 1; i < localPathPoints.size(); i++) {
                        path.lineTo(localPathPoints.get(i).first, localPathPoints.get(i).second);
                    }
                    path.lineTo(localPathPoints.get(0).first, localPathPoints.get(0).second);
                    canvas.drawPath(path, mPathPaint);
                }
                counter++;
            }

            //Draw points
            List<Pair<Float, Float>> localPoints = generateLocalPoints(mPoints, w, h);
            for (Pair<Float, Float> point : localPoints) {
                //Log.d(TAG, "onDraw point:" + point.first + "," + point.second);
                canvas.drawCircle(point.first, point.second, circlRadius, mPointPaint);
            }
        }
    }

    private List<Pair<Float, Float>> generateLocalPoints(List<Pair<Double, Double>> points, int width, int height) {
        //Log.d(TAG, "generateLocalPoints points:" + points.size() + " w:" + width + " h:" + height);
        Pair<Pair<Double, Double>, Pair<Double, Double>> extremes = findExtremes(points);
        float xRatio = (float) (((4 * width) / 5f) / (extremes.second.first - extremes.first.first));
        float yRatio = (float) (((4 * height) / 5f) / (extremes.second.second - extremes.first.second));
        float minRatio = Math.min(xRatio, yRatio);
        //Log.d(TAG, "generateLocalPoints xRatio:" + xRatio + " yRatio:" + yRatio);


        List<Pair<Float, Float>> localPoints = new ArrayList<>();
        for (Pair<Double, Double> point : points) {
            //localPoints.add(new Pair<Float, Float>((float) ((point.first - extremes.first.first) * xRatio), (float) ((point.second - extremes.first.second) * yRatio)));
            //localPoints.add(new Pair<Float, Float>((float) ((point.first - extremes.first.first) * minRatio), (float) ((point.second - extremes.first.second) * minRatio)));
            localPoints.add(new Pair<Float, Float>((float) (width - (point.second - extremes.first.second) * xRatio), (float) (height - (point.first - extremes.first.first) * yRatio)));
        }

        return localPoints;
    }

    private Pair<Pair<Double, Double>, Pair<Double, Double>> findExtremes(List<Pair<Double, Double>> points) {
        double top = Integer.MAX_VALUE;
        double left = Integer.MAX_VALUE;
        double bottom = 0;
        double right = 0;

        for (Pair<Double, Double> point : points) {
            if (point.first < left) {
                left = point.first;
            }
            if (point.first > right) {
                right = point.first;
            }
            if (point.second < top) {
                top = point.second;
            }
            if (point.second > bottom) {
                bottom = point.second;
            }
        }

        //Log.d(TAG, "findExtremes returning (x,y):(" + left + "," + top + ")(" + right + "," + bottom + ")");
        return new Pair<>(new Pair<Double, Double>(left, top), new Pair<Double, Double>(right, bottom));
    }


}

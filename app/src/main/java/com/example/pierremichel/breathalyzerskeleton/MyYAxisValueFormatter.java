package com.example.pierremichel.breathalyzerskeleton;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

/**
 * Created by Micha on 2017-03-29.
 */

public class MyYAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<String> mValues;

    public MyYAxisValueFormatter(ArrayList<String> values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mValues.get((int) value);
    }

}

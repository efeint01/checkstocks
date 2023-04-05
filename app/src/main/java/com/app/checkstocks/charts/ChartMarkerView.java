package com.app.checkstocks.charts;

import android.content.Context;

import com.app.checkstocks.databinding.CustomMarkerViewLayoutBinding;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class ChartMarkerView extends MarkerView {
    private final CustomMarkerViewLayoutBinding binding;

    public ChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        binding = CustomMarkerViewLayoutBinding.bind(this);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        binding.tvContent.setText(String.format("%s$", e.getY()));
        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            mOffset = new MPPointF(-(binding.getRoot().getWidth() / 2), -binding.getRoot().getHeight());
        }
        return mOffset;
    }
}

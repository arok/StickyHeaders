package com.brandongogetap.stickyheaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.brandongogetap.stickyheaders.ViewRetriever.RecyclerViewRetriever;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StickyLinearLayoutManager extends LinearLayoutManager {

    private StickyHeaderPositioner positioner;
    private StickyHeaderHandler headerHandler;
    private List<Integer> headerPositions;
    private RecyclerViewRetriever viewRetriever;
    private RecyclerView recyclerView;
    private int headerElevation = StickyHeaderPositioner.NO_ELEVATION;
    @Nullable private StickyHeaderListener listener;

    public StickyLinearLayoutManager(Context context, StickyHeaderHandler headerHandler) {
        this(context, VERTICAL, false, headerHandler);
        init(headerHandler);
    }

    public StickyLinearLayoutManager(Context context, int orientation, boolean reverseLayout, StickyHeaderHandler headerHandler) {
        super(context, orientation, reverseLayout);
        init(headerHandler);
    }

    private void init(StickyHeaderHandler stickyHeaderHandler) {
        setStickyHeaderHandler(stickyHeaderHandler);
    }

    /**
     * Enable or disable elevation for Sticky Headers.
     * <p>
     * If you want to specify a specific amount of elevation, use
     * {@link StickyLinearLayoutManager#elevateHeaders(int)}
     *
     * @param elevateHeaders Enable Sticky Header elevation. Default is false.
     */
    public void elevateHeaders(boolean elevateHeaders) {
        this.headerElevation = elevateHeaders ?
                StickyHeaderPositioner.DEFAULT_ELEVATION : StickyHeaderPositioner.NO_ELEVATION;
        elevateHeaders(headerElevation);
    }

    /**
     * Enable Sticky Header elevation with a specific amount.
     *
     * @param dp elevation in dp
     */
    public void elevateHeaders(int dp) {
        this.headerElevation = dp;
        if (positioner != null) {
            positioner.setElevateHeaders(dp);
        }
    }

    private void setStickyHeaderHandler(StickyHeaderHandler headerHandler) {
        Preconditions.checkNotNull(headerHandler, "StickyHeaderHandler == null");
        this.headerHandler = headerHandler;
        headerPositions = new ArrayList<>();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        cacheHeaderPositions();
        positioner.reset(getOrientation(), findFirstVisibleItemPosition());
        positioner.updateHeaderState(
                findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever);
    }

    private void cacheHeaderPositions() {
        headerPositions.clear();

        int itemCount = headerHandler.getItemCount();
        if (itemCount == 0) {
            positioner.setHeaderPositions(headerPositions);
            return;
        }

        for (int i = 0; i < itemCount; i++) {
            if (headerHandler.isHeader(i)) {
                headerPositions.add(i);
            }
        }
        positioner.setHeaderPositions(headerPositions);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        if (Math.abs(scroll) > 0) {
            positioner.updateHeaderState(
                    findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever);
        }
        return scroll;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollHorizontallyBy(dx, recycler, state);
        if (Math.abs(scroll) > 0) {
            positioner.updateHeaderState(
                    findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever);
        }
        return scroll;
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int dataPosition = getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                visibleHeaders.put(dataPosition, view);
            }
        }
        return visibleHeaders;
    }

    /**
     * Register a callback to be invoked when a header is attached/re-bound or detached.
     *
     * @param listener The callback that will be invoked, or null to unset.
     */
    public void setStickyHeaderListener(@Nullable StickyHeaderListener listener) {
        this.listener = listener;
        if (positioner != null) {
            positioner.setListener(listener);
        }
    }

    @Override public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        super.removeAndRecycleAllViews(recycler);
        if (positioner != null) {
            positioner.clearHeader();
        }
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        recyclerView = view;
        Preconditions.validateParentView(recyclerView);
        viewRetriever = new RecyclerViewRetriever(recyclerView);
        positioner = new StickyHeaderPositioner(recyclerView);
        positioner.setElevateHeaders(headerElevation);
        positioner.setListener(listener);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);

        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        }

        if (newAdapter != null) {
            newAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        }
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        if (positioner != null) {
            positioner.clearHeader();
        }
    }

    private RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (positioner != null) {
                positioner.clearHeader();
            }
        }
    };
}

package com.brandongogetap.stickyheaders.demo;

import android.content.Context;
import com.brandongogetap.stickyheaders.StickyGridLayoutManager;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler;

public final class TopSnappedStickyLayoutManager extends StickyGridLayoutManager {

    TopSnappedStickyLayoutManager(Context context, StickyHeaderHandler headerHandler) {
        super(context, 2, headerHandler);
    }

    @Override public void scrollToPosition(int position) {
        super.scrollToPositionWithOffset(position, 0);
    }
}

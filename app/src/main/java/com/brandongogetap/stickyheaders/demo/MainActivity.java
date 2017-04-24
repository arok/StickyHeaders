package com.brandongogetap.stickyheaders.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.brandongogetap.stickyheaders.StickyGridLayoutManager;
import com.brandongogetap.stickyheaders.exposed.StickyHeaderListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        List<Item> items = compileItems();
        final RecyclerAdapter adapter = new RecyclerAdapter(items);
        StickyGridLayoutManager layoutManager = new TopSnappedStickyLayoutManager(this, adapter);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItem(position) instanceof HeaderItem ? 2 : 1;
            }
        });
        layoutManager.elevateHeaders(true); // Default elevation of 5dp
        // You can also specify a specific dp for elevation
//        layoutManager.elevateHeaders(10);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        layoutManager.setStickyHeaderListener(new StickyHeaderListener() {
            @Override
            public void headerAttached(RecyclerView.ViewHolder headerView, int adapterPosition) {
                Log.d("Listener", "Attached with position: " + adapterPosition);
            }

            @Override
            public void headerDetached(RecyclerView.ViewHolder headerView, int adapterPosition) {
                Log.d("Listener", "Detached with position: " + adapterPosition);
            }
        });
    }

    private List<Item> compileItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (i == 2 || (i % 4 == 0 && i > 0)) {
                items.add(new HeaderItem("Header at " + i, ""));
            } else {
                items.add(new Item("Item at " + i, "Item description at " + i));
            }
        }
        return items;
    }
}

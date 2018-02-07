package org.horaapps.leafpic.timeline;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.filter.MediaFilter;
import org.horaapps.leafpic.data.provider.CPHelper;
import org.horaapps.leafpic.data.sort.MediaComparators;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.fragments.BaseFragment;
import org.horaapps.leafpic.util.DeviceUtils;
import org.horaapps.leafpic.util.preferences.Defaults;
import org.horaapps.liz.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment which shows the Timeline.
 */
public class TimelineFragment extends BaseFragment {

    public static final String TAG = "TimelineFragment";

    private static final String ARGS_ALBUM = "args_album";

    private static final String KEY_ALBUM = "key_album";
    private static final String KEY_GROUPING_MODE = "key_grouping_mode";

    @BindView(R.id.timeline_items) RecyclerView timelineItems;
    @BindView(R.id.timeline_swipe_refresh_layout) SwipeRefreshLayout refreshLayout;

    private TimelineAdapter timelineAdapter;
    private TimelineListener timelineListener;
    private GridLayoutManager gridLayoutManager;

    private Album contentAlbum;

    private GroupingMode groupingMode;

    public static TimelineFragment newInstance(@NonNull Album album) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ALBUM, album);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            contentAlbum = savedInstanceState.getParcelable(KEY_ALBUM);
            groupingMode = (GroupingMode) savedInstanceState.get(KEY_GROUPING_MODE);
            return;
        }

        // Get content from arguments
        Bundle arguments = getArguments();
        if (arguments == null) return;
        contentAlbum = arguments.getParcelable(ARGS_ALBUM);
        groupingMode = GroupingMode.DAY; // Default
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::loadAlbum);
        setupRecyclerView();
        loadAlbum();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timeline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        GroupingMode selectedGrouping = getGroupingMode(item.getItemId());
        if (selectedGrouping == null) return false;

        groupingMode = selectedGrouping;
        timelineAdapter.setGroupingMode(groupingMode);
        item.setChecked(true);
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(KEY_ALBUM, contentAlbum);
        outState.putSerializable(KEY_GROUPING_MODE, groupingMode);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    private GroupingMode getGroupingMode(@IdRes int menuId) {
        switch (menuId) {
            case R.id.timeline_grouping_day: return GroupingMode.DAY;
            case R.id.timeline_grouping_week: return GroupingMode.WEEK;
            case R.id.timeline_grouping_month: return GroupingMode.MONTH;
            case R.id.timeline_grouping_year: return GroupingMode.YEAR;
            default: return null;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int gridSize = getTimelineGridSize();
        timelineAdapter.setTimelineGridSize(gridSize);
        gridLayoutManager.setSpanCount(gridSize);
    }

    public void setTimelineListener(@NonNull TimelineListener timelineListener) {
        this.timelineListener = timelineListener;
    }

    private void setupRecyclerView() {
        TimelineAdapter.TimelineItemDecorator decorator = new TimelineAdapter.TimelineItemDecorator(getContext(), R.dimen.timeline_decorator_spacing);
        gridLayoutManager = new GridLayoutManager(getContext(), getTimelineGridSize());
        timelineItems.setLayoutManager(gridLayoutManager);
        timelineItems.addItemDecoration(decorator);

        timelineAdapter = new TimelineAdapter(getContext(), getTimelineGridSize());
        timelineAdapter.setGridLayoutManager(gridLayoutManager);
        timelineAdapter.setGroupingMode(groupingMode);
        timelineAdapter.getClicks()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pos -> {
                    if (timelineListener != null) {
                        timelineListener.onMediaClick(contentAlbum, timelineAdapter.getMedia(), pos);
                    }
                });

        timelineItems.setAdapter(timelineAdapter);
    }

    private void loadAlbum() {
        List<Media> mediaList = new ArrayList<>();
        CPHelper.getMedia(getContext(), contentAlbum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(media -> MediaFilter.getFilter(contentAlbum.filterMode()).accept(media))
                .subscribe(mediaList::add,
                        throwable -> refreshLayout.setRefreshing(false),
                        () -> {
                            contentAlbum.setCount(mediaList.size());
                            refreshLayout.setRefreshing(false);
                            setAdapterMedia(mediaList);
                        });
    }

    private void setAdapterMedia(@NonNull List<Media> mediaList) {
        Collections.sort(mediaList, MediaComparators.getComparator(SortingMode.DATE, SortingOrder.DESCENDING));
        timelineAdapter.setMedia(mediaList);
    }

    private int getTimelineGridSize() {
        return DeviceUtils.isPortrait(getResources())
                ? Defaults.TIMELINE_ITEMS_PORTRAIT
                : Defaults.TIMELINE_ITEMS_LANDSCAPE;
    }

    @Override
    public boolean editMode() {
        return timelineAdapter.selecting();
    }

    @Override
    public boolean clearSelected() {
        return timelineAdapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        timelineItems.setBackgroundColor(t.getBackgroundColor());
        timelineAdapter.refreshTheme(t);
        refreshLayout.setColorSchemeColors(t.getAccentColor());
        refreshLayout.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }

    /**
     * Interface to report events to parent container
     */
    public interface TimelineListener {

        void onMediaClick(Album album, ArrayList<Media> media, int position);
    }
}

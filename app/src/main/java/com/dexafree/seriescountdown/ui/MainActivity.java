package com.dexafree.seriescountdown.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.dexafree.seriescountdown.R;
import com.dexafree.seriescountdown.adapters.SeriesAdapter;
import com.dexafree.seriescountdown.adapters.ViewPagerAdapter;
import com.dexafree.seriescountdown.interfaces.SeriesView;
import com.dexafree.seriescountdown.model.Serie;
import com.dexafree.seriescountdown.presenters.RxMainPresenter;
import com.dexafree.seriescountdown.utils.RecyclerClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MainActivity extends BaseActivity implements SeriesView, RxMainPresenter.Callback {


    @Bind(R.id.series_recycler_view)
    RecyclerView seriesRecyclerView;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.tablayout)
    TabLayout tabLayout;

    private RxMainPresenter presenter;
    private SeriesAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new RxMainPresenter(this);
        setToolbar();
        ButterKnife.bind(this);
        mAdapter = new SeriesAdapter(new ArrayList<Serie>());
        prepareViews();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        presenter.loadSeries();
    }

    private void prepareViews(){

        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);


        seriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2) {
            @Override
            public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
                int overscroll = dx - scrollRange;
                if (overscroll > 10) {
                    presenter.listFinished();
                }
                return scrollRange;
            }
        });
        seriesRecyclerView.setItemAnimator(new SlideInUpAnimator());
        seriesRecyclerView.getItemAnimator().setAddDuration(300);
        seriesRecyclerView.getItemAnimator().setRemoveDuration(300);
        seriesRecyclerView.addOnItemTouchListener(new RecyclerClickListener(this, new RecyclerClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Serie serie = (Serie) view.getTag();
                ImageView image = (ImageView) view.findViewById(R.id.serie_image);

                startDetailActivity(serie, image);
            }
        }));
        seriesRecyclerView.setAdapter(mAdapter);
    }

    private void setupViewPager(){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.accent_material_light)), "CAT");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.ripple_material_light)), "DOG");
        adapter.addFrag(new DummyFragment(getResources().getColor(R.color.button_material_dark)), "MOUSE");
        viewPager.setAdapter(adapter);
    }

    private void startDetailActivity(Serie serie, View view){
        DetailActivity.launch(this, view, serie);
    }

    @Override
    public void showSeries(List<Serie> series) {
        seriesRecyclerView.setAdapter(new SeriesAdapter(series));
    }

    @Override
    public void addItem(Serie serie) {
        mAdapter.addItem(serie);
    }

    @Override
    public void showError() {

    }
}

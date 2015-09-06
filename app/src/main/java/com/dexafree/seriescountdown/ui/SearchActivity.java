package com.dexafree.seriescountdown.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;

import com.dexafree.seriescountdown.R;
import com.dexafree.seriescountdown.adapters.SeriesAdapter;
import com.dexafree.seriescountdown.interfaces.ISearchView;
import com.dexafree.seriescountdown.model.Serie;
import com.dexafree.seriescountdown.presenters.SearchPresenter;
import com.dexafree.seriescountdown.utils.RecyclerClickListener;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

/**
 * Created by Carlos on 5/9/15.
 */
public class SearchActivity extends BaseActivity implements ISearchView {

    private final static String EXTRA_IMAGE = "SearchActivity:fabimage";
    private final static long ANIM_DURATION = 1000;

    @Bind(R.id.content_layout)
    RelativeLayout contentLayout;

    @Bind(R.id.search_edittext)
    AutoCompleteTextView searchEditText;

    @Bind(R.id.series_recyclerview)
    RecyclerView seriesRecyclerView;

    @OnClick(R.id.search_button)
    void onSearchButtonClick(){
        searchText();
    }

    private SearchPresenter presenter;
    private SeriesAdapter seriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setToolbar();
        ButterKnife.bind(this);
        presenter = new SearchPresenter(this);
        prepareLists();
    }

    private void prepareLists(){

        RxTextView
                .textChangeEvents(searchEditText)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(event -> presenter.onTextChanged(event.text().toString()));

        seriesAdapter = new SeriesAdapter(new ArrayList<>());

        seriesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2) {
            @Override
            public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
                int overscroll = dx - scrollRange;
                if (overscroll > 20) {
                    scrollFinished();
                }
                return scrollRange;
            }
        });
        seriesRecyclerView.setItemAnimator(new FadeInUpAnimator());
        seriesRecyclerView.getItemAnimator().setAddDuration(400);
        seriesRecyclerView.getItemAnimator().setRemoveDuration(300);
        seriesRecyclerView.addOnItemTouchListener(new RecyclerClickListener(getContext(),
                (view, position) -> presenter.onItemClicked(position)));

        seriesRecyclerView.setAdapter(seriesAdapter);

        searchEditText.setOnItemClickListener((parent, view, position, id) -> {
            String query = parent.getAdapter().getItem(position).toString();
            presenter.searchText(query);
        });
    }

    private void scrollFinished(){
        searchText();
    }

    private void searchText(){
        String query = searchEditText.getText().toString();
        presenter.onScrollFinished(query);
    }

    @Override
    public void showSuggestions(List<String> suggestions) {
        if(suggestions.size() > 0) {
            ArrayAdapter<String> suggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
            searchEditText.setAdapter(suggestionsAdapter);
            searchEditText.showDropDown();
        } else {
            searchEditText.dismissDropDown();
        }
    }

    @Override
    public void addSerie(Serie serie) {
        seriesAdapter.addItem(serie);
    }

    @Override
    public void cleanList() {
        seriesAdapter.clearItems();
    }

    @Override
    public void showSearchError() {
        showToast("Search error");
    }
}

package com.topjohnwu.magisk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.topjohnwu.magisk.adapters.ReposAdapter;
import com.topjohnwu.magisk.asyncs.UpdateRepos;
import com.topjohnwu.magisk.components.Fragment;
import com.topjohnwu.magisk.utils.Topic;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReposFragment extends Fragment implements Topic.Subscriber {

    private Unbinder unbinder;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.empty_rv) TextView emptyRv;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;

    public static ReposAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repos, container, false);
        unbinder = ButterKnife.bind(this, view);

        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            recyclerView.setVisibility(View.VISIBLE);
            emptyRv.setVisibility(View.GONE);
            new UpdateRepos(true).exec();
        });

        getActivity().setTitle(R.string.downloads);

        return view;
    }

    @Override
    public void onResume() {
        adapter = new ReposAdapter(getApplication().repoDB, getApplication().moduleMap);
        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter = null;
    }

    @Override
    public void onTopicPublished(Topic topic, Object result) {
        mSwipeRefreshLayout.setRefreshing(false);
        recyclerView.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        emptyRv.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public Topic[] getSubscription() {
        return new Topic[] { getApplication().repoLoadDone };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_repo, menu);
        SearchView search = (SearchView) menu.findItem(R.id.repo_search).getActionView();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

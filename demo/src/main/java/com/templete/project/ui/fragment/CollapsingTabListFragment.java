package com.templete.project.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lib.base.adapter.DemoAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 折叠场景 Tab 页：简单列表，供 ViewPager2 使用。
 */
public class CollapsingTabListFragment extends Fragment {

    private static final String ARG_PREFIX = "prefix";
    private static final String ARG_LABEL = "label";

    public static CollapsingTabListFragment newInstance(String prefix, String label) {
        CollapsingTabListFragment f = new CollapsingTabListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PREFIX, prefix);
        b.putString(ARG_LABEL, label);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setOverScrollMode(View.OVER_SCROLL_NEVER);

        String prefix = "条目 ";
        String label = "";
        Bundle args = getArguments();
        if (args != null) {
            prefix = args.getString(ARG_PREFIX, prefix);
            label = args.getString(ARG_LABEL, "");
        }

        DemoAdapter adapter = new DemoAdapter(requireContext());
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(prefix + (label.isEmpty() ? "" : (label + " · ")) + (i + 1));
        }
        adapter.setList(list);
        rv.setAdapter(adapter);
        return rv;
    }
}

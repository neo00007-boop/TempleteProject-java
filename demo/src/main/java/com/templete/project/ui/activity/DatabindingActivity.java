package com.templete.project.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.greendao.db.bean.DemoBean;
import com.lib.base.mvvm.BaseViewModel;
import com.templete.project.R;
import com.templete.project.databinding.DataBindingActivityBinding;
import com.templete.project.databinding.DynamicItem1Binding;
import com.templete.project.mvvm.DemoViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * PackageName  com.templete.project.ui.activity
 * ProjectName  TempleteProject-java
 * Date         2022/5/11.
 *
 * @author xwchen
 */

public class DatabindingActivity extends AppCompatActivity {

    private DemoViewModel demoViewModel;

    public <U extends BaseViewModel> U createViewModel(Class<U> clazz) {
        if (clazz == null) {
            throw new RuntimeException("clazz mustn't be null!");
        }
        // AppCompatActivity 已实现 HasDefaultViewModelProviderFactory，默认就是 AndroidViewModelFactory
        return new ViewModelProvider(this).get(clazz);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingActivityBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.data_binding_activity);
        viewDataBinding.setLifecycleOwner(this);

        demoViewModel = createViewModel(DemoViewModel.class);

        RecyclerView recyclerView = viewDataBinding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        List<DemoBean> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(new DemoBean());
        }
        myAdapter.submitList(list);

        viewDataBinding.getRoot().postDelayed(() -> {
            demoViewModel.setDarks(1);
        }, 1000);
    }

    class MyAdapter extends BaseQuickAdapter<DemoBean, MyAdapter.ViewHolder> {

        public MyAdapter() {
            super();
        }

        @NonNull
        @Override
        protected ViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
            DynamicItem1Binding itemBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dynamic_item1, parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @Nullable DemoBean item) {
            holder.bind();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            DynamicItem1Binding binding;

            public ViewHolder(DynamicItem1Binding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind() {
                binding.setDemo(demoViewModel.getDarks().getValue());
                binding.executePendingBindings();
            }
        }
    }
}

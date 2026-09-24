package com.templete.project.ui.activity;

import android.util.Pair;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.bean.NavBean;
import com.templete.project.databinding.NavsActivityBinding;
import com.templete.project.ui.fragment.ContainerFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 1、根据导航数据自动处理多级viewpager嵌套，正常最多两层viewpager（超过三层后很容易会有上百个子fragment，这是不合理的）；
 * 2、view懒加载，不展示的viewpager不会初始化，最大限度减轻内存压力；
 * 3、http懒加载，不展示的viewpager不会发起网络请求，最大限度减轻服务端压力，不会直接一次发起几十个请求.
 * <p>
 * ProjectName  TempleteProject-java
 * PackageName  com.templete.project.ui.activity
 * Author       Administrator
 * Date         2022/3/11.
 */

public class NavsActivity extends BaseActivity<NavsActivityBinding> {

    private List<NavBean> navs;
    private ContainerFragment containerFragment;

    @Override
    protected NavsActivityBinding viewBinding() {
        return NavsActivityBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("ViewPager 多级导航");
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initData() {
        // 一级 → 二级 → 三级；叶子无子节点时展示 ContentFragment
        navs = Arrays.asList(
                buildFamily("张", "大三", true),
                buildFamily("李", "大四", false),
                buildFamily("王", "大五", false),
                buildFamily("赵", "大六", false),
                buildFamily("孙", "大七", false),
                buildFamily("周", "大八", false)
        );
        bindIndexPair(navs, -1);

        containerFragment = ContainerFragment.newInstance(navs, false);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, containerFragment).commitNow();
    }

    /** 一级：姓+排行；二级：姓+一~七；三级：二级名+甲乙丙 */
    private NavBean buildFamily(String surname, String rank, boolean select) {
        NavBean root = new NavBean(surname + rank, "data", select, null);
        String[] seconds = {"一", "二", "三", "四", "五", "六", "七"};
        String[] thirds = {"甲", "乙", "丙"};
        List<NavBean> level2 = new ArrayList<>(seconds.length);
        for (int i = 0; i < seconds.length; i++) {
            String l2Name = surname + seconds[i];
            NavBean l2 = new NavBean(l2Name, "data", i == 0, null);
            List<NavBean> level3 = new ArrayList<>(thirds.length);
            for (int j = 0; j < thirds.length; j++) {
                level3.add(new NavBean(l2Name + thirds[j], "data", j == 0, null));
            }
            l2.navs = level3;
            level2.add(l2);
        }
        root.navs = level2;
        return root;
    }

    private void bindIndexPair(List<NavBean> list, int parentIndex) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            NavBean bean = list.get(i);
            bean.indexPair = new Pair<>(parentIndex, i);
            bindIndexPair(bean.navs, i);
        }
    }

    public NavBean getDefaultFirstNav() {
        if (navs == null || navs.isEmpty()) return null;
        // 找当前层选中
        NavBean selected = null;
        for (NavBean nav : navs) {
            if (nav.isSelect) {
                selected = nav;
                break;
            }
        }
        // 如果没有 isSelect，取第一个
        if (selected == null) {
            selected = navs.get(0);
        }
        // 一直往下找
        while (selected.navs != null && !selected.navs.isEmpty()) {
            NavBean next = null;
            for (NavBean child : selected.navs) {
                if (child.isSelect) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                next = selected.navs.get(0);
            }
            selected = next;
        }
        return selected;
    }
}
package com.james602152002.multiaxiscardlayoutmanagerdemo.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.james602152002.multiaxiscardlayoutmanagerdemo.R;
import com.james602152002.multiaxiscardlayoutmanagerdemo.adapter.CardDetailAdapter;
import com.james602152002.multiaxiscardlayoutmanagerdemo.bean.BeanCardDetailListItems;
import com.james602152002.multiaxiscardlayoutmanagerdemo.item_decoration.CardDetailDecoration;
import com.james602152002.multiaxiscardlayoutmanagerdemo.widget.CollapsingToolBarMaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shiki60215 on 18-3-2.
 */

public class ActivityCardDetail extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.smart_refresh_layout)
    SmartRefreshLayout smartRefreshLayout;
    private final SparseArray<BeanCardDetailListItems> data = new SparseArray<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);
        ButterKnife.bind(this);

        initToolBar();
        initView();
    }

    private void initToolBar() {
        CollapsingToolbarLayout mCToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mCToolbarLayout.setCollapsedTitleTextColor(0);
        mCToolbarLayout.setExpandedTitleColor(0);
        // Set the support action bar
        initToolBar(mToolbar);

        LinearLayout view = new LinearLayout(this);
        view.setGravity(Gravity.CENTER_VERTICAL);
        TextView text_view = new TextView(this);
        text_view.setTextColor(Color.WHITE);
        text_view.setText(getIntent().getStringExtra("title"));
        text_view.setGravity(Gravity.CENTER_VERTICAL);
        view.addView(text_view);
        mToolbar.addView(view);
        SimpleDraweeView image = findViewById(R.id.toolbar_image);
        image.setImageURI(getIntent().getStringExtra("uri"));
    }

    private void initView() {
//        smartRefreshLayout.setHeaderHeight(DensityUtil.px2dp(100) )
        ((CollapsingToolBarMaterialHeader) findViewById(R.id.header)).setAppBarLayout((AppBarLayout) findViewById(R.id.appbar));
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                fetchData();
            }
        });
        smartRefreshLayout.autoRefresh();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new CardDetailDecoration());
//        recyclerView.setItemAnimator(new CardDetailItemAnimator());
        final short duration = 1000;
        AnimationSet anim = new AnimationSet(true);
        TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0);
        translateAnim.setDuration(duration);
        RotateAnimation rotateAnim = new RotateAnimation(10, 0);
        rotateAnim.setDuration(duration);
        anim.addAnimation(translateAnim);
        anim.addAnimation(rotateAnim);

        recyclerView.setLayoutAnimation(new LayoutAnimationController(anim, 0));
    }

    private void fetchData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SparseArray<BeanCardDetailListItems> items = new SparseArray<>();
                for (int i = 0; i < 5; i++) {
                    BeanCardDetailListItems item = new BeanCardDetailListItems();
                    item.setTitle(new StringBuilder("Card Detail Title ").append(i + 1).toString());
                    items.put(i, item);
                }
                for (int i = 0; i < items.size(); i++) {
                    data.put(data.size(), items.get(i));
                }
                smartRefreshLayout.finishRefresh();
                recyclerView.setAdapter(new CardDetailAdapter(ActivityCardDetail.this, data));
            }
        }, 2000);
    }
}

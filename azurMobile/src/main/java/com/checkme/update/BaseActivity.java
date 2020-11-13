package com.checkme.update;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import butterknife.ButterKnife;

/**
 * Created by gongguopei on 2018/10/22.
 */
public abstract class BaseActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setWindowFeatureAndFlag();
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        ButterKnife.bind(this);

        initView();
        loadAnimator();
    }

    protected void setWindowFeatureAndFlag() {}
    protected abstract int getContentViewId();
    protected void initView() {}
    protected void loadAnimator() {}
}

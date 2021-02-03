package com.checkme.azur.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.checkme.newazur.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by gongguopei on 2018/12/17.
 */
public class DisclaimerActivity extends com.checkme.update.BaseActivity{

    @BindView(R.id.WidgetActionbarTextTitle)
    TextView title;

    @BindView(R.id.tv_disclaimer_content)
    TextView mArticleTextView;

//    int width;
    @Override
    protected int getContentViewId() {
        return R.layout.activity_disclaimer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        if(title != null) {
            title.setText(R.string.tv_about_this_app);
        }
    }

    @OnClick(R.id.WidgetActionbarBnMenu)
    public void backClick() {
        super.onBackPressed();
    }


}

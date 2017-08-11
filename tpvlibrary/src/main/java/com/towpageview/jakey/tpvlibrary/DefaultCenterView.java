package com.towpageview.jakey.tpvlibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by jakey on 2017/7/22.
 */

public class DefaultCenterView implements ICenterView {

    private final Context mContext;
    private TextView mTvPullUp;
    private TextView mTvPullDown;
    private ImageView mIvPull;
    private View mInflate;

    public DefaultCenterView(Context context) {
        mContext = context;
    }

    @Override
    public View getView() {

        if(null==mInflate) {
            mInflate = LayoutInflater.from(mContext).inflate(R.layout.default_center_view, null);
            mTvPullUp = (TextView) mInflate.findViewById(R.id.tv_pull_up);
            mTvPullDown = (TextView) mInflate.findViewById(R.id.tv_pull_down);
            mIvPull = (ImageView) mInflate.findViewById(R.id.iv_pull);
        }

        return mInflate;
    }

    @Override
    public void OnPullToNext() {
        mTvPullUp.setVisibility(View.VISIBLE);
        mTvPullDown.setVisibility(View.GONE);
        mTvPullUp.setText("上拉查看图文详情");
        mIvPull.setRotation(0);

    }

    @Override
    public void OnReleaseToNext() {
        mTvPullUp.setVisibility(View.VISIBLE);
        mTvPullDown.setVisibility(View.GONE);
        mTvPullUp.setText("释放查看图文详情");
        mIvPull.setRotation(180);
    }

    @Override
    public void OnPullToUp() {
        mTvPullUp.setVisibility(View.GONE);
        mTvPullDown.setVisibility(View.VISIBLE);
        mTvPullDown.setText("下拉返回顶部");
        mIvPull.setRotation(180);
    }

    @Override
    public void OnReleaseToUp() {
        mTvPullUp.setVisibility(View.GONE);
        mTvPullDown.setVisibility(View.VISIBLE);
        mTvPullDown.setText("释放返回顶部");
        mIvPull.setRotation(0);
    }

    @Override
    public void OnDone() {
        mTvPullUp.setVisibility(View.VISIBLE);
        mTvPullDown.setVisibility(View.GONE);
        mTvPullUp.setText("");
        mIvPull.setRotation(0);
    }

    @Override
    public void OnUping() {
        mTvPullUp.setVisibility(View.GONE);
        mTvPullDown.setVisibility(View.VISIBLE);
        mTvPullDown.setText("正在返回顶部");
        mIvPull.setRotation(0);
    }

    @Override
    public void OnNexting() {
        mTvPullUp.setVisibility(View.VISIBLE);
        mTvPullDown.setVisibility(View.GONE);
        mTvPullUp.setText("正切换到图文详情");
        mIvPull.setRotation(180);
    }
}

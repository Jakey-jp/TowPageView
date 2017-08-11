package com.towpageview.jakey.tpvlibrary;

import android.view.View;

/**
 * Created by jakey on 2017/7/22.
 */

public interface ICenterView {

    View getView();

    void OnPullToNext();

    void OnReleaseToNext();

    void OnPullToUp();

    void OnReleaseToUp();

    void OnDone();

    void OnUping();

    void OnNexting();


}

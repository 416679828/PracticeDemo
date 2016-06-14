package com.gank.todaynews.view;

import android.os.Bundle;
import android.util.ArrayMap;

import com.gank.base.BaseTodayNewsListFramgent;
import com.gank.data.Story;
import com.gank.data.TodayNews;
import com.gank.setting.SwithModelEvent;
import com.gank.todaynews.adapter.TodayItemAdapter;
import com.gank.todaynews.model.TodayModelImp;
import com.gank.todaynews.presenter.TodayPresenter;
import com.gank.todaynews.presenter.TodayPresenterImp;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by thinkpad on 2016/4/29.
 */
public class TodayFragment extends BaseTodayNewsListFramgent implements TodayView, TodayItemAdapter.onImgCollectClickListener {
    private TodayPresenter dayGankPresenter;
    private Realm realm;
    private ArrayMap<Long, Boolean> mMap = new ArrayMap<>();
    public TodayFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        EventBus.getDefault().register(this);
        dayGankPresenter = new TodayPresenterImp(new TodayModelImp(getContext()), this);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMap.clear();
        RealmQuery<Story> query = realm.where(Story.class);
        todayItemAdapter.setmMap(putElementToMap(query.findAll()));
    }

    private ArrayMap<Long, Boolean> putElementToMap(RealmResults<Story> results) {
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                mMap.put(results.get(i).getmId(), true);
            }
        }
        return mMap;
    }

    @Override
    protected void loadData() {
        dayGankPresenter.loadData();

    }

    @Override
    public void setupDayGankDataToView(TodayNews dayGankData) {
        todayItemAdapter.setmList(dayGankData.getmStories());

    }

    @Override
    public void showProgress() {
        refreshView.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        refreshView.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null)
            realm.close();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void setImgCollectListener(TodayItemAdapter adapter) {
        adapter.setListener(this);

    }

    @Override
    public void onImgClick(Story dayGankData, boolean isChecked,ArrayMap<Long, Boolean> mMap) {
        mMap.put(dayGankData.getmId(),isChecked);
        if(isChecked) {
            copyToDataBase(dayGankData);
        }else {
            removeFromDataBase(dayGankData);
        }

    }

    private void removeFromDataBase(Story dayGankData) {
        realm.beginTransaction();
        RealmResults<Story> results = realm.where(Story.class).findAll();
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getmId() == dayGankData.getmId()) {
                results.remove(i);
            }
        }
        realm.commitTransaction();
    }

    private void copyToDataBase(Story dayGankData) {
        if (realm != null) {
            realm.beginTransaction();
            realm.copyToRealm(dayGankData);
            realm.commitTransaction();

        }
    }

    public void onEvent(SwithModelEvent event) {
        getFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();

    }
}

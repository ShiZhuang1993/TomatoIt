package com.lqm.tomatoit.ui.presenter;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lqm.tomatoit.R;
import com.lqm.tomatoit.api.WanService;
import com.lqm.tomatoit.model.ResponseData;
import com.lqm.tomatoit.model.pojoVO.ArticleListVO;
import com.lqm.tomatoit.model.pojoVO.TypeTagVO;
import com.lqm.tomatoit.ui.adapter.ArticleListAdapter;
import com.lqm.tomatoit.ui.base.BasePresenter;
import com.lqm.tomatoit.ui.view.TypeView;
import com.lqm.tomatoit.util.UIUtils;
import com.lqm.tomatoit.widget.AutoLinefeedLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * user：lqm
 * desc：分类
 */

public class TypePresenter extends BasePresenter<TypeView> {

    private FragmentActivity mActivity;
    private TypeView mTypeView;
    private int mCurrentPage ;
    private List<TypeTagVO> mTagDatas;
    private ArticleListAdapter mAdapter;
    private int mId;
    private int mTabSelect; //标记选中的Tab标签
    private int mTagSelect; //标记选中的Tag标签，用户设置背景色
    private List<TextView> tagTvs;
    private AutoLinefeedLayout llTag;

    public TypePresenter(FragmentActivity activity) {
        this.mActivity = activity;
    }

    //分类标签
    public void getTagData() {
        mTypeView = getView();
        WanService.getTypeTagData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<ResponseData<List<TypeTagVO>>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(ResponseData<List<TypeTagVO>> responseData) {
                    mTagDatas = responseData.getData();
                    setTabUI();
                    mTabSelect = 0;
                    mTagSelect = 0;
                    getServerData(mTagDatas.get(0).getChildren().get(0).getId());
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
    }

    //一级Tab
    private void setTabUI() {
        TabLayout tabLayout = mTypeView.getTabLayout();
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (TypeTagVO bean : mTagDatas) {
            tabLayout.addTab(tabLayout.newTab().setText(bean.getName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTagUI(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (llTag != null && llTag.getVisibility() == View.VISIBLE){
                    llTag.setVisibility(View.GONE);
                }else{
                    setTagUI(tab.getPosition());
                }
            }
        });

    }

    //二级Tag
    private void setTagUI(int position) {
        llTag = mTypeView.getTagLayout();
        llTag.setVisibility(View.VISIBLE);
        llTag.removeAllViews();
        if (tagTvs == null){
            tagTvs = new ArrayList<>();
        }else{
            tagTvs.clear();
        }
        for (int i = 0; i < mTagDatas.get(position).getChildren().size(); i++) {
            View view = LinearLayout.inflate(mActivity, R.layout.item_tag_layout, null);
            TextView tvItem = (TextView) view.findViewById(R.id.tv_item);
            tvItem.setText(mTagDatas.get(position).getChildren().get(i).getName());
            llTag.addView(view);
            tagTvs.add(tvItem);

            int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击某个tag按钮
                    mTabSelect = position;
                    mTagSelect = finalI;
                    tvItem.setTextColor(UIUtils.getColor(R.color.white));
                    tvItem.setBackgroundResource(R.drawable.shape_tag_sel);
                    mId = mTagDatas.get(position).getChildren().get(finalI).getId();
                    getServerData(mId);
                }
            });
        }

        //设置选中的tag背景
        for (int j = 0; j < tagTvs.size(); j++) {
            if (position == mTabSelect && j == mTagSelect){
                tagTvs.get(j).setTextColor(UIUtils.getColor(R.color.white));
                tagTvs.get(j).setBackgroundResource(R.drawable.shape_tag_sel);
            }else{
                tagTvs.get(j).setTextColor(UIUtils.getColor(R.color.text0));
                tagTvs.get(j).setBackgroundResource(R.drawable.shape_tag_nor);
            }
        }

    }


    //根据点击的标签获取数据
    public void getServerData(int cid){
        mCurrentPage = 0; //从第0页开始
        mAdapter =  mTypeView.getAdapter();
        WanService.getTypeDataById(mCurrentPage,cid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseData<ArticleListVO>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseData<ArticleListVO> responseData) {
                        if (responseData.getData().getDatas() != null){
                            getView().getRefreshDataSuccess(responseData.getData().getDatas());
                            mTypeView.getTagLayout().setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().getDataError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    //加载下一页
    public void getMoreData() {
        mCurrentPage = mCurrentPage + 1;
        WanService.getTypeDataById(mCurrentPage,mId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseData<ArticleListVO>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseData<ArticleListVO> responseData) {
                        getView().getMoreDataSuccess(responseData.getData().getDatas());
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().getDataError(e.getMessage());

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

}
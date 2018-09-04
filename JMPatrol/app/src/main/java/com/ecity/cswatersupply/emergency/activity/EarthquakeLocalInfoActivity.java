package com.ecity.cswatersupply.emergency.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ecity.android.eventcore.EventBusUtil;
import com.ecity.android.eventcore.ResponseEvent;
import com.ecity.android.eventcore.UIEvent;
import com.ecity.cswatersupply.Constants;
import com.ecity.cswatersupply.R;
import com.ecity.cswatersupply.SessionManager;
import com.ecity.cswatersupply.adapter.ArrayListAdapter;
import com.ecity.cswatersupply.emergency.adapter.QuakeInfoListAdapter;
import com.ecity.cswatersupply.emergency.fragment.ReportQueryInfoFragment;
import com.ecity.cswatersupply.emergency.menu.EarthQuakeFieldInvestigationCommand;
import com.ecity.cswatersupply.emergency.menu.EarthQuakeQuickReportCommand;
import com.ecity.cswatersupply.emergency.menu.QuakeInfoDistributionOperaterXtd;
import com.ecity.cswatersupply.emergency.menu.QuakeInfoSearchOperater;
import com.ecity.cswatersupply.emergency.menu.UnUsualReportOperater;
import com.ecity.cswatersupply.emergency.model.EarthQuakeInfoModel;
import com.ecity.cswatersupply.emergency.model.EarthQuakeQuickReportModel;
import com.ecity.cswatersupply.emergency.model.SearchType;
import com.ecity.cswatersupply.emergency.network.request.GetEarthQuakeParameter;
import com.ecity.cswatersupply.emergency.service.EmergencyService;
import com.ecity.cswatersupply.emergency.view.CustomAlertDialog;
import com.ecity.cswatersupply.event.ResponseEventStatus;
import com.ecity.cswatersupply.event.UIEventStatus;
import com.ecity.cswatersupply.model.RequestCode;
import com.ecity.cswatersupply.model.ResultCode;
import com.ecity.cswatersupply.network.RequestParameter;
import com.ecity.cswatersupply.ui.activities.CustomReportActivity1;
import com.ecity.cswatersupply.ui.activities.MapActivity;
import com.ecity.cswatersupply.ui.widght.MapActivityTitleView;
import com.ecity.cswatersupply.utils.CustomViewInflater;
import com.ecity.cswatersupply.utils.LoadingDialogUtil;
import com.ecity.cswatersupply.utils.ResourceUtil;
import com.ecity.cswatersupply.utils.ToastUtil;
import com.ecity.cswatersupply.utils.UIHelper;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.zzz.ecity.android.applibrary.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地震情
 *
 * @author 49136
 */
public class EarthquakeLocalInfoActivity extends BaseActivity {

    public final static String QUAKE_ITEM_CLICK_RESULT = "QUAKE_ITEM_CLICK_RESULT";
    private final static int INTERVAL = 1000 * 60 * 5;

    public  List<EarthQuakeInfoModel> quakeInfoLists = new ArrayList<EarthQuakeInfoModel>();
    private MapActivityTitleView titleView;
    private ListView mListView;
    private LinearLayout searchLayout;
    private PullToRefreshListView pullRefreshListView;
    private ArrayListAdapter<EarthQuakeInfoModel> mAdapter;
    private RequestParameter.IRequestParameter searchParameter;
    //是否是查询返回的地震信息
    private boolean isSearchResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_information);
        EventBusUtil.register(this);
        initTitleView();
        initView();
        initLisenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (validate()) {
            requestQuakeInfoList();
        } else {
            quakeInfoLists = SessionManager.quakeInfoList;
            mAdapter.setList(quakeInfoLists);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initTitleView() {
        titleView = (MapActivityTitleView) findViewById(R.id.view_title);
        if (null != this.getIntent().getExtras()) {//TODO
            titleView.setBtnStyle(MapActivityTitleView.BtnStyle.ONLY_BACK);
        } else {
            titleView.setBtnStyle(MapActivityTitleView.BtnStyle.RIGHT_ACTION);
            titleView.setLegendBackground(getResources().getDrawable(R.drawable.icon_map_unfocus));
        }
        titleView.setTitleText(getResources().getString(R.string.earth_quake_information_title_quick));
    }

    private void initView() {
        searchLayout = (LinearLayout) findViewById(R.id.ll_searchview);
        pullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_listview);
        pullRefreshListView.setPullLoadEnabled(false);
        pullRefreshListView.setPullRefreshEnabled(true);

        mListView = pullRefreshListView.getRefreshableView();
        mListView.setDivider(ResourceUtil.getDrawableResourceById(R.drawable.shape_list_divider));
        mListView.setDividerHeight(ResourceUtil.getDimensionPixelSizeById(R.dimen.margin_spacing_level_4));

        mAdapter = new QuakeInfoListAdapter(this);
        mAdapter.setList(quakeInfoLists);
        mListView.setAdapter(mAdapter);
    }

    private void initLisenter() {
        titleView.setOnLegendListener(new RigthActionClickListener());
        searchLayout.setOnClickListener(new EarthquakeLocalInfoActivity.SearchViewOnClickListener());
        pullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestQuakeInfoList();
                pullRefreshListView.onPullDownRefreshComplete();
                pullRefreshListView.setLastUpdateTime();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mListView.setOnItemClickListener(new EarthquakeLocalInfoActivity.OnListItemClickListener());
    }

    public void onBackButtonClicked(View v) {//TODO
        finish();
    }

    private void requestQuakeInfoList() {
        isSearchResult  = false;
        SessionManager.lastUpdateTime = System.currentTimeMillis();
        LoadingDialogUtil.show(this, R.string.getting_earth_quake_infos);
        EmergencyService.getInstance().getEarthQuackeList(new GetEarthQuakeParameter(), ResponseEventStatus.EMERGENCY_GET_EARTHQUAKE_ALL);
    }

    private boolean validate() {
        //最多2分钟更新一次
        if ((System.currentTimeMillis() - SessionManager.lastUpdateTime) > INTERVAL) {
            SessionManager.lastUpdateTime = System.currentTimeMillis();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case ResultCode.EARTH_QUAKE_INFO_OK:
                getSearchResult(data);
                break;
            case ResultCode.EARTH_QUAKE_INFO_SEARCH_CANCEL:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBusUtil.unregister(this);
        super.onDestroy();
    }

    private void getSearchResult(Intent data) {
        isSearchResult  = true;
        searchParameter = (RequestParameter.IRequestParameter) data.getSerializableExtra(Constants.EARTH_QUAKE_INFO_PARAM);
        if (null == searchParameter) {
            Toast.makeText(getApplicationContext(), R.string.no_earth_quake_infos, Toast.LENGTH_LONG).show();
            return;
        }
        //调用网络
        EmergencyService.getInstance().getEarthQuackeList(searchParameter, ResponseEventStatus.EMERGENCY_GET_EARTHQUAKE_ALL);
        LoadingDialogUtil.show(this, R.string.str_searching);
    }

    public void onEventMainThread(UIEvent event) {
        switch (event.getId()) {
            case UIEventStatus.EMERGENCY_ANNONOUN_SEARCH:
                Intent data = event.getData();
                getSearchResult(data);
        }
    }

    public void onEventMainThread(ResponseEvent event) {
        LoadingDialogUtil.dismiss();
        pullRefreshListView.onPullUpRefreshComplete();
        pullRefreshListView.onPullDownRefreshComplete();
        pullRefreshListView.setLastUpdateTime();
        if (!event.isOK()) {
            return;
        }
        switch (event.getId()) {
            case ResponseEventStatus.EMERGENCY_GET_EARTHQUAKE_ALL:
                // 得到所有地震信息
                handleGetEarthQuakeInfoList(event);
                break;
            case ResponseEventStatus.EMERGENCY_GET_REPORT_RELATE:
                LoadingDialogUtil.dismiss();
                ToastUtil.show("关联成功", 0);
                UIEvent uiEvent = new UIEvent(UIEventStatus.EMERGENCY_RELATE_FINISH);
                EventBusUtil.post(uiEvent);
                this.finish();
                break;
            default:
                break;
        }
    }

    private void handleGetEarthQuakeInfoList(ResponseEvent event) {
        quakeInfoLists = event.getData();

        if (null == quakeInfoLists) {
            Toast.makeText(getApplicationContext(), R.string.no_earth_quake_infos, Toast.LENGTH_LONG).show();
            return;
        }

        if(!isSearchResult) {
            SessionManager.quakeInfoList = quakeInfoLists;
        }
        mAdapter.setList(quakeInfoLists);
        mAdapter.notifyDataSetChanged();
    }

    //设置查询框点击事件
    private class SearchViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(EarthquakeLocalInfoActivity.this, CustomReportActivity1.class);
            Bundle bundle = new Bundle();
            bundle.putInt(CustomViewInflater.REPORT_TITLE, R.string.earth_quake_infor_search_title);
            bundle.putString(CustomViewInflater.REPORT_COMFROM, QuakeInfoSearchOperater.class.getName());
            bundle.putInt(CustomViewInflater.BOTTOM_SINLEBTN_TXT, R.string.ok);
            //SearchType.EARTH_QUAKE为地震信息查询；SearchType.QUICK_REPORT为速报信息查询
            bundle.putInt(Constants.EARTH_QUAKE_INFO_SEARCH_TYPE, SearchType.EARTH_QUAKE.getValue());
            intent.putExtras(bundle);
            startActivityForResult(intent, RequestCode.EARTH_QUAKE_INFO_SEARCH, bundle);
        }
    }

    private class RigthActionClickListener implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(EarthquakeLocalInfoActivity.this, MapActivity.class);
            intent.putExtra(MapActivity.MAP_OPERATOR, QuakeInfoDistributionOperaterXtd.class.getName());
            intent.putExtra(MapActivity.MAP_TITLE, getString(R.string.earth_quake_information_title));
            startActivity(intent);
        }
    }

    private class OnListItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

            if (null != getIntent().getExtras()) {
                if (getIntent().getSerializableExtra(ReportQueryInfoFragment.INTENT_RELATE_GOING) != null) {
                    EarthQuakeQuickReportModel model = (EarthQuakeQuickReportModel) getIntent().getSerializableExtra(ReportQueryInfoFragment.INTENT_RELATE_GOING);
                    EarthQuakeInfoModel data = quakeInfoLists.get(position);
                    CustomAlertDialog dialog = new CustomAlertDialog(EarthquakeLocalInfoActivity.this, "提示", "确定关联该地震", new MyDialogListener(model, data), CustomAlertDialog.DialogStyle.YESNO);
                    dialog.setNegativeButtonTitle("确定");
                    dialog.setPositiveButtonTitle("取消");
                    dialog.show();
                    return;
                }

                if (getIntent().getStringExtra(EarthQuakeQuickReportCommand.FLAG_FROM_EARTHQUAKE_ZQSB) != null) {
                    Intent intent = new Intent(EarthquakeLocalInfoActivity.this, QuickReportListActivity.class);
                    intent.putExtra(Constants.EARTH_QUAKE_LIST_CLICK, SessionManager.quakeInfoList.get(position));
                    startActivity(intent);
                    return;
                } else if (getIntent().getStringExtra(EarthQuakeFieldInvestigationCommand.FLAG_FROM_EARTHQUAKE_XCDC) != null) {
                    Intent intent = new Intent(EarthquakeLocalInfoActivity.this, EarthQuakeEmergencyInvestigationActivity.class);
                    intent.putExtra(Constants.EARTH_QUAKE_LIST_CLICK, SessionManager.quakeInfoList.get(position));
                    startActivity(intent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt(CustomViewInflater.REPORT_TITLE, R.string.emergency_destruction_report_title);
                    bundle.putString(CustomViewInflater.REPORT_COMFROM, UnUsualReportOperater.class.getName());
                    bundle.putString(CustomViewInflater.EVENTTYPE, "0004");
                    bundle.putString(UnUsualReportOperater.EARTHQUAKEID, String.valueOf(SessionManager.quakeInfoList.get(position).getId()));
                    UIHelper.startActivityWithExtra(CustomReportActivity1.class, bundle);
                }
            } else {
                Intent intent = new Intent(EarthquakeLocalInfoActivity.this, MapActivity.class);
                intent.putExtra(Constants.EARTH_QUAKE_LIST_CLICK, SessionManager.quakeInfoList.get(position));
                intent.putExtra(MapActivity.MAP_OPERATOR, QuakeInfoDistributionOperaterXtd.class.getName());
                intent.putExtra(MapActivity.MAP_TITLE, getString(R.string.earth_quake_information_title));
                startActivity(intent);
            }
        }
    }

    private class MyDialogListener implements CustomAlertDialog.OnCustomDialogListener {
        private EarthQuakeQuickReportModel model;
        private EarthQuakeInfoModel data;

        public MyDialogListener(EarthQuakeQuickReportModel model, EarthQuakeInfoModel data) {
            this.model = model;
            this.data = data;
        }

        @Override
        public void back(boolean result) {
            if (!result) {
                LoadingDialogUtil.show(EarthquakeLocalInfoActivity.this, "正在关联地震");
                EmergencyService.getInstance().getRelateEarthquake(model, data);
            }
        }
    }

}
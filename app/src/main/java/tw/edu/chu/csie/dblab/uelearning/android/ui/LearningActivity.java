package tw.edu.chu.csie.dblab.uelearning.android.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.chu.csie.dblab.uelearning.android.R;
import tw.edu.chu.csie.dblab.uelearning.android.config.Config;
import tw.edu.chu.csie.dblab.uelearning.android.database.DBProvider;
import tw.edu.chu.csie.dblab.uelearning.android.learning.ActivityManager;
import tw.edu.chu.csie.dblab.uelearning.android.learning.TargetManager;
import tw.edu.chu.csie.dblab.uelearning.android.server.UElearningRestClient;
import tw.edu.chu.csie.dblab.uelearning.android.ui.fragment.BrowseMaterialFragment;
import tw.edu.chu.csie.dblab.uelearning.android.ui.fragment.PlaceInfoFragment;
import tw.edu.chu.csie.dblab.uelearning.android.ui.fragment.PlaceMapFragment;
import tw.edu.chu.csie.dblab.uelearning.android.ui.fragment.StudyGuideFragment;
import tw.edu.chu.csie.dblab.uelearning.android.util.ErrorUtils;
import tw.edu.chu.csie.dblab.uelearning.android.util.HelpUtils;

public class LearningActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final int RESULT_MATERIAL = 507;

    ProgressDialog mProgress_activity_finish;
    StudyGuideFragment studyGuideFragment;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);

        // 清除已推薦的學習點
        DBProvider db = new DBProvider(LearningActivity.this);
        db.removeAll_recommand();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // 結束中畫面
        mProgress_activity_finish = new ProgressDialog(LearningActivity.this);
        mProgress_activity_finish.setMessage(getResources().getString(R.string.finishing_study_activity));
        mProgress_activity_finish.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress_activity_finish.setIndeterminate(true);
        // TODO: 設計成可中途取消的功能
        mProgress_activity_finish.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_MATERIAL) {
            if(resultCode == RESULT_OK){

                // 從教材頁面接收剛剛學過的是哪個標地
                Bundle bundle = data.getExtras();
                int learnedTId = bundle.getInt("LearnedPointId");
                if(Config.DEBUG_SHOW_MESSAGE) {
                    Toast.makeText(LearningActivity.this, "Back: "+learnedTId, Toast.LENGTH_SHORT).show();
                }
                // 繼續推薦下一個學習點
                studyGuideFragment.updateNextPoint(learnedTId);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_learning, menu);
        if(!Config.SCAN_BY_KEYIN_ENABLE) {
            menu.findItem(R.id.menu_keyin_tid).setVisible(false);
        }
        if(!Config.Earlier_FINISH_ACTIVITY_ENABLE) {
            menu.findItem(R.id.menu_finish_study_activity).setVisible(false);
        }
        if(Config.DEBUG_ACTIVITY) {
            menu.findItem(R.id.menu_inside_tester).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // QR Code 掃描
        if (id == R.id.menu_qr_scan) {

            TargetManager.enterPointByQRCode(LearningActivity.this);
        }
        // 輸入標的編號
        else if (id == R.id.menu_keyin_tid) {

            TargetManager.enterPointByDialog(LearningActivity.this);

        }
        // 結束學習活動
        else if (id == R.id.menu_finish_study_activity) {

            // 若尚未學習完成的話
            if(ActivityManager.getRemainingPointTotal(LearningActivity.this) > 0) {
                // 顯示確認訊息提示
                AlertDialog.Builder finishDBuilder = new AlertDialog.Builder(LearningActivity.this);
                finishDBuilder.setCancelable(true);
                finishDBuilder.setTitle(R.string.finish_study_activity);
                finishDBuilder.setMessage(R.string.finish_study_activity_unfinished_message);
                finishDBuilder.setPositiveButton(R.string.finish_study_activity, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishStudyActivity();
                    }
                });
                finishDBuilder.setNegativeButton(R.string.cancel, null);

                AlertDialog finishDialog = finishDBuilder.create();
                finishDialog.show();
            }
            else {
                finishStudyActivity();
            }

        }
        // 暫停學習活動
        else if (id == R.id.menu_pause_study_activity) {
            // 返回學習活動選擇頁面
            studyGuideFragment.stopUpdateUITask();
            finish();
        }
        else if (id == R.id.menu_about) {
            HelpUtils.showAboutDialog(LearningActivity.this);
            return true;
        }
        else if(id == R.id.menu_inside_tester) {
            Intent toTester = new Intent(LearningActivity.this, TesterActivity.class);
            startActivity(toTester);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() != 3) {
            moveTaskToBack(true);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * 結束學習活動
     */
    public void finishStudyActivity() {
        studyGuideFragment.stopUpdateUITask();
        mProgress_activity_finish.show();

        DBProvider db = new DBProvider(LearningActivity.this);
        // 取得目前使用者的登入階段Token
        String token = db.get_token();

        // 取得目前正在學習的活動編號
        int saId = db.get_activity_id();

        // 對伺服器通知學習活動已結束
        RequestParams finish_params = new RequestParams();
        try {
            UElearningRestClient.post("/tokens/" + URLEncoder.encode(token, HTTP.UTF_8) +
                    "/activitys/" + saId + "/finish",
                    finish_params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                            mProgress_activity_finish.show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            mProgress_activity_finish.dismiss();

                            String content = null;
                            try {
                                content = new String(responseBody, "UTF-8");
                                JSONObject response = new JSONObject(content);
                                JSONObject activityJson = response.getJSONObject("activity");

                                // 停止學習導引界面計時
                                //studyGuideFragment.stopUpdateUITask();

                                // 紀錄進資料庫
                                DBProvider db = new DBProvider(LearningActivity.this);
                                int saId = db.get_activity_id();
                                db.remove_enableActivity_inStudying_bySaId(saId);
                                db.removeAll_target();
                                db.removeAll_recommand();
                                db.removeAll_activity();

                                // 離開學習畫面
                                LearningActivity.this.finish();

                            } catch (UnsupportedEncodingException e) {
                                ErrorUtils.error(LearningActivity.this, e);
                            } catch (JSONException e) {
                                ErrorUtils.error(LearningActivity.this, e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            mProgress_activity_finish.dismiss();

                            // 此學習活動早已結束
                            if(statusCode == 405) {

                                // 紀錄進資料庫
                                DBProvider db = new DBProvider(LearningActivity.this);
                                int saId = db.get_activity_id();
                                db.removeAll_activity();
                                db.remove_enableActivity_inStudying_bySaId(saId);

                                // 離開學習畫面
                                LearningActivity.this.finish();
                            }
                            // 其他錯誤
                            else {
                                if(responseBody != null) {

                                    try {
                                        // TODO: 取得可用的學習活動失敗的錯誤處理
                                        String content = new String(responseBody, HTTP.UTF_8);
                                        if (Config.DEBUG_SHOW_MESSAGE) {
                                            Toast.makeText(LearningActivity.this,
                                                    "s: " + statusCode + "\n" + content,
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(LearningActivity.this, R.string.inside_error, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (UnsupportedEncodingException e) {
                                        ErrorUtils.error(LearningActivity.this, e);
                                    }
                                }
                                else {
                                    ErrorUtils.error(LearningActivity.this, error);
                                }
                            }

                        }
                    });
        } catch (UnsupportedEncodingException e) {
            ErrorUtils.error(LearningActivity.this, e);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment f;
            switch (position) {
                case 0:
                    f = studyGuideFragment.newInstance(position);
                    studyGuideFragment = (StudyGuideFragment)f;
                    break;
                case 1:
                    f = PlaceMapFragment.newInstance(position);
                    break;
                case 2:
                    f = PlaceInfoFragment.newInstance(position);
                    break;
                case 3:
                    f = BrowseMaterialFragment.newInstance(position);
                    break;
                default:
                    f = null;
            }
            return f;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            //return 4;
            // TODO: 因為其他功能尚未實作，故先隱藏
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.study_guide).toUpperCase(l);
                case 1:
                    return getString(R.string.place_map).toUpperCase(l);
                case 2:
                    return getString(R.string.place_info).toUpperCase(l);
                case 3:
                    return getString(R.string.browse_material).toUpperCase(l);
            }
            return null;
        }
    }

}

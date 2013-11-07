package com.yamatoya.hyper2channeler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.takaharabooks.lib.view.ViewLib;
import com.yamatoya.hyper2channeler.ad.ConbinateAdView;
import com.yamatoya.hyper2channeler.adapter.RssListAdapter;
import com.yamatoya.hyper2channeler.adapter.SpinnerAdapter;
import com.yamatoya.hyper2channeler.db.DB_Data;
import com.yamatoya.hyper2channeler.item.RssItem;
import com.yamatoya.hyper2channeler.pref.Preferences_Data;
import com.yamatoya.hyper2channeler.pref.SettingActivity;
import com.yamatoya.hyper2channeler.thread.RssParserTask;

public class Hyper2ch extends SherlockActivity
{
	static public final int INTENT_FAVORITE_LIST = 1000;
	static public final int INTENT_SETTING = 1001;
	static public final int MENU_ITEM_FAVORITE = Menu.FIRST;
	static public final int MENU_ITEM_SETTING = Menu.FIRST + 1;

	protected List<RssItem> mItems = null;
	protected List<RssItem> mCategoryItems = null;
	protected RssListAdapter mAdapter;
	protected SpinnerAdapter mSpinnerAdapter;
	protected ConbinateAdView m_csAd = null;
	protected Preferences_Data mPreferencesData;

	public DB_Data m_dbData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(R.style.Theme_Sherlock_Light);
		setContentView(R.layout.ac_top);

		// DB初期化
        m_dbData = new DB_Data(this);
        m_dbData.InitDB();
		mPreferencesData = new Preferences_Data(this);

		// 初期設定
		InitBackground();

		// 初回RSSデータ読み込み
		InitRssData();

		// 初期化
		InitEditBox();
		//InitCategory();
		//InitListView();
	}

    @Override
    public void onResume()
    {
    	if(null!=m_csAd)m_csAd.StartAd_Asta();
    	super.onResume();
    }

    @Override
    public void onPause()
    {
    	if(null!=m_csAd)m_csAd.StopAd_Asta();
    	super.onPause();
    }

    @Override
    public void onDestroy()
    {
    	if(null!=m_csAd)m_csAd.DestroyAd_Asta();
    	super.onDestroy();
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean ret = super.onCreateOptionsMenu(menu);

		MenuItem itemFavorite = menu.add(0, MENU_ITEM_FAVORITE, 0, "お気に入り");
		itemFavorite.setIcon(android.R.drawable.star_on);
		itemFavorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		MenuItem itemSetting = menu.add(0, MENU_ITEM_SETTING, 0, "設定");
		itemSetting.setIcon(android.R.drawable.ic_menu_preferences);
		itemSetting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return ret;
	}
	// メニューの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case MENU_ITEM_FAVORITE:// お気に入り画面
        	IntentFavoriteList();
            return true;
        case MENU_ITEM_SETTING:// 設定画面
        	IntentSetting();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	InitBackground();
    	if(null==mCategoryItems){
    		InitListView(mItems);
    	}else{
    		InitListView(mCategoryItems);
    	}
    }

    /*************************************
     * 背景の初期設定
     **************************************/
    public void InitBackground()
    {
    	int nKind = mPreferencesData.GetDispBackground();
    	RelativeLayout csLayout = (RelativeLayout)findViewById(R.id.acTopLayout);
    	switch(nKind)
    	{
    	case 0: default:
        	csLayout.setBackgroundColor(Color.argb(255, 239, 239, 255));
        	break;
    	case 1:
    		csLayout.setBackgroundResource(R.drawable.haikei03);
    		break;
    	}
    }

    /*************************************
     * 広告の初期設定
     **************************************/
    public void InitAd()
    {
    	Point sWindow = ViewLib.GetWindowSize(this);
    	int nIconNum = (int)Math.floor((double)sWindow.x / (double)ViewLib.ChangeDip(this, 60));

    	FrameLayout adFrame = (FrameLayout)findViewById(R.id.adLayout);
    	m_csAd = new ConbinateAdView(this, adFrame);
    	m_csAd.InitAd(nIconNum, 57);
    	m_csAd.StartAd_Asta();
    }

	public void InitRssData()
	{
		LoadRssItem(null, "");
	}

	/***********************************************************************************************
	 * 検索バーの初期設定<br>
	 * EditTextと検索ボタンのアイテムクリック時、データ取得
	 *
	 * @param	none
	 * @return	none
	 ***********************************************************************************************/
	public void InitEditBox()
	{
		EditText vwEdit = (EditText)findViewById(R.id.searchEditBox);
		vwEdit.clearFocus();
		vwEdit.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
		        //EnterKeyが押されたかを判定
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
				{
		            //ソフトキーボードを閉じる
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

		            //検索処理
					ImageButton vwButton = (ImageButton)findViewById(R.id.SearchButton);
					vwButton.performClick();

					return true;
				}
				return false;
			}
		});

		ImageButton vwButton = (ImageButton)findViewById(R.id.SearchButton);

		vwButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				// 広告表示
				if(null==m_csAd)
				{
					Spinner vwCategory = (Spinner)findViewById(R.id.CategorySpinner);
					vwCategory.setVisibility(View.GONE);
					FrameLayout vwLayout = (FrameLayout)findViewById(R.id.adLayout);
					vwLayout.setVisibility(View.VISIBLE);
					InitAd();
				}
				// 検索処理
				EditText vwEdit = (EditText)findViewById(R.id.searchEditBox);
				String strData = vwEdit.getText().toString();
				LoadRssItem(strData, null);
			}
		});
	}

	/***********************************************************************************************
	 * カテゴリ選択ボタンの初期設定<br>
	 * アイテムクリック時、データ取得
	 *
	 * @param	none
	 * @return	none
	 ***********************************************************************************************/
	private boolean bInitCategory = true;
	public void InitCategory()
	{
		if(!bInitCategory) return;
		bInitCategory = false;

		Spinner vwCategory = (Spinner)findViewById(R.id.CategorySpinner);
		if(null!=mItems && 0<mItems.size())
		{
			List<String> strList = new ArrayList<String>();
			for(int nIndex=0; nIndex<mItems.size(); nIndex++)
			{
				String strCategory = mItems.get(nIndex).getCategory().toString();
				if(!strList.contains(strCategory)){
					strList.add(strCategory.toString());
				}
			}
			Collections.sort(strList, new StrComp());
			strList.add(0, getString(R.string.btn_select_category));

			mSpinnerAdapter = new SpinnerAdapter(this, strList);
			mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			vwCategory.setAdapter(mSpinnerAdapter);
		}

		vwCategory.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long arg3)
			{
				if(0<position)
				{
					// 広告表示
					if(null==m_csAd)
					{
						ImageButton vwButton = (ImageButton)findViewById(R.id.SearchButton);
						vwButton.setVisibility(View.GONE);
						EditText vwEdit = (EditText)findViewById(R.id.searchEditBox);
						vwEdit.setVisibility(View.GONE);
						FrameLayout vwLayout = (FrameLayout)findViewById(R.id.adLayout);
						vwLayout.setVisibility(View.VISIBLE);
						InitAd();
					}
					// 検索処理
					LoadRssItem(null, mSpinnerAdapter.getItem(position));
				}else{
					//InitRssData();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	/***********************************************************************************************
	 * リストの初期設定<br>
	 * 2chから取得したデータを表示
	 *
	 * @param	none
	 * @return	none
	 ***********************************************************************************************/
	public void InitListView(List<RssItem> items)
	{
		ListView RssList = (ListView)findViewById(R.id.newsListView);
		RssList.removeAllViewsInLayout();

		RssList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
			{
				RssItem item = mAdapter.getItem(position);
				Intent2chViewer(item);
			}
		});

		// アダプタをリストビューにセットする
		mAdapter = new RssListAdapter(Hyper2ch.this, items, m_dbData);
		mAdapter.sort(new DateComp());
		RssList.setAdapter(mAdapter);

		EditText vwEdit = (EditText)findViewById(R.id.searchEditBox);
		vwEdit.clearFocus();
	}

	/***********************************************************************************************
	 * 2chのRSSをＷＥＢから取得します<br>
	 * 検索文字列かカテゴリのどちらか片方を指定します
	 *
	 * @param	strSearchText		検索文字列
	 * @param	strCategory			検索カテゴリ
	 * @return	none
	 ***********************************************************************************************/
	public void LoadRssItem(String strSearchText, String strCategory)
	{
		//if(null!=mItems) mItems.clear();
		// Webから読み込み
		if(null==mItems)
		{
			mItems = new ArrayList<RssItem>();

			RssParserTask task = new RssParserTask(Hyper2ch.this);
			if(null!=strSearchText) task.SetSearchText(strSearchText);
			if(null!=strCategory) task.SetCategory(strCategory);
			// 全てから
			String strUrl[] = getResources().getStringArray(R.array.rss_url);
			String strName[] = getResources().getStringArray(R.array.rss_site_name);
			task.execute(strUrl, strName);
		}
		// 取得済みのデータから絞込み
		else
		{
			mCategoryItems = new ArrayList<RssItem>();
			for(int nIndex=0; nIndex<mItems.size(); nIndex++)
			{
				RssItem item = mItems.get(nIndex);
				if(null!=strSearchText)
				{
					if(IsSearchText(strSearchText, item)){
						mCategoryItems.add(item);
					}
				}
				else if(null!=strCategory)
				{
					if(IsCategory(strCategory, item)){
						mCategoryItems.add(item);
					}
				}
			}
			// データ表示
			InitListView(mCategoryItems);
		}

	}
	/***********************************************************************************************
	 * 2chのRSSをランダムに1箇所のＷＥＢから取得します<br>
	 * 検索文字列かカテゴリのどちらか片方を指定します
	 *
	 * @param	strSearchText		検索文字列
	 * @param	strCategory			検索カテゴリ
	 * @return	none
	 ***********************************************************************************************/
	public void LoadRssItemRandomOneSite(String strSearchText, String strCategory)
	{
		RssParserTask task = new RssParserTask(Hyper2ch.this);
		if(null!=strSearchText) task.SetSearchText(strSearchText);
		if(null!=strCategory) task.SetCategory(strCategory);
		// ランダムにひとつから
		String strUrl[] = getResources().getStringArray(R.array.rss_url);
		String strName[] = getResources().getStringArray(R.array.rss_site_name);
		String strOneUrl[] = new String[1];
		String strOneName[] = new String[1];
		for(;;)
		{
			int nRand = (new Random()).nextInt(strName.length) - 1;
			int nID = nRand+1;
			SharedPreferences prefs		= PreferenceManager.getDefaultSharedPreferences(this);
	    	boolean bEnable   		= prefs.getBoolean(SettingActivity.KEY_RSS_ + nID, true);
	    	if(bEnable){
	    		strOneUrl[0] = strUrl[nRand];
				strOneName[0] = strName[nRand];
	    		break;
	    	}
		}
		task.execute(strOneUrl, strOneName);
	}

	protected boolean IsSearchText(String strSearchText, RssItem item)
	{
		boolean bRet = false;
		String strTitle = item.getTitle().toString();
		String strCategory = item.getCategory().toString();
		if(!strSearchText.equals("") && strTitle.contains(strSearchText)) bRet = true;
		if(!strSearchText.equals("") && strCategory.contains(strSearchText)) bRet = true;
		return bRet;
	}

	protected boolean IsCategory(String strSearchCategory, RssItem item)
	{
		boolean bRet = false;
		String strCategory = item.getCategory().toString();
		if(strSearchCategory.equals("")) bRet = true;
		if(!strSearchCategory.equals("") && strSearchCategory.equals(strCategory)) bRet = true;
		return bRet;
	}

	/***********************************************************************************************
	 * アダプタセット<br>
	 * 2chから取得中にアダプタをセット
	 *
	 * @param	adapter		RssListAdapterアダプタ
	 * @return	none
	 ***********************************************************************************************/
	public void SetAdapter(RssListAdapter adapter)
	{
		mAdapter = adapter;
	}

	/***********************************************************************************************
	 * RssItemを追加<br>
	 *
	 * @param	RssItem		RssItemデータ
	 * @return	none
	 ***********************************************************************************************/
	public void AddRssItem(List<RssItem> list)
	{
		mItems.addAll(list);
	}

    /*******************************************
     * ２ｃｈ（ＷＥＢ）を起動
     *******************************************/
    protected void Intent2chViewer(RssItem sItem)
    {
    	String strLink = sItem.getLink().toString();

    	Intent intent = new Intent();
        intent.setClassName(
        		getPackageName(),
        		getPackageName() + ".Web2chViewer");
        intent.putExtra("FAVRITE_NO", sItem.getFavoriteNo());
        intent.putExtra("SITE_NAME", sItem.getSiteName().toString());
        intent.putExtra("TITLE", sItem.getTitle().toString());
        intent.putExtra("DATE", sItem.getDate().toString());
        intent.putExtra("LINK", strLink);
        intent.putExtra("CATEGORY", sItem.getCategory().toString());
        if(null != strLink){
        	intent.putExtra("URL", strLink);
        }
        startActivityForResult(intent, INTENT_SETTING);
    }
    /*******************************************
     * 設定画面を起動
     *******************************************/
    protected void IntentFavoriteList()
    {
    	Intent intent = new Intent();
        intent.setClassName(
        		getPackageName(),
        		getPackageName() + ".FavoriteList");
        startActivityForResult(intent, INTENT_FAVORITE_LIST);
    }
    /*******************************************
     * 設定画面を起動
     *******************************************/
    protected void IntentSetting()
    {
    	Intent intent = new Intent();
        intent.setClassName(
        		getPackageName(),
        		getPackageName() + ".pref.SettingActivity");
        startActivityForResult(intent, INTENT_SETTING);
    }

    /*******************************************
     * 並び替え処理
     *******************************************/
    public class DateComp implements Comparator<RssItem>
    {
    	static final int DESC = -1;
        @Override
        public int compare(RssItem RItem1, RssItem RItem2)
        {
        	String s1 = RItem1.getDate().toString();
        	String s2 = RItem2.getDate().toString();

            if(s1.length()==0) return 1;
            if(s2.length()==0) return -1;

            return (s1.compareTo(s2) * DESC);
        }
    }

    /*******************************************
     * 並び替え処理
     *******************************************/
    public class StrComp implements Comparator<String>
    {
    	static final int ASC = 1;
        @Override
        public int compare(String str1, String str2)
        {
            if(str1.length()==0) return 1;
            if(str2.length()==0) return -1;

            return (str1.compareTo(str2) * ASC);
        }
    }

    /*************************************************
	 * 縦横固定の設定をActivityに適用する
	 * @param fixOrient 固定するならtrue，回転するように戻すならfalse
	 */
	protected void controlOrientationFix(boolean fixOrient) {
		if (fixOrient) {
			int ori = getResources().getConfiguration().orientation;
			if (ori == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
		}
	}

    /*******************************************
     * 初期読み込み設定
     *******************************************/
	public void InitSetting()
    {
    	// データ読み込み中は画面の向きは固定にする
    	controlOrientationFix(true);
    	// バックライトを点灯
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /***********************************************************
     * 関数名：FinishSetting
     * 概　要：設定終了処理
     * 引　数：<none>
     * 戻り値：<none>
     * 修正日：2011/12/10
     ***********************************************************/
    public void FinishSetting(){
		// バックライトを消灯
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 画面の向きの固定を解除
        controlOrientationFix(false);
    }

}

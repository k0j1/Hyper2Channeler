package com.yamatoya.hyper2channeler.thread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Xml;

import com.yamatoya.hyper2channeler.Hyper2ch;
import com.yamatoya.hyper2channeler.R;
import com.yamatoya.hyper2channeler.item.RssItem;
import com.yamatoya.hyper2channeler.pref.SettingActivity;

public class RssParserTask extends AsyncTask<String[], Integer, RssItem>
{
	private Hyper2ch mActivity;
	private List<RssItem> mList;
	private ProgressDialog mProgressDialog;

	private String mSearchText = "";
	private String mCategory = "";
	private boolean m_bCategory = false;

	// コンストラクタ
	public RssParserTask(Hyper2ch activity)
	{
		mActivity = activity;
		mCategory = "";
		mList = new ArrayList<RssItem>();
	}

	public void SetSearchText(String strSearchText)
	{
		mSearchText = strSearchText;
	}
	public void SetCategory(String strCategory)
	{
		mCategory = strCategory;
		m_bCategory = true;
	}

	// タスクを実行した直後にコールされる
	@Override
	protected void onPreExecute()
	{
		// ライトオン、画面固定に設定
		mActivity.InitSetting();
		// プログレスバーを表示する
		startProgresDlg(0);
	}

    // プログレスバー更新処理： UIスレッドで実行される
    @Override
    protected void onProgressUpdate(Integer... progress)
    {
    	if(null!=progress && null!=progress[0]) setProgressValue(progress[0]);
    }

	// バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
	@Override
	protected RssItem doInBackground(String[]... params) {
		RssItem result = null;
		try {
			// HTTP経由でアクセスし、InputStreamを取得する
			String strUrl[] = params[0];
			String strName[] = params[1];
			setProgresDlgMax(strName.length);
			for(int nIndex=0; nIndex<strUrl.length; nIndex++)
			{
				int nID = nIndex+1;
				SharedPreferences prefs		= PreferenceManager.getDefaultSharedPreferences(mActivity);
		    	boolean bEnable   		= prefs.getBoolean(SettingActivity.KEY_RSS_ + nID, true);
		    	// 設定画面で有効の場合のみ取得
		    	if(bEnable)
		    	{
					URL url = new URL(strUrl[nIndex]);
					InputStream is = url.openConnection().getInputStream();
					parseXml(is, strName[nIndex]);
		    	}
				publishProgress(nIndex+1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ここで返した値は、onPostExecuteメソッドの引数として渡される
		return result;
	}

	// メインスレッド上で実行される
	@Override
	protected void onPostExecute(RssItem result)
	{
		dismissProgresDlg();

		// ライト自動、画面向き自動に設定
		mActivity.FinishSetting();

		// データ表示
		mActivity.AddRssItem(mList);
		mActivity.InitCategory();
		mActivity.InitListView(mList);
	}

	/***********************************************************************************************
	 * XMLをパースする
	 ***********************************************************************************************/
	public void parseXml(InputStream is, String strName) throws IOException, XmlPullParserException
	{
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			RssItem currentItem = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						if (tag.equals("entry") || tag.equals("item")) {
							currentItem = new RssItem();
						} else if (currentItem != null) {
							if (tag.equals("title")) {
								currentItem.setTitle(parser.nextText());
							} else if (tag.equals("issued") || tag.equals("date")) {
								//SimpleDateFormat df2 = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", java.util.Locale.US);
								//Date d = df2.parse(parser.nextText());
								currentItem.setDate(parser.nextText());
							} else if(tag.equals("link")){
								String strLink = "";
								for(int i=0; i<parser.getAttributeCount(); i++)
								{
									if(parser.getAttributeName(i).equals("href")){
										strLink = parser.getAttributeValue(i);
									}
			    				}
								if(strLink.equals(""))
								{
									strLink = parser.nextText();
									//strLink = parser.getAttributeValue(null, "href");
								}
								currentItem.setLink(strLink);
							} else if(tag.equals("subject")){
								currentItem.setCategory(parser.nextText());
							}
						}
						break;
					case XmlPullParser.END_TAG:
						tag = parser.getName();
						if (tag.equals("entry") || tag.equals("item"))
						{
							boolean bSearch = false;
							boolean bCategory = false;
							if(IsSearchText(currentItem.getCategory().toString(), currentItem.getTitle().toString()) ){
								bSearch = true;
							}
							if(IsCategory(currentItem.getCategory().toString()))
							{
								bCategory = true;
							}
							// 条件一致していれば追加
							if(bSearch || bCategory){
								RssItem sFavItem = mActivity.m_dbData.GetFavoriteData(currentItem.getLink().toString());
								if(null!=sFavItem) currentItem.setFavoriteNo(sFavItem.getFavoriteNo());
								currentItem.setSiteName(strName);
								mList.add(currentItem);
							}
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return mList;
	}

	private boolean IsSearchText(String strTitle, String strCategory)
	{
		boolean bRet = false;
		if(!mSearchText.equals("") && strTitle.contains(mSearchText)) bRet = true;
		if(!mSearchText.equals("") && strCategory.contains(mSearchText)) bRet = true;
		return bRet;
	}

	private boolean IsCategory(String strCategory)
	{
		boolean bRet = false;
		if(m_bCategory)
		{
			if(mCategory.equals("")) bRet = true;
			if(!mCategory.equals("") && mCategory.equals(strCategory)) bRet = true;
		}
		return bRet;
	}

	//////////////////////////////////////////////////////
    // 読み込み中ダイアログ開始
    //////////////////////////////////////////////////////
    public void startProgresDlg(int nNum)
    {
    	if(null!=mProgressDialog) mProgressDialog.dismiss();
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle("Now Loading...");
        mProgressDialog.setMax(nNum);
        mProgressDialog.setMessage(mActivity.getString(R.string.PROGRESS_MSG_LOADING));
        //mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
    public void setProgresDlgMax(int nNum)
    {
        mProgressDialog.setMax(nNum);
    }

    //////////////////////////////////////////////////////
    // 読み込み中ダイアログ削除
    //////////////////////////////////////////////////////
    protected void dismissProgresDlg()
    {
		if(mProgressDialog != null && mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
    }

    //////////////////////////////////////////////////////
    // プログレスバーの更新
    //////////////////////////////////////////////////////
    private void setProgressValue(Integer progress)
    {
    	if(null!=mProgressDialog){
    		mProgressDialog.setProgress(progress);
    	}
    }

}
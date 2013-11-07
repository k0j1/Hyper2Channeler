package com.yamatoya.hyper2channeler;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.yamatoya.hyper2channeler.item.RssItem;

public class FavoriteList extends Hyper2ch
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}
	// メニューの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return false;
    }

	/***********************************************************************************************
	 * 2chのRSSをＷＥＢから取得します<br>
	 * 検索文字列かカテゴリのどちらか片方を指定します
	 *
	 * @param	strSearchText		検索文字列
	 * @param	strCategory			検索カテゴリ
	 * @return	none
	 ***********************************************************************************************/
    @Override
	public void LoadRssItem(String strSearchText, String strCategory)
	{
		// Webから読み込み
		if(null==mItems)
		{
			mItems = m_dbData.GetFavoriteData();
			if(null!=mItems)
			{
				//AddRssItem(mItems);
				InitCategory();
				InitListView(mItems);
			}
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

}

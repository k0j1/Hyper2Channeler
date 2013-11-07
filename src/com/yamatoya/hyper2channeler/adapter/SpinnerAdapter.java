package com.yamatoya.hyper2channeler.adapter;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

/***********************************************************************************************
 * RSS情報アイテムクラス
 ***********************************************************************************************/
public class SpinnerAdapter extends ArrayAdapter<String>
{
	List<String> mCategory;

	// コンストラクタ
	public SpinnerAdapter(Context context, List<String> objects)
	{
		super(context, android.R.layout.simple_spinner_item, objects);
	}

	public void add(int nPos, String strCategory)
	{
		add(nPos, strCategory);
	}

	// 未使用
	/***********************************************************************************************
	 * カテゴリの取得・セット
	 ***********************************************************************************************/
	public String getCategory(int position) {
		return (String)getItem(position);
	}
	public void addCategory(String strCategory)
	{
		if(!mCategory.contains(strCategory)){
			mCategory.add(strCategory);
		}
	}
	public void addCategory(int nPos, String strCategory)
	{
		if(!mCategory.contains(strCategory)){
			mCategory.add(nPos, strCategory);
		}
	}
}
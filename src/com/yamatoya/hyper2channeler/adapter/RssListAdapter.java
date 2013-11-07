package com.yamatoya.hyper2channeler.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yamatoya.hyper2channeler.R;
import com.yamatoya.hyper2channeler.db.DB_Data;
import com.yamatoya.hyper2channeler.item.RssItem;


public class RssListAdapter extends ArrayAdapter<RssItem>
{
	protected List<RssItem> mItems;
	private LayoutInflater mInflater;
	private TextView mTitle;
	private TextView mDate;
	private DB_Data m_dbData;

	// コンストラクタ
	public RssListAdapter(Context context, List<RssItem> objects, DB_Data dbData) {
		super(context, 0, objects);
		mItems = objects;
		m_dbData = dbData;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/***********************************************************************************************
	 * アダプタにRSS情報アイテムを追加<br>
	 *
	 * @param	RssItem		RSS情報のアイテム
	 * @return	none
	 ***********************************************************************************************/
	public void addItem(RssItem item)
	{
		mItems.add(item);
	}

	@Override
	public int getCount()
	{
		return mItems.size();
	}

	// 1行ごとのビューを生成する
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_rss_row, null);
		}

		// 現在参照しているリストの位置からItemを取得する
		RssItem item = this.getItem(position);
		if (item != null) {
			// Itemから必要なデータを取り出し、それぞれTextViewにセットする
			String link = item.getLink().toString();
			String title = item.getTitle().toString();
			String strSiteName = item.getSiteName().toString();
			//CharSequence html = Html.fromHtml(String.format("%s", link, title));
			mTitle = (TextView) view.findViewById(R.id.item_title);
			mTitle.setText(title);
			// お気に入り、日付、サイト名の列を追加
			boolean bFavorite = m_dbData.IsFavoriteData(item.getLink().toString());
			String strStar = "";
			if(bFavorite) strStar = "<font color=#cccc00>★</font>";
			String date = item.getDate().toString();
			mDate = (TextView) view.findViewById(R.id.item_date);
			CharSequence htmlDate = Html.fromHtml(String.format("%s%s年%s月%s日 %s　<font color=green>%s</font>",
				strStar, date.substring(0, 4), date.substring(5, 7), date.substring(8, 10), date.substring(11, 19), strSiteName)
			);
			mDate.setText(htmlDate);
		}
		return view;
	}
}
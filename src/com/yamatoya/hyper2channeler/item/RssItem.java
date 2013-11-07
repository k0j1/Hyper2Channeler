package com.yamatoya.hyper2channeler.item;

/***********************************************************************************************
 * RSS情報アイテムクラス
 ***********************************************************************************************/
public class RssItem
{
	// お気に入り番号
	private int m_nFavoriteNo;
	// 記事のサイト名
	private CharSequence mSiteName;
	// 記事のタイトル
	private CharSequence mTitle;
	// 記事の日付
	private CharSequence mDate;
	// 記事のリンク
	private CharSequence mLink;
	// 記事のカテゴリ
	private CharSequence mCategory;

	// コンストラクタ
	public RssItem() {
		m_nFavoriteNo = 0;
		mTitle = "";
		mDate = "";
		mLink = "";
		mCategory = "";
	}

	/***********************************************************************************************
	 * お気に入り番号の取得・セット
	 ***********************************************************************************************/
	public int getFavoriteNo() {
		return m_nFavoriteNo;
	}
	public void setFavoriteNo(int nFavoriteNo) {
		m_nFavoriteNo = nFavoriteNo;
	}

	/***********************************************************************************************
	 * 日付の取得・セット
	 ***********************************************************************************************/
	public CharSequence getDate() {
		return mDate;
	}
	public void setDate(CharSequence date) {
		mDate = date;
	}

	/***********************************************************************************************
	 * タイトルの取得・セット
	 ***********************************************************************************************/
	public CharSequence getTitle() {
		return mTitle;
	}
	public void setTitle(CharSequence title) {
		mTitle = title;
	}

	/***********************************************************************************************
	 * タイトルの取得・セット
	 ***********************************************************************************************/
	public CharSequence getSiteName() {
		return mSiteName;
	}
	public void setSiteName(CharSequence name) {
		mSiteName = name;
	}

	/***********************************************************************************************
	 * リンクの取得・セット
	 ***********************************************************************************************/
	public CharSequence getLink() {
		return mLink;
	}
	public void setLink(CharSequence link) {
		mLink = link;
	}

	/***********************************************************************************************
	 * カテゴリの取得・セット
	 ***********************************************************************************************/
	public CharSequence getCategory() {
		return mCategory;
	}
	public void setCategory(CharSequence category) {
		mCategory = category;
	}
}
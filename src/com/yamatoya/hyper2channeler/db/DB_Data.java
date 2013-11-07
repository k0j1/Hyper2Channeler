package com.yamatoya.hyper2channeler.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.takaharabooks.lib.db.DB_Base;
import com.yamatoya.hyper2channeler.item.RssItem;

public class DB_Data extends DB_Base
{
	//**********************定数定義**********************
	//***** 数値
	// Version
	public static final int DB_VER = 1;
	//***** 文字列
	// DB名
	public static final String	DB_NAME = "Pocket2ch.db";
	// テーブル名
	public static final String	DB_FAVORITE_TBL = "tb_favorite_data";
	// View名
	public static final String	DB_VIEW = "vw_data";
	// 列名
	public static final String	DB_COL_FAVORITE_ID			= "favorite_id";
	public static final String	DB_COL_FAVORITE_SITE_NAME	= "favorite_site_name";
	public static final String	DB_COL_FAVORITE_TITLE		= "favorite_title";
	public static final String	DB_COL_FAVORITE_DATE		= "favorite_date";
	public static final String	DB_COL_FAVORITE_LINK 		= "favorite_link";
	public static final String	DB_COL_FAVORITE_CATEGORY	= "favorite_category";
	// クエリ
	public static final String	DB_QUERY_SELECT_ALL_DATA =
		"select * from " + DB_FAVORITE_TBL;
	public static final String	DB_QUERY_SELECT_ONE_DATA =
		"select * from " + DB_FAVORITE_TBL + " where " + DB_COL_FAVORITE_ID + " = %d";
	public static final String	DB_QUERY_SELECT_ONE_DATA_BY_LINK =
			"select * from " + DB_FAVORITE_TBL + " where " + DB_COL_FAVORITE_LINK + " = \"%s\"";
	//**********************定数定義**********************

	private Context m_csContext;
	//private SQLiteDatabase	m_csDB;

	// コンストラクタ
	public DB_Data(Context context) {
		super(context, DB_NAME, DB_VER);
		m_csContext = context;
	}

    //データベースオブジェクトを取得する（データベースにアクセスするとDBがない場合は作成される。）
	public void InitDB(){
		InitDBBase(DB_MODE_WRITE);
	}

	// @Override onCreate
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		CreateTBLForFavoriteData(db);
    }

	// @Override onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//Log.v("MyDBOpenHelper", "onUpgrade_Start");
		//Log.v("MyDBOpenHelper", "onUpgrade_End");
	}

	/*****************************************
	 *  フォルダ情報テーブルを作成
	 ****************************************/
	public void CreateTBLForFavoriteData(SQLiteDatabase db)
	{
		try{
	        String sql = String.format(
	        		"create table %s (" +
	    	        " %s integer primary key, %s text, %s text, " +
	    	        " %s text, %s text, %s text " +
	        		")",
	        		DB_FAVORITE_TBL,
	        		DB_COL_FAVORITE_ID,  	DB_COL_FAVORITE_SITE_NAME,
	        		DB_COL_FAVORITE_TITLE,	DB_COL_FAVORITE_DATE,
	        		DB_COL_FAVORITE_LINK,	DB_COL_FAVORITE_CATEGORY
	        );
	        db.execSQL(sql);
    		Log.v("CreateTBLForFolder", "SUCCESS");
    	}catch(SQLException e){
    		Log.v("CreateTBLForFolder", e.getMessage());
			return;
		}
	}

	/*****************************************
	 *  ラベル名を取得
	 ****************************************/
	public RssItem GetFavoriteData(int nFavoriteID)
	{
		Cursor csCur;
		// ＤＢからデータの取得
		try{
			String strQuery = String.format( DB_QUERY_SELECT_ONE_DATA, nFavoriteID );
			Log.v("DB_Data", strQuery);
	    	csCur = readDB(strQuery);
		}catch(Exception e){
			// 例外時は何もしない
			e.printStackTrace();
			return null;
		}

		csCur.moveToFirst();
		RssItem sItem = new RssItem();
		sItem.setFavoriteNo(csCur.getInt(0));
		sItem.setSiteName(csCur.getString(1));
		sItem.setTitle(csCur.getString(2));
		sItem.setDate(csCur.getString(3));
		sItem.setLink(csCur.getString(4));
		sItem.setCategory(csCur.getString(5));
    	csCur.moveToNext();
		csCur.close();

        return sItem;
	}

	/*****************************************
	 *  ラベル名を取得
	 ****************************************/
	public RssItem GetFavoriteData(String strLink)
	{
		Cursor csCur;
		// ＤＢからデータの取得
		try{
			String strQuery = String.format( DB_QUERY_SELECT_ONE_DATA_BY_LINK, strLink );
			Log.v("DB_Data", strQuery);
	    	csCur = readDB(strQuery);
		}catch(Exception e){
			// 例外時は何もしない
			e.printStackTrace();
			return null;
		}

		csCur.moveToFirst();
		RssItem sItem = new RssItem();
		sItem.setFavoriteNo(csCur.getInt(0));
		sItem.setSiteName(csCur.getString(1));
		sItem.setTitle(csCur.getString(2));
		sItem.setDate(csCur.getString(3));
		sItem.setLink(csCur.getString(4));
		sItem.setCategory(csCur.getString(5));
    	csCur.moveToNext();
		csCur.close();

        return sItem;
	}

	/*****************************************
	 *  ラベル名を取得
	 ****************************************/
	public List<RssItem> GetFavoriteData()
	{
		Cursor csCur;
		// ＤＢからデータの取得
		try{
			String strQuery = DB_QUERY_SELECT_ALL_DATA;
			Log.v("DB_Data", strQuery);
	    	csCur = readDB(strQuery);
		}catch(Exception e){
			// 例外時は何もしない
			e.printStackTrace();
			return null;
		}

		int nDataNum = csCur.getCount();

		List<RssItem> sItems = new ArrayList<RssItem>();
		csCur.moveToFirst();
		for(int nIndex=0; nIndex<nDataNum; nIndex++)
		{
			RssItem sItem = new RssItem();
			sItem.setFavoriteNo(csCur.getInt(0));
			sItem.setSiteName(csCur.getString(1));
			sItem.setTitle(csCur.getString(2));
			sItem.setDate(csCur.getString(3));
			sItem.setLink(csCur.getString(4));
			sItem.setCategory(csCur.getString(5));
			sItems.add(sItem);
	    	csCur.moveToNext();
		}
		csCur.close();

        return sItems;
	}

	/*****************************************
	 *  ラベル名を取得
	 ****************************************/
	public boolean IsFavoriteData(int nFavoriteID)
	{
		boolean bRet = false;

		Cursor csCur;
		// ＤＢからデータの取得
		try{
			String strQuery = String.format( DB_QUERY_SELECT_ONE_DATA, nFavoriteID );
			Log.v("DB_Data", strQuery);
	    	csCur = readDB(strQuery);
		}catch(Exception e){
			// 例外時は何もしない
			return bRet;
		}

		int nDataNum = csCur.getCount();
		csCur.close();

		if(0<nDataNum) bRet = true;

        return bRet;
	}

	/*****************************************
	 *  ラベル名を取得
	 ****************************************/
	public boolean IsFavoriteData(String strLink)
	{
		boolean bRet = false;

		Cursor csCur;
		// ＤＢからデータの取得
		try{
			String strQuery = String.format( DB_QUERY_SELECT_ONE_DATA_BY_LINK, strLink );
			Log.v("DB_Data", strQuery);
	    	csCur = readDB(strQuery);
		}catch(Exception e){
			// 例外時は何もしない
			return bRet;
		}

		int nDataNum = csCur.getCount();
		csCur.close();

		if(0<nDataNum) bRet = true;

        return bRet;
	}

	/*****************************************
	 *  データを挿入
	 ****************************************/
	public int InsertFavoriteDataInfo(RssItem sItem)
	{
		int nNo = 0;
		// ないでーたのみ追加する
		if(!IsFavoriteData(sItem.getLink().toString()))
		{
			nNo = GetMaxFavoriteID() + 1;
			ContentValues cv = new ContentValues();
			cv.put(DB_COL_FAVORITE_ID, nNo);
			cv.put(DB_COL_FAVORITE_SITE_NAME, sItem.getSiteName().toString());
			cv.put(DB_COL_FAVORITE_TITLE, sItem.getTitle().toString());
			cv.put(DB_COL_FAVORITE_DATE, sItem.getDate().toString());
			cv.put(DB_COL_FAVORITE_LINK, sItem.getLink().toString());
			cv.put(DB_COL_FAVORITE_CATEGORY, sItem.getCategory().toString());
			InsertDB(m_csContext, cv, DB_FAVORITE_TBL);
		}

		return nNo;
	}


	/*****************************************
	 *  ＤＢからラベル別ファイル削除
	 ****************************************/
	public void DeleteFavoriteData(String strLink)
	{
		String strWhere = String.format("%s = \"%s\"", DB_COL_FAVORITE_LINK, strLink);
		DeleteDB(m_csContext, DB_FAVORITE_TBL, strWhere);
	}

	/*****************************************
	 *  ラベルの最大のインデックスＩＤを取得
	 ****************************************/
	public int GetMaxFavoriteID(){
		return GetMax(DB_COL_FAVORITE_ID, DB_FAVORITE_TBL);
	}

	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	// ＤＢ上に入れるデフォルトの値

	//private int[] m_stnDefxxx = {
	//};

}

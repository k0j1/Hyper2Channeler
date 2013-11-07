package com.yamatoya.hyper2channeler.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.yamatoya.hyper2channeler.R;

public class SettingActivity extends PreferenceActivity
{
	//定数/////////////////////////////////////////////////////////////////
	public static final String KEY_DISP_BACKGROUND = "key_disp_background";
	public static final String KEY_RSS_ = "key_rss_";
	///////////////////////////////////////////////////////////////////

	public Preferences_Data mPreferencesData;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);

		mPreferencesData = new Preferences_Data(this);

		// 表示設定
		InitDispSetting();

		// 取得先の表示設定
		String strName[] = getResources().getStringArray(R.array.rss_site_name);
		for(int nCnt=0; nCnt<strName.length; nCnt++){
			int nRssID = nCnt+1;
			InitCKButton(KEY_RSS_ + nRssID, strName[nCnt]);
		}
	}

	// ボタンの設定
	public void InitDispSetting()
	{
		Preference csList = (Preference)findPreference(KEY_DISP_BACKGROUND);
		csList.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference arg0)
			{
				ShowDlgForDispBackground();
				return false;
			}
		});
		SetDispBackgroundSummary();
	}
	public void SetDispBackgroundSummary()
	{
		final CharSequence[] str_items = {
				getString(R.string.DLG_DISP_SETTING_BACKGROUND_1),	// デフォルト
				getString(R.string.DLG_DISP_SETTING_BACKGROUND_2)	// グラデーション（緑）
		};
		// サマリに選択している方を表示
		int nSelectItem = mPreferencesData.GetDispBackground();
		String strSummary = str_items[nSelectItem].toString();
		Preference csList = (Preference)findPreference(KEY_DISP_BACKGROUND);
		csList.setSummary(strSummary);
	}


	// ボタンの設定
	public void InitCKButton(CharSequence strKEY, String strName)
	{
		// チェックボックス設定のインスタンスを、キーを基に取得する
		CheckBoxPreference csCK = (CheckBoxPreference)findPreference(strKEY);
		if(null == csCK) return;

		// タイトルセット
		csCK.setTitle(strName);
		// デフォルトセット
		csCK.setDefaultValue(true);

		// リスナーを設定する
		csCK.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		        if (((Boolean)newValue).booleanValue()) {
		        	// none
		        } else {
		        	// none
		        }
		        // 変更を適用するために true を返す
		        return true;
		    }
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/***********************************************************
     * 関数名：ShowDlgForDispBackground
     * 概　要：表示背景を選択するダイアログ
     * 引　数：<none>
     * 戻り値：表示背景を選択するダイアログ
     * 修正日：2012/02/24
     ***********************************************************/
    public void ShowDlgForDispBackground()
    {
    	final CharSequence[] str_items = {
    			getString(R.string.DLG_DISP_SETTING_BACKGROUND_1),	// デフォルト
    			getString(R.string.DLG_DISP_SETTING_BACKGROUND_2)	// グラデーション（緑）
    	};

    	int nSelectItem = mPreferencesData.GetDispBackground();
		Dialog dlg = new AlertDialog.Builder(this)
        .setSingleChoiceItems(str_items, nSelectItem, new DialogInterface.OnClickListener(){
        	@Override
        	public void onClick(DialogInterface dialog, int which)
        	{
        		mPreferencesData.PutDispBackground(which);
        		SetDispBackgroundSummary();
        		dialog.dismiss();
        	}
        })
        .create();

		dlg.show();
    }


}
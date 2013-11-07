/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.yamatoya.hyper2channeler.ad;

import jp.co.imobile.android.AdIconView;
import jp.co.imobile.android.AdIconViewParams;
import jp.maru.mrd.IconCell;
import jp.maru.mrd.IconLoader;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.takaharabooks.lib.view.ViewLib;
import com.yamatoya.hyper2channeler.R;
import com.yamatoya.hyper2channeler.pref.Preferences_Data;

public class ConbinateAdView
{
	// Admaker定数===========================================================
	// アイコン型広告
    //public static final String MEDIA_CODE = "com.takaharabook2ZIo";
	static public final int IMOBILE_MEDIA_ID = 70377;
	static public final int IMOBILE_SPOT_ID = 139480;

	static public final int AD_K0J1 = 0;
	static public final int AD_YAMATOYA = 1;
	//=======================================================================

    // 広告の種類
    int m_nAdKindFlag;

	// iMobile
    private AdIconView m_iMobile;
	//AdView m_csAdMob = null;

	// アスタ
    public LinearLayout iconAdView;
	private IconLoader<Integer> mIconLoader = null;

	Activity m_csAc;

	ViewGroup m_csLayout;
	FrameLayout m_csFrameLayout;
	Preferences_Data mPrefData;

	// コンストラクタ
	// プログレスバーを初期化
	public ConbinateAdView(Activity ac, FrameLayout layout){
		m_csAc = ac;
		m_csFrameLayout = layout;
		mPrefData = new Preferences_Data(ac);
	}

    /*************************************
     * 広告初期化
     **************************************/
	public void InitAd(int nIconNum, int nIconSize)
	{
    	// 広告の設定
		int nAd = mPrefData.GetAdChange();
    	switch(nAd)
    	{
    	case AD_K0J1:
    		InitIMobile(nIconNum, nIconSize);
    		mPrefData.PutAdChange(AD_YAMATOYA);
    		break;
    	case AD_YAMATOYA:
	    	InitAd_Asta();
    		mPrefData.PutAdChange(AD_K0J1);
	    	break;
    	}
	}

    /*************************************
     * 広告初期化（アスタ）
     **************************************/
    //public IconAd(final Activity act, final LinearLayout layout, final IconLoader<Integer> iconLoader) {
	public void InitAd_Asta()
	{
        initIconAd();
        m_csFrameLayout.addView(iconAdView);
	}
	public void StartAd_Asta(){ if(null!=mIconLoader) mIconLoader.startLoading(); };
	public void StopAd_Asta(){ if(null!=mIconLoader) mIconLoader.stopLoading(); };
	public void DestroyAd_Asta(){ mIconLoader = null; };

    /*************************************
     * 広告設置（共通）
     **************************************/
	public void SetLayout(View adView){
		RelativeLayout.LayoutParams csBackParams;
		csBackParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewLib.ChangeDip(m_csAc, 80));
		//csBackParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adView.setLayoutParams(csBackParams);
		m_csFrameLayout.addView(adView);
	}

    /*************************************
     * 広告初期化（AdMob）
     **************************************/
/*    protected void InitAdMob()
    {
		m_csAdMob = new AdView(m_csAc, AdSize.BANNER, "a151321a3f6150b");

		// 設置
		SetLayout(m_csAdMob);

    	AdRequest adRequest = new AdRequest();
    	m_csAdMob.loadAd(adRequest);
    	m_csAdMob.setAdListener(new AdListener() {
			@Override
			public void onDismissScreen(Ad arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onLeaveApplication(Ad arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPresentScreen(Ad arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onReceiveAd(Ad arg0) {
				// TODO Auto-generated method stub
			}
        });

    	m_csAdMob.setVisibility(View.VISIBLE);
    }
*/
	public void InitIMobile(int nIconNum, int nIconSize)
	{
		AdIconViewParams params = new AdIconViewParams(m_csAc);
		params.setIconSize(nIconSize);
		m_iMobile = AdIconView.create(m_csAc, IMOBILE_MEDIA_ID, IMOBILE_SPOT_ID , nIconNum, params);
		SetLayout(m_iMobile);
		m_iMobile.start();
	}

    protected void DestroyAd(){
    	//m_csAdMob.destroy();
    }
    protected void StopAd(){
    }
    protected void RestartAd(){
    }

    /**
     * アイコン型広告初期化
     */
    private void initIconAd()
    {
        LinearLayout.LayoutParams layoutParams;

    	// 画面の向きを取得
        int nOrient = ViewLib.GetWindowOrientation(m_csAc);

        // アイコン型広告識別コード設定
        mIconLoader = new IconLoader<Integer>(IconAd.MEDIA_CODE_YAMATO, m_csAc);
        mIconLoader.setRefreshInterval(30);

        // レイアウト設定
        iconAdView = new LinearLayout(m_csAc);
        //if(nOrient == Configuration.ORIENTATION_PORTRAIT)
        {
        	iconAdView.setOrientation(LinearLayout.HORIZONTAL);
        	iconAdView.setGravity(Gravity.CENTER_HORIZONTAL);
            iconAdView.setPadding(0, ViewLib.ChangeDip(m_csAc, 5), 0, 0);
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewLib.ChangeDip(m_csAc, 80));
        }
        //else{
        //	iconAdView.setOrientation(LinearLayout.VERTICAL);
        //	iconAdView.setGravity(Gravity.CENTER_VERTICAL);
        //	layoutParams = new LinearLayout.LayoutParams(ViewLib.ChangeDip(m_csAc, 80), LinearLayout.LayoutParams.MATCH_PARENT );
        //}
        iconAdView.setLayoutParams(layoutParams);

        // 画面の大きさに合わせてアイコンを設置します
        Point sWinSize = ViewLib.GetWindowSize(m_csAc);
        int nIconNum = (int)(sWinSize.x / ViewLib.ChangeDip(m_csAc, 80));
        //if(nOrient == Configuration.ORIENTATION_LANDSCAPE){
        //	nIconNum = (int)((sWinSize.y) / ViewLib.ChangeDip(m_csAc, 80)) - 1;
        //}
        // アイコン設置
        for (int ii=0; ii<nIconNum; ii++)
        {
            final LinearLayout iconAdSubView = new LinearLayout(m_csAc);
            LinearLayout.LayoutParams layoutSubParams =
            		new LinearLayout.LayoutParams(ViewLib.ChangeDip(m_csAc, 80), ViewLib.ChangeDip(m_csAc, 80), 1.0f);
            iconAdSubView.setLayoutParams(layoutSubParams);

            // ここでIconAd.javaを呼び出します
            newAd_Asta(iconAdSubView, mIconLoader);

            // 設定したアイコンデータをViewに追加する
            iconAdView.addView(iconAdSubView);
        }

    }

    /*************************************
     * アイコン広告の生成（アスタ）
     **************************************/
    //public IconAd(final Activity act, final LinearLayout layout, final IconLoader<Integer> iconLoader) {
	public void newAd_Asta(LinearLayout csLayout, final IconLoader<Integer> iconLoader)
	{
        try {
            // ここでicon.xmlファイルを読み込んで来てます
            View view = m_csAc.getLayoutInflater().inflate(R.layout.icon, null);
            if (view instanceof IconCell) {
                  ((IconCell)view).addToIconLoader(iconLoader);
            }
            csLayout.addView(view);
        } catch(Exception e) {
               e.printStackTrace();

        }
	}

}
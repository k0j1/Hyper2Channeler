package com.yamatoya.hyper2channeler.ad;

import jp.maru.mrd.IconCell;
import jp.maru.mrd.IconLoader;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.yamatoya.hyper2channeler.R;

public class IconAd
{
	// アイコン型広告
    public static final String MEDIA_CODE_YAMATO = "ast00197zjkaqf23n5ab"; //←メモした識別コードに変えてください

	private Activity mActivity;
	private LinearLayout mLayout;
	private IconLoader<Integer> mIconLoader;

    public IconAd(final Activity act, final LinearLayout layout, final IconLoader<Integer> iconLoader) {
        mActivity = act;
        mLayout = layout;
        mIconLoader = iconLoader;
        adInit();
    }

    /**
     * 広告初期化
     */
    private void adInit()
    {
    	try {
            // ここでicon.xmlファイルを読み込んで来てます
            View view = mActivity.getLayoutInflater().inflate(R.layout.icon, null);
            if (view instanceof IconCell) {
                  ((IconCell)view).addToIconLoader(mIconLoader);
            }

            mLayout.addView(view);
        } catch(Exception e) {
               e.printStackTrace();

        }
    }
}
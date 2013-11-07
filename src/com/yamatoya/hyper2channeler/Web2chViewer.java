package com.yamatoya.hyper2channeler;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.takaharabooks.lib.view.ViewLib;
import com.yamatoya.hyper2channeler.ad.ConbinateAdView;
import com.yamatoya.hyper2channeler.db.DB_Data;
import com.yamatoya.hyper2channeler.item.RssItem;

public class Web2chViewer extends Activity
{
	private ConbinateAdView m_csAd;
    private ProgressBar mProgBar;
    private DB_Data m_dbData;

	private WebView m_csWeb;
	private RssItem m_sItem;
	private String m_strStartUrl = null;

	// プリファレンス
	//private Preferences_Data m_SettingData;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.ac_web_2ch);

        //m_SettingData = new Preferences_Data(this);
        m_dbData = new DB_Data(this);
        m_dbData.InitDB();

        // 初期設定
        InitAd();
        InitData();
        InitTitle();
        InitWebView();

        // ＤＢ初期化
    	//if( null == m_dbCaptureMemo ){
    	//	m_dbCaptureMemo = new DB_CaptureMemo(this, SDAccess.GetDirectory());
    	//	m_dbCaptureMemo.InitDB(SDAccess.GetFiles());
    	//	m_dbCaptureMemo.SetDB();
    	//}
    }

    @Override
    public void onResume()
    {
    	m_csAd.StartAd_Asta();
    	super.onResume();
    }

    @Override
    public void onPause()
    {
    	m_csAd.StopAd_Asta();
    	super.onPause();
    }

    @Override
    public void onDestroy()
    {
    	m_csAd.DestroyAd_Asta();
    	super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return true;
    }
    @Override
    public boolean onMenuOpened (int featureId, Menu menu)
    {
    	return true;
    }

    // メニューの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return false;
    }

    // バックキーの動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if(keyCode != KeyEvent.KEYCODE_BACK){
    		// その他のキー
			return super.onKeyDown(keyCode, event);
		}else{
			// バックキー
			if( !BackWeb() ){
				//Toast.makeText(this, st_strItem, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
    	return false;
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
    }

    /*************************************
     * ＵＲＬの初期設定
     **************************************/
    public void InitData()
    {
    	try{
    		m_sItem = new RssItem();
        	Bundle extras = getIntent().getExtras();
	        if (extras!=null && Intent.ACTION_SEND.equals(getIntent().getAction())) {
	        	m_sItem.setFavoriteNo(extras.getInt("FAVORITE_NO"));
	        	m_sItem.setSiteName(extras.getString("SITE_NAME"));
	        	m_sItem.setTitle(extras.getString("TITLE"));
	        	m_sItem.setDate(extras.getString("DATE"));
	        	m_sItem.setLink(extras.getString("LINK"));
	        	m_sItem.setCategory(extras.getString("CATEGORY"));
	        	m_strStartUrl = extras.getCharSequence(Intent.EXTRA_TEXT).toString();
	        }
	    	if (extras!=null && m_strStartUrl==null) {
	        	m_sItem.setFavoriteNo(extras.getInt("FAVORITE_NO"));
	        	m_sItem.setSiteName(extras.getString("SITE_NAME"));
	        	m_sItem.setTitle(extras.getString("TITLE"));
	        	m_sItem.setDate(extras.getString("DATE"));
	        	m_sItem.setLink(extras.getString("LINK"));
	        	m_sItem.setCategory(extras.getString("CATEGORY"));
	    		m_strStartUrl = (String)extras.getSerializable("URL");
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	//デフォルトのＵＲＬ
    	if(m_strStartUrl == null){
    		m_strStartUrl = "http://www.google.com/";
    	}
    }

    /*************************************
     * タイトルの初期設定
     **************************************/
    public void InitTitle()
    {
    	TextView vwTitle = (TextView)findViewById(R.id.titleTextView);
    	vwTitle.setText(m_sItem.getTitle());
    	vwTitle.setSingleLine();
    	vwTitle.setFocusableInTouchMode(true);
    	vwTitle.setEllipsize(TruncateAt.MARQUEE);

    	mProgBar = (ProgressBar)findViewById(R.id.loadProgressBar);
    	mProgBar.setMax(100);
        mProgBar.setProgress(0);

        ImageView vwStar = (ImageView)findViewById(R.id.starImageView);
        if(!m_dbData.IsFavoriteData(m_sItem.getLink().toString())){
        	vwStar.setImageResource(android.R.drawable.star_big_off);
        }else{
        	vwStar.setImageResource(android.R.drawable.star_big_on);
        }
        vwStar.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View view) {
				ImageView vwStar = (ImageView)view;
		        if(!m_dbData.IsFavoriteData(m_sItem.getLink().toString()))
		        {
		        	m_dbData.InsertFavoriteDataInfo(m_sItem);
		        	vwStar.setImageResource(android.R.drawable.star_big_on);
		        }else{
		        	m_dbData.DeleteFavoriteData(m_sItem.getLink().toString());
		        	vwStar.setImageResource(android.R.drawable.star_big_off);
		        }
			}
        });
    }

    /*************************************
     * WebViewの初期設定
     **************************************/
    public void InitWebView()
    {
    	try{
	    	// WebViewで使うcookieの準備
	    	CookieSyncManager.createInstance(this);
	    	CookieSyncManager.getInstance().startSync();
	    	CookieManager.getInstance().setAcceptCookie(true);
	    	CookieManager.getInstance().removeExpiredCookie();
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	// WebViewの設定
        m_csWeb = (WebView)findViewById(R.id.web);

        //javascriptの設定はデフォルトで無効のためjavascriptを有効にする
        try{
            m_csWeb.setWebViewClient(new WebViewClient());
	        //m_csWeb.getSettings().setJavaScriptEnabled(true);
	        m_csWeb.getSettings().setBuiltInZoomControls(true);
	        m_csWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
	        // プラグインの設定
	        m_csWeb.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
        	Toast.makeText(this, "Javascript Setting Error", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
        	e.printStackTrace();
        	Toast.makeText(this, "Javascript Setting Error", Toast.LENGTH_SHORT).show();
        }
        // HTML5のための設定
        try{
	        // HTML5のための設定①－localStorage, settionStorage
	        m_csWeb.getSettings().setDomStorageEnabled(true);
	        String databasePath = this.getApplicationContext().getDir("localstorage", Context.MODE_PRIVATE).getPath();
	        m_csWeb.getSettings().setDatabasePath(databasePath);
	        // HTML5のための設定②－
	        m_csWeb.getSettings().setDatabaseEnabled(true);
	        File databaseDir = new File(getCacheDir(), databasePath);
	        databaseDir.mkdirs();
	        m_csWeb.getSettings().setDatabasePath(databaseDir.toString());
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
	    	Toast.makeText(this, "HTML5 Setting Error", Toast.LENGTH_SHORT).show();
	    }catch(IllegalStateException e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "HTML5 Setting Error", Toast.LENGTH_SHORT).show();
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "HTML5 Setting Error", Toast.LENGTH_SHORT).show();
	    }

        try{
	        WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
	        LoadUrlWithCookie(m_strStartUrl);
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "WebViewDatabase Setting Error", Toast.LENGTH_SHORT).show();
	    }

        // タッチ処理
        try{
	        m_csWeb.setOnTouchListener(new OnTouchListener()
	        {
		    	  @Override
		    	  public boolean onTouch(View v, MotionEvent event)
		    	  {
		    		try{
			    	    //get the URL of the touched anchor tag
			    	    WebView.HitTestResult hr = ((WebView)v).getHitTestResult();
			    	    String str = null;
			    	    if(null != hr) str = hr.getExtra();
			    	    //check if it is the URL of the thumbnail of the video
			    	    //which looks like
			    	    //http://i.ytimg.com/vi/<VIDEOID>/hqdefault.jpg?w=160&h=96&sigh=7exXMTRY7yiZm4hS4V_f9uVO-GU
			    	    if(str!=null && str.startsWith("http://i.ytimg.com/vi/")){
			    	      String videoId = str.split("\\/")[4];
			    	      //Everything is in place, now launch the activity
			    	      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" +videoId));
			    	      v.getContext().startActivity(i);
			    	      return true;
			    	    }
		            }catch(NoClassDefFoundError e){
		            	e.printStackTrace();
		    		}catch(Exception e){
		    			e.printStackTrace();
		    		}
			    	 return false;
		    	  }
	        });
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "onTouch Setting Error", Toast.LENGTH_SHORT).show();
	    }

        // ウェブクライアント
        try{
	    	m_csWeb.setWebViewClient(new WebViewClient()
	    	{
	    		private String loginCookie;
	            @Override
	            public void onLoadResource(WebView view, String url) {
	                    CookieManager cookieManager = CookieManager.getInstance();
	                    loginCookie = cookieManager.getCookie(url);
	            }
	            @Override
	            public void onPageStarted(WebView view, String url, Bitmap favicon) {
	                super.onPageStarted(view, url, favicon);
	                //setProgressBarIndeterminateVisibility(true);
	    			//EditText edit = (EditText)findViewById(R.id.UriText);
	    			//edit.setText(url);
	            }
	            @Override
	            public void onPageFinished(WebView view, String url) {
	            	CookieManager cookieManager = CookieManager.getInstance();
	                cookieManager.setCookie(url, loginCookie);
	                setProgressBarIndeterminateVisibility(false);
	                super.onPageFinished(view, url);
	            }
	            @Override
	            public void onReceivedHttpAuthRequest(WebView web, HttpAuthHandler handler, String host, String realm){

	            	String[] up = web.getHttpAuthUsernamePassword(host, realm);
	                if (up != null && up.length == 2) {
	                	handler.proceed(up[0], up[1]);
	                	//m_csWeb.setHttpAuthUsernamePassword(host, realm, up[0], up[1]);
	                }
	            }
	            @Override
	            public void onReceivedError (WebView view, int error, String desc, String failUrl) {
	            	Log.e("Log",		"errorCode:"+String.valueOf(error));
	            	Log.e("Description","Description:"+desc);
	            	Log.e("failUrl", 	"failUrl:"+failUrl);
	            }
	            @Override
	            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
	            	 handler.proceed() ;
	            }

	  			@Override
	  			public boolean shouldOverrideUrlLoading(WebView view, String strURL)
	  			{
	  				boolean bRet = false;
		    	    if(strURL!=null && IsURLofGooglePlay(strURL))
		    	    {
		    	    	String strDetailsKeyword = strURL.substring(strURL.lastIndexOf("details?id="), strURL.length()-1);
		    	    	IntentGooglePlay(Web2chViewer.this, strDetailsKeyword);
		    	    	bRet = true;
		    	    }
		    	    else if(strURL!=null && IsURLofYoutube(strURL))
		    	    {
			    	      String videoId = strURL.split("\\/")[4];
			    	      //Everything is in place, now launch the activity
			    	      Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" +videoId));
			    	      view.getContext().startActivity(i);
			    	      bRet = true;
		    	    }

	  				return bRet;
	  			}
	        });
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
	    	Toast.makeText(this, "WebViewClient Setting Error", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "WebViewClient Setting Error", Toast.LENGTH_SHORT).show();
        }

        try{
	        m_csWeb.setWebChromeClient(new WebChromeClient()
	        {
	        	  @Override
	        	  public void onProgressChanged(WebView view, int progress) {
	        		  // 読み込み中
	        		  mProgBar.setProgress(progress);
	                  if (progress==100) {
	                	  mProgBar.setVisibility(View.INVISIBLE);
	                  }
	        	  }
	        });
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
	    	Toast.makeText(this, "WebChromeClient Setting Error", Toast.LENGTH_SHORT).show();
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "WebChromeClient Setting Error", Toast.LENGTH_SHORT).show();
	    }

        try{
            // 画面右端の空白を消す
        	m_csWeb.setVerticalScrollbarOverlay(true);
        }catch(NoClassDefFoundError e){
        	e.printStackTrace();
	    	Toast.makeText(this, "WebView Setting Error", Toast.LENGTH_SHORT).show();
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "WebView Setting Error", Toast.LENGTH_SHORT).show();
	    }

        try{
    		m_csWeb.requestFocus();
	    }catch(Exception e){
	    	e.printStackTrace();
	    	Toast.makeText(this, "WebView Focus Error", Toast.LENGTH_SHORT).show();
	    }
    }

	public static boolean isHoneycomb() {
	    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
	public static boolean isHoneycombTablet(Context context) {
	    return isHoneycomb() && (context.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	// メッセージ表示
	public void ShowToast(String strMsg)
	{
		Toast.makeText(getBaseContext(), strMsg, Toast.LENGTH_SHORT).show();
	}


    /*******************************************
     * クッキーのセット
     *******************************************/
	public void LoadUrlWithCookie(String strUrl)
	{
		try{
			// HttpClientの準備
			DefaultHttpClient httpClient;
			httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams().setParameter("http.connection.timeout", 5000);
			httpClient.getParams().setParameter("http.socket.timeout", 3000);

			// ログイン
			HttpURLConnection urlConnection = null;
		    try {
				URL url = new URL(strUrl);
				urlConnection = (HttpURLConnection) url.openConnection();
		    	urlConnection.setRequestMethod("POST");
		    	urlConnection.connect();
		    } catch(Exception e) {
		    } finally {
		    	try {
		    		if(urlConnection != null){
		    			urlConnection.disconnect();
		    		}
		    	} catch (Exception e) {}
		    }

			// HttpClientで得たCookieの情報をWebViewでも利用できるようにする
			CookieStore cookieStr = httpClient.getCookieStore();
			Cookie cookie = null;
			if ( cookieStr != null ) {
				List<Cookie> cookies = cookieStr.getCookies();
				if (!cookies.isEmpty()) {
					for (int i = 0; i < cookies.size(); i++) {
						cookie = cookies.get(i);
					}
				}
				if (cookie != null) {
					String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
					CookieManager.getInstance().setCookie( strUrl, cookieString);
					CookieSyncManager.getInstance().sync();
				}
			}

			m_csWeb.loadUrl(strUrl);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    /*******************************************
     * 一つ前のページに（ページがなければ終了）
     *******************************************/
    private boolean BackWeb()
    {
    	boolean bRet = false;
		if(m_csWeb.canGoBack()){
			// 履歴があれば表示
			m_csWeb.goBack();
			bRet = true;
		}
		return bRet;
    }

    /*******************************************
     * 一つ先のページに
     *******************************************/
    private boolean ForwardWeb()
    {
    	boolean bRet = false;
    	if (m_csWeb.canGoForward()){
    		m_csWeb.goForward();
    		bRet = true;
        }
    	return bRet;
    }

    /******************************************************
     * Youtubeへのリンクかを判定
     *
     * @param	strURL		URL
     * @return	none
     ******************************************************/
	private boolean IsURLofYoutube(String strURL){	return strURL.startsWith("http://i.ytimg.com/vi/"); }

    /******************************************************
     * GooglePlayへのリンクかを判定
     *
     * @param	strURL		URL
     * @return	none
     ******************************************************/
	private boolean IsURLofGooglePlay(String strURL){	return strURL.startsWith("https://play.google.com/"); }

    /******************************************************
     * GooglePlayアプリ起動
     *
     * @param	ac				Activity
     * @param	strKeyword		GooglePlayで表示するキーワード
     * @return	none
     ******************************************************/
	static public void IntentGooglePlay(Activity ac, String strKeyword)
	{
		Uri uri = Uri.parse("market://" + strKeyword);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		ac.startActivity(intent);
	}

}
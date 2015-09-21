package com.luoye.gifmaker;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import android.content.*;
import android.content.res.*;
import com.luoye.gifmaker.*;
import java.lang.reflect.*;
import android.util.*;
import android.net.*;

public class MainActivity extends TabActivity
{
	
	//String alipay="https://qr.alipay.com/ap3jg6uth85ml1xa56";
	TabHost tabHost; 
    TabSpec spec;
	Intent intent;// Reusable Intent for each tab
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		//getWindow().setFlags(1024,1024);
        setContentView(R.layout.main);
		//overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
		
		tabHost = getTabHost();
		//Resources res = getResources();

        //第一个TAB
		intent = new Intent(this, HCActivity.class);//新建一个Intent用作Tab1显示的内容
		spec = tabHost.newTabSpec("tab1")//新建一个 Tab
			.setIndicator("合成GIF")//设置名称以及图标
			.setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
		tabHost.addTab(spec);//添加进tabHost

        //第二个TAB
		intent = new Intent(this, FJActivity.class);//第二个Intent用作Tab1显示的内容
		spec = tabHost.newTabSpec("tab2")//新建一个 Tab
			.setIndicator("分解GIF")//设置名称以及图标
			.setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
		tabHost.addTab(spec);//添加进tabHost

		
		tabHost.setCurrentTab(0);
		forceShowOverflowMenu();
    }
	private void forceShowOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
				.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		setOverflowIconVisible(featureId, menu);
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// TODO: Implement this method
		menu.add(0, 2, 0, "捐赠…");
		menu.add(0, 0, 0, "设置");
		menu.add(0, 1, 0, "使用帮助");
		
		return super.onCreateOptionsMenu(menu);
	}

	AlertDialog.Builder adlog;
	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// TODO: Implement this method
		if (item.getItemId() == 0)
		{

			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			//overridePendingTransition(R.anim.out_to_bottom, R.anim.in_from_bottom);
		}
		else if (item.getItemId() == 1)
		{

			adlog = new AlertDialog.Builder(this);
			adlog.setTitle("使用帮助")
				.setMessage(getResources().getString(R.string.help2)+getResources().getString(R.string.help))
				.setNegativeButton("确定", null)
				.setIcon(android.R.drawable.ic_menu_help)
				.show();
		}
		//捐赠
		else if(item.getItemId()==2)
		{
			Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.Alipay))); 
			it.setClassName("com.android.browser", "com.android.browser.BrowserActivity"); 
			startActivity(it); 
		}
		return super.onOptionsItemSelected(item);

	}


	/**
	 * 显示OverflowMenu的Icon
	 * 
	 * @param featureId
	 * @param menu
	 */
	private void setOverflowIconVisible(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
						"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
					Log.d("OverflowIconVisible", e.getMessage());
				}
			}
		}
	}
	
}

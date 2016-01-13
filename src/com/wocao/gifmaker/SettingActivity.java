package com.wocao.gifmaker;

import android.preference.*;
import android.preference.Preference.*;
import android.os.*;
import android.content.*;
import com.wocao.gifmaker.*;
import android.view.View.*;
import android.net.*;

public class SettingActivity extends PreferenceActivity
{
	CheckBoxPreference cb_use_first_size;

	EditTextPreference edit_delay,edit_output_path1,edit_output_path2,edit_pic_height,edit_pic_width;
	Preference about;
	boolean isSelected;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		cb_use_first_size = (CheckBoxPreference)findPreference("cb_use_first_size");

		edit_pic_width = (EditTextPreference)findPreference("edit_pic_width");
		edit_pic_height = (EditTextPreference)findPreference("edit_pic_height");
		about=findPreference("about");
		isSelected=cb_use_first_size.isChecked();
		setTwoEditEnabled();

		cb_use_first_size.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
			{
				public boolean onPreferenceChange(Preference p1, Object p2)
				{
					if (p1.getKey().equals("cb_use_first_size"))
					{
						isSelected=!cb_use_first_size.isChecked();
						setTwoEditEnabled();
					}
					return true;
				}
			});
		about.setOnPreferenceClickListener(new OnPreferenceClickListener(){

				@Override
				public boolean onPreferenceClick(Preference p1)
				{
					// TODO: Implement this method
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getResources().getString(R.string.QQGroup)));
					startActivity(intent);
					return true;
				}
				

			});
			
	}
	
	public void setTwoEditEnabled()
	{
		if (isSelected&&edit_pic_height.isEnabled())
		{
			edit_pic_height.setEnabled(false);
			edit_pic_width.setEnabled(false);
		}
		else
		{
			edit_pic_height.setEnabled(true);
			edit_pic_width.setEnabled(true);
		}
	}
	@Override
	public void onBackPressed()
	{
		// TODO: Implement this method
		super.onBackPressed();
		overridePendingTransition(R.anim.null_anim, R.anim.out_to_bottom);
		//设置切换动画，先进入，后退出，从上向下
	}

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		isSelected=false;
		super.onDestroy();
	}

	


}


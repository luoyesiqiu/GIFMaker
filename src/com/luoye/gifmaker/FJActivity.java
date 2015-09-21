package com.luoye.gifmaker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.*;
import android.os.*;
import com.luoye.gifmaker.*;
import com.luoye.gifmaker.core.*;
import com.luoye.gifmaker.other.*;

public class FJActivity extends Activity implements OnClickListener,GifDecoderAction
{

	SharedPreferences sp;
	ListView list;
	Bitmap bitmapItem=null;
	ProgressDialog pa;
	Button bn_add,bn_clear,bn_hc;
	AnimatedGIFEncoder ag;
	List<Map<String, Object>> items ;
	Map<String, Object> item;
	SimpleAdapter adapter;
	//SimpleDateFormat formatter;
	String preferencePath;
	GifDecoder gifDecoder;
	AlertDialog.Builder adlog;
	int curIndex;
	final String THIS_ACTION="fjAction";
	
	class MenuId{
		public final static int ID_REMOVE_ITEM=200;
	};
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fj);
//		overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
		bn_add = (Button)findViewById(R.id.bn_add2);
		bn_clear = (Button)findViewById(R.id.bn_clear2);
		bn_hc = (Button)findViewById(R.id.bn_fj);
		list = (ListView)findViewById(R.id.list2);

		bn_add.setOnClickListener(this);
		bn_clear.setOnClickListener(this);
		bn_hc.setOnClickListener(this);
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(THIS_ACTION);
		registerReceiver(br, intentFilter);
		FileList.curpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		//FileList.filter = ".*.gif$";
		FileList.broadcastAction = THIS_ACTION;
		initAdapter();
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
				@Override
				public void onCreateContextMenu(ContextMenu p1, View p2, ContextMenu.ContextMenuInfo p3)
				{
					// TODO: Implement this method
					p1.add(0, MenuId.ID_REMOVE_ITEM, 0, "移除该图片");
				}
			});
    }


	//点击Context菜单的项时发生
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo =
			(AdapterContextMenuInfo) item.getMenuInfo();
		// TODO: Implement this method
		if (item.getItemId() == MenuId.ID_REMOVE_ITEM)
		{
			//移除项
			items.remove(menuInfo.position);
			adapter.notifyDataSetChanged();
			showToast("移除成功");
		}
		return super.onOptionsItemSelected(item);
	}

//handler状态：0分解完全部图，1出错，2分解完一张图
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			// TODO: Implement this method
			if (msg.what == 0)
			{
				pa.dismiss();
				adlog = new AlertDialog.Builder(FJActivity.this);
				adlog.setTitle("分解完成")
					.setMessage("全部图片已分解完成" + "\n保存路径：" + sp.getString("edit_output_path2", "/mnt/sdcard/GIFMaker/png"))
					.setNegativeButton("确定", null)
					//.setIcon(android.R.drawable.ic_menu_help)
					.show();
				showToast("分解完成");
			}
			else if (msg.what == 1)
			{
				pa.dismiss();
				adlog = new AlertDialog.Builder(FJActivity.this);
				adlog.setTitle("分解未完成")
					.setMessage("要分解的图片太大，无法生成，发生在第" + curIndex + "张")
					.setNegativeButton("确定", null)
					//.setIcon(android.R.drawable.ic_menu_help)
					.show();
				showToast("分解失败");
			}
			else if (msg.what == 2)
			{
				pa.setMessage(String.format("正在分解(%d/%d)……",curIndex+1,items.size()));
			}
		}

	};
	/**
	 * gif解码观察者
	 * @param parseStatus 解码是否成功，成功会为true
	 * @param frameIndex 当前解码的第几帧，当全部解码成功后，这里为-1
	 */
	@Override
	public void parseOk(boolean parseStatus, int frameIndex)
	{
		// TODO: Implement this method
		//Log.d(">>>>parse" + ">>>" + curIndex, frameIndex + "");
		if (frameIndex != -1)
		{
			preferencePath = sp.getString("edit_output_path2", "/mnt/sdcard/GIFMaker/png");
			try
			{
				Bitmap bitmap=gifDecoder.next().image;
				IO.saveBitmap(bitmap, preferencePath + "/" + items.get(curIndex).get("fileName") + "_" + String.format("%03d",frameIndex) + ".png");
			}
			//内存不足时
			catch (Exception ooMemoryError)
			{
				handler.sendEmptyMessage(1);
				return;
			}
		}
		//分解完一张图
		if (frameIndex == -1)
		{
			curIndex++;
			//记得释放内存，有效果
			if (gifDecoder != null)
			{
				gifDecoder.free();
				//gifDecoder.destroy();
				gifDecoder = null;
			}
			//分解完一张就通知主线程
			handler.sendEmptyMessage(2);
			//列表还有图片，就开启一个线程来分解
			if (curIndex < items.size())
			{
				gifDecoder = new GifDecoder(IO.readStream(items.get(curIndex).get("filePath").toString()), this);
				gifDecoder.start();
			}
			//当分解完全部图片时
			else if (curIndex == items.size())
			{
				curIndex = 0;
				handler.sendEmptyMessage(0);
			}

		}

	}

	//按钮事件*************
	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
		if (p1.getId() == R.id.bn_add2)
		{

			Intent intent=new Intent();
			intent.setClass(FJActivity.this, FileList.class);
			startActivity(intent);
			//overridePendingTransition(R.anim.out_to_bottom, R.anim.in_from_bottom);
		}
		else if (p1.getId() == R.id.bn_clear2)
		{
			if (items.size() > 0)
			{
				adlog = new AlertDialog.Builder(this);
				adlog.setTitle("提示")
					.setMessage("确定清空列表吗？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							// TODO: Implement this method
							items.clear();
							adapter.notifyDataSetChanged();
						}
					})
					.setNegativeButton("取消", null)
					.show();
			}
		}
		else if (p1.getId() == R.id.bn_fj)
		{

			if (items.size() > 0)
			{
				pa = ProgressDialog.show(FJActivity.this, null, String.format("正在分解(%d/%d)……",curIndex+1,items.size()));
				//让它不可返回
				pa.setCancelable(false);
				curIndex = 0;
				gifDecoder = new GifDecoder(IO.readStream(items.get(curIndex).get("filePath").toString()), this);
				gifDecoder.start();

			}
			else
				showToast("请先添加图片");

		}
	}
	Runnable loadFileList=new Runnable(){

		@Override
		public void run()
		{
			// TODO: Implement this method
			FileList.filter = ".*.gif$";

			FileList.getListFromPath(FileList.curpath);
		}


	};
	@Override
	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter)
	{
		// TODO: Implement this method
		return super.registerReceiver(receiver, filter);
	}

	//实例化一个广播接收器,收到广播说明选择了文件
	BroadcastReceiver br=new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			// TODO: Implement this method
			if (p2.getAction().equals(THIS_ACTION))
			{
				bitmapItem = IO.getImageFromPath(FileList.selectedFilePath);

				item = new HashMap<String, Object>();    
				item.put("imageItem", bitmapItem); 
				item.put("filePath", FileList.selectedFilePath);
				item.put("fileName", new File(FileList.selectedFilePath).getName());  
				item.put("shortName",FileList.getShortName(new File(FileList.selectedFilePath).getName(),12,2));
				items.add(item);  

				adapter.notifyDataSetChanged();

				list.setSelection(items.size() - 1);
			}
		}
	};

	public void showToast(CharSequence text)
	{
		Toast.makeText(FJActivity.this, text, 2000).show();
	}

	//初始化adapter
	public void initAdapter()
	{
		items = new ArrayList<Map<String,Object>>(); 
		//实例化一个适配器  
		adapter = new SimpleAdapter(FJActivity.this, items,
									R.layout.fj_list_item,
									new String[]{ "shortName","filePath"},
									new int[]{ R.id.fj_tv_filename}
									);  

		list.setAdapter(adapter);  

	}

	long cur=0;
	@Override
	public void onBackPressed()
	{
		if (System.currentTimeMillis() - cur > 2000)
		{
			cur = System.currentTimeMillis();
			Toast.makeText(FJActivity.this, "再按一次返回键退出", 0).show();
		}
		else
		{
			finish();
			System.exit(0);
		}
	}

	@Override
	protected void onResume()
	{
		//窗口被重新激活时发生
		// TODO: Implement this method
		FileList.filter = ".*.gif$";
		FileList.broadcastAction = THIS_ACTION;
		//清空缓存数组
		FileList.dirs_cache = null;
		FileList.files_cache = null;
		//开启线程载入列表
		new Thread(loadFileList).start();
//		FileList.dirs_name = null;
//		FileList.files_name = null;
		//showToast(THIS_ACTION);
		super.onResume();
	} 

}

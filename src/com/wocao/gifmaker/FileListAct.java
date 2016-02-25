package com.wocao.gifmaker;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import android.widget.SimpleAdapter.*;
import java.io.*;
import java.text.*;
import java.util.*;

import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView.*;
import com.wocao.gifmaker.*;
import com.wocao.gifmaker.other.*;
import android.media.*;
public class FileListAct extends Activity implements OnScrollListener
{

	OnClickListener w;
	GridView list;
	SimpleAdapter adapter;
	static List<File> filesList;
	//缓存上层目录
	/*******************
	 *缓存有两种情况被清空
	 *1，按返回上层目录之后。
	 *2，重新载入Activity后
	 ********************/
	
	static List<File> filesList_cache;

	Map<String,String> map_paths_name,map_files;

	static String broadcastAction="Action";
	static String selectedFilePath=null;
	static File curpath=null;
	static String filter="";
	List<Map<String, Object>> items;
	Map<String, Object> item;
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		list = (GridView)findViewById(R.id.file_list);
		try
		{
			//getListFromPath(curpath);
			loadList();
			loadVisiableListItemImages(0, 10);
			adapter.notifyDataSetChanged();
		}
		catch (Exception e)
		{//错误时获取根目录
		
			curpath = new File("/");
			getListFromPath(curpath);
			loadList();
		}
		//这些事件放在载入列表之后，可以加快载入列表的速度
		//上下文菜单事件
		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
				@Override
				public void onCreateContextMenu (ContextMenu p1, View p2, ContextMenu.ContextMenuInfo p3)
				{ 
					if (filesList.get(((AdapterContextMenuInfo)p3).position-1).isFile() )
						p1.add(0, 0, 0, "查看图片");
				}
			});
		list.setOnScrollListener(this);
		//设置列表单击事件
		list.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick (AdapterView<?> p1, View p2, int position, long p4)
				{
					// TODO: Implement this method
					//点击返回上级时
					if (position == 0 && !curpath.toString().equals("/"))
					{
						goBack();
					}

					//点击文件夹时,这里要注意数组的范围
					else if ((position == 0 && curpath.toString().equals("/")) || filesList.get(position-1).isDirectory())
					{
						
						try
						{
							
							filesList_cache = filesList;

							//对根目录的处理
							if (curpath.toString().equals("/"))
							{
								
								curpath =new File( curpath + "/" + filesList.get(position).getName());
							}
							else
							{
								curpath =new File (curpath + "/" + filesList.get(position - 1).getName());
							}

							getListFromPath(curpath);
							loadList();
							//如果有图片则载入图片
							loadVisiableListItemImages(0, 10);
							adapter.notifyDataSetChanged();
						}
						catch (Exception e)
						{
							//发生异常时，当前路径不变，并重新载入列表
							
							curpath =new File( curpath.getParent());
							getListFromPath(curpath);
							loadList();
							//如果有图片则载入图片
							loadVisiableListItemImages(0, 10);
							adapter.notifyDataSetChanged();
							showToast("抱歉，该文件夹不能访问！" + "\n" + e.toString());
						}
					}

					//点击文件时
					else if (filesList.get(position-1).isFile())
					{
						//if ((curpath.charAt(curpath.length() - 1) == '/'))
						//curpath = curpath.substring(0, curpath.length() - 1);
						selectedFilePath =  curpath + "/" + filesList.get(position -1).getName();

						showToast("文件：" + filesList.get(position-1).getName() + " 已添加到列表");
						//结束
						//FileListActivity.this.finish();
						//发送广播
						Intent intent=new Intent();
						intent.setAction(broadcastAction);
						sendBroadcast(intent);
					}

				}
			});
			getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onBackPressed()
	{
		// TODO: Implement this method
		if(curpath.toString().equals("/"))
			finish();
			else
				goBack();
	}
	
	
	
	/*
	*返回上层目录
	*/
	private void goBack()
	{
		try
		{
			curpath = new File(curpath.getParent());
			//缓存都不为空时才能使用缓存
			if (filesList_cache != null)
			{
				
				filesList = filesList_cache;
				//使用后清空缓存
				
				filesList_cache = null;
			}
			//没有缓存就重新获取列表
			else
			{
				getListFromPath(curpath);
			}

			loadList();
			loadVisiableListItemImages(0, 10);
			adapter.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			showToast("抱歉，发生了错误！" + "\n" + e.toString());
		}
	}
	/*
	 *@view 报告滑动状态的视图
	 *@scrollState 滑动状态
	 *滑动状态
	 *SCROLL_STATE_IDLE : 0  视图没有滑动
	 *SCROLL_STATE_TOUCH_SCROLL : 1  用户正在触摸滑动，手指仍在屏幕上
	 *SCROLL_STATE_FLING : 2  用户之前触摸滑动，现在正在滑行，直到停止
	 */
	@Override
	public void onScrollStateChanged (AbsListView p1, int p2)
	{
		// TODO: Implement this method
		//showToast(p2 + "");
		//停止滑动时更新列表
		if (p2 == SCROLL_STATE_IDLE)
		{
			adapter.notifyDataSetChanged();
		}

	}

	/**
	 *@view 报告滑动状态的视图
	 *@firstVisibleItem 可视的第一个列表项的索引
	 *@visibleItemCount 可视的列表项个数
	 *@totalItemCount 总共的列表项个数
	 */
	@Override
	public void onScroll (AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		//showToast(visibleItemCount + "");
		loadVisiableListItemImages(firstVisibleItem, visibleItemCount);
	}
	private void loadVisiableListItemImages (int firstVisibleItem, int visibleItemCount)
	{
		//showToast(visibleItemCount+"");
		Bitmap tempBitmap=null;
		for (int i=firstVisibleItem;i <= visibleItemCount + firstVisibleItem;i++)
		{
			try
			{
				//是文件才操作
				if (items.get(i).get("isFile").equals(true))
				{
					//图标为空时才获取
					if (items.get(i).get("iconItem") == null)
					{
						items.get(i).put("iconItem"
										 , tempBitmap = IO.getImageFromPath2(items.get(i).get("filePath").toString()));
						//载入图片错误，则显示默认
						if (null == tempBitmap)
							items.get(i).put("iconItem", R.drawable.image);
					}
				}

			}
			catch (Exception e)
			{

			}
		}
	}
//点击Context菜单的项时发生
	@Override
	public boolean onContextItemSelected (MenuItem item)
	{
		AdapterContextMenuInfo menuInfo =
			(AdapterContextMenuInfo) item.getMenuInfo();
		// TODO: Implement this method
		if (item.getItemId() == 0)
		{
			//预览
			//showToast(files.get(menuInfo.position - dirs.size()-1).getName());

			Intent intent=new Intent(FileListAct.this,GifViewActivity.class);
			intent.putExtra("imagePath",curpath + "/" + filesList.get(menuInfo.position - 1).getName());
			startActivity(intent);

		}
		return super.onOptionsItemSelected(item);
	}
	//在调用该方法之前务必先调用getListFromPath方法
	public void loadList ()
	{
		String tempName=null;
		Bitmap tempBitmap=null;
		//getListFromPath(curpath);
		items = new ArrayList<Map<String,Object>>(); 
		//添加返回上级目录项
		if (!curpath.getAbsolutePath().equals("/"))
		{
			item = new HashMap<String, Object>();  
			item.put("iconItem", R.drawable.folder);  
			item.put("nameItem", ".."); 
			//item.put("sizeItem", "");
			item.put("modifyItem", "");
			item.put("isFile", false);
			items.add(item);  
		}
		//添加文件夹项
        
		//添加文件项
		for (int i = 0; i < filesList.size() ; i++)
		{
			item = new HashMap<String, Object>();  
			tempName = filesList.get(i).getName();
			//是文件夹才添加图标
			item.put("iconItem", filesList.get(i).isDirectory()?R.drawable.folder:null);  
			//文件就不写文件名了
			item.put("nameItem", filesList.get(i).isFile()?"":filesList.get(i).getName());
			//是文件才添加大小
			//item.put("sizeItem", filesList.get(i).isFile()?formetFileSize(getFileSizes(filesList.get(i))):"");
			item.put("filePath", filesList.get(i).getAbsolutePath());
			item.put("isFile", filesList.get(i).isFile());
			items.add(item);  
		}


		//实例化一个适配器  
		adapter = 
			new SimpleAdapter(this, items, 
							  R.layout.filelist_item, 					  
							  new String[]{"iconItem", "nameItem"}, 
							  new int[]{R.id.filelist_icon, R.id.filelist_name});  
		//list中放bitmap
		adapter.setViewBinder(new ViewBinder(){
				public boolean setViewValue (View view, Object data, String str)
				{
					if (view instanceof ImageView && data instanceof Bitmap)
					{
						ImageView iv = (ImageView) view;
						iv.setImageBitmap((Bitmap) data);
						return true;
					}
					else
						return false;
				}
			}
		);
		list.setAdapter(adapter);

		//显示的更加人性化
		setTitle(getShortPath(curpath.toString()));
		//getActionBar().setTitle(getShortPath(curpath));
	}

	//获取缩略路径
	public static String getShortPath (String path)
	{
		int n=0,p=0;
		for (int i=0;i < path.length();i++)
			if (path.charAt(i) == '/')
				++n;

		if (n >= 3)
		{
			for (int i=0;i < path.length();i++)
			{
				if (path.charAt(i) == '/')
					++p;
				if (p == n - 1)
				{
					String newPath = "..." + path.substring(i, path.length());
					return newPath;
				}
			}
		}

		return path;
	}
	//载入列表到数组
	public static  void getListFromPath (File path)
	{
		filesList = new ArrayList<File>();
        File[] files_all = path.listFiles();

		//不是根目录添加返回项
		for (int i=0;i < files_all.length;i++)
		{
			if ((files_all[i].isFile() && files_all[i].getName().matches(filter))||files_all[i].isDirectory())
			{
				filesList.add(files_all[i]);
			}
			
		}
		//对集合进行排列
		
		Collections.sort(filesList, new FileNameSort());
	}

	
	
	public void showToast (CharSequence text)
	{
		Toast.makeText(FileListAct.this, text, 2000).show();
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// TODO: Implement this method
		//menu.add(0, 0, 0, "返回");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// TODO: Implement this method
		if (item.getItemId() == android.R.id.home)
		{
			finish();
		}

		return super.onOptionsItemSelected(item);

	}
	public String getDate (long time)
	{
		//String nowStr;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date curDate = new Date(time);//获取当前时间 
		return  sdf.format(curDate);//转换时间格式

	}
	public static long getFileSizes (File f)
	{
		//取得文件大小
		long s=0;
		try
		{
			s = f.length();
		}
		catch (Exception e)
		{
		}
		return s;
	}
	/*
	*获取缩略文件名
	*@str 文件名
	*@len1 前面最大字符数
	*@len2 后面最大字符数
	*return 缩略的文件名
	*/
	public static String getShortName (String str,final int len1,final int len2)
	{
		int index;
		String before=null;
		String after=null;
		index = str.lastIndexOf(".");
		if (index > (len1+len2))
		{
			before = str.substring(0, len1);
			after = str.substring(index-len2, str.length());
			str = before + "..." + after;
		}
		return str;
	}
	public static  String formetFileSize (long fileS)
	{
		//转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024)
		{
			fileSizeString = df.format((double) fileS) + "b";
			if (fileS == 0)
				fileSizeString = fileS + ".0b";
		}
		else if (fileS < 1048576)
		{
			fileSizeString = df.format((double) fileS / 1024) + "Kb";
		}
		else if (fileS < 1073741824)
		{
			fileSizeString = df.format((double) fileS / 1048576) + "Mb";
		}
		else
		{
			fileSizeString = df.format((double) fileS / 1073741824) + "Gb";
		}
		return fileSizeString;
	}
}


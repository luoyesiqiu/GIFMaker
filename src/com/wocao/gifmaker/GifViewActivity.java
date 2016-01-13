package com.wocao.gifmaker;
import org.json.JSONObject;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.app.AlertDialog.*;
import com.wocao.gifmaker.*;
import com.wocao.gifmaker.core.*;
import com.wocao.gifmaker.other.*;
import android.graphics.*;
import java.io.*;

public class GifViewActivity extends Activity
{
	GifView gifview;
	Intent intent;
    String imagePath;
	ImageView iv;
	Bitmap tempBitmap;
	byte[] tempByte;
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		requestWindowFeature(1);
		getWindow().setFlags(1024,1024);
		setContentView(R.layout.gifview);
		gifview = (GifView)findViewById(R.id.gifview);
		iv = (ImageView)findViewById(R.id.iv);
		intent = getIntent();
		imagePath = intent.getStringExtra("imagePath");
		showToast("查看：" + imagePath);
		//gifview.setGifImageType(gifview.GifImageType.);
		try
		{
			if (imagePath.matches(".*.gif$"))
			{
				tempByte=IO.readStream(imagePath);
				gifview.setGifImage(tempByte);
			}
			else
			{
				tempBitmap=BitmapFactory.decodeFile(imagePath);
				iv.setImageBitmap(tempBitmap);
			}
		}
		catch (OutOfMemoryError error)
		{
			showToast("查看失败");
		}
		catch(Exception e){
			showToast("查看失败");
		}

	}
	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// TODO: Implement this method
		menu.add(0, 0, 0, "返回");
		return super.onCreateOptionsMenu(menu);
	}
	public void showToast (CharSequence text)
	{
		Toast.makeText(GifViewActivity.this, text, 2000).show();
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// TODO: Implement this method
		if (item.getItemId() == 0)
		{
			finish();
		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onDestroy ()
	{
		// TODO: Implement this method
		if(tempBitmap!=null)
			tempBitmap.recycle();
			tempByte=null;
			super.onDestroy();
	}

	
}

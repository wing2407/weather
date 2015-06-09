package com.weather.app.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.weather.app.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FutureWeatherActivity extends Activity {

	private ListView futureList; // 未来天气数据list
	private TextView cityName; // 用于显示城市名

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.future_weather);

		cityName = (TextView) findViewById(R.id.city_name_future);	
		futureList = (ListView) findViewById(R.id.weather_list);
		// 定义动态数组
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		//读取sharedpreference中的数据
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		for (int i = 0; i < 5; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("date", prefs.getString("date" + i, ""));
			map.put("temp",
					prefs.getString("low" + i, "") + "~"
							+ prefs.getString("high" + i, ""));
			map.put("type", prefs.getString("type" + i, ""));
			map.put("fengxiang", prefs.getString("fengxiang" + i, ""));
			map.put("fengli", prefs.getString("fengli" + i, ""));
			listItem.add(map);
		}

		// 绑定数据，参数：绑定的数据，行布局，Map，各自定义布局控件
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this, listItem,
				R.layout.future_weather_list, new String[] { "date", "temp",
						"type", "fengxiang", "fengli" }, new int[] {
						R.id.date_list, R.id.temp_list, R.id.type_list,
						R.id.fengxiang_list, R.id.fengli_list });
		
		cityName.setText(prefs.getString("city_name", "")); //显示城市名标题
		cityName.setVisibility(View.VISIBLE);
		
		futureList.setAdapter(mSimpleAdapter);// 为ListView绑定适配器

	}

}
/*
 * // 自定义适配器 class FutureWeatherAdapter extends ArrayAdapter<FutureWeather> {
 * 
 * private int resourceId; private Context mContext;
 * 
 * public FutureWeatherAdapter(Context context, int resource,
 * List<FutureWeather> objects) { super(context, resource, objects); resourceId
 * = resource; this.mContext = context; }
 * 
 * @Override public View getView(int position, View convertView, ViewGroup
 * parent) { //FutureWeather futureWeather = getItem(position); //
 * 获取当前项的FutureWeather实例 ViewHolder holder = null; if (convertView == null) {
 * holder = new ViewHolder(); convertView =
 * LayoutInflater.from(mContext).inflate(resourceId, null); holder.dateText =
 * (TextView) findViewById(R.id.date_list); holder.tempText = (TextView)
 * findViewById(R.id.temp_list); holder.typeText = (TextView)
 * findViewById(R.id.type_list); holder.fxText = (TextView)
 * findViewById(R.id.fengxiang_list); holder.flText = (TextView)
 * findViewById(R.id.fengli_list); convertView.setTag(holder); } else { holder =
 * (ViewHolder) convertView.getTag(); }
 * 
 * holder.dateText.setText(getItem(position).getDate());
 * holder.tempText.setText(getItem(position).getLow() + "/" +
 * getItem(position).getHigh());
 * holder.typeText.setText(getItem(position).getType());
 * holder.fxText.setText(getItem(position).getFengxiang());
 * holder.flText.setText(getItem(position).getFengli()); return convertView; }
 * 
 * class ViewHolder { TextView dateText; TextView tempText; TextView typeText;
 * TextView fxText; TextView flText; }
 * 
 * }
 */


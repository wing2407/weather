package com.weather.app.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.weather.app.R;
import com.weather.app.model.City;
import com.weather.app.model.County;
import com.weather.app.model.Province;
import com.weather.app.model.WeatherDB;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0; // 列表状态0,1,2
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherDB weatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList; // 省列表
	private List<City> cityList; // 市列表
	private List<County> countyList; // 县列表

	private Province selectedProvince; // 选中的省份
	private City selectedCity; // 选中的城市
	private int currentLevel; // 选中的级别

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

/*		//读取标志位，如果为ture直接跳转
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}*/

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		weatherDB = WeatherDB.getInstance(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces(); // 加载省级数据
	}

	// 查询全国所有省，优先数据库查询，然后xml查询
	private void queryProvinces() {
		provinceList = weatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromXml(); // 子线程查询xml文件
		}
	}

	// 查询省下所有市，数据库查询
	private void queryCities() {
		cityList = weatherDB.loadCities(Integer.parseInt(selectedProvince
				.getProvinceCode()));
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}
	}

	// 查询市下所有县，数据库查询
	private void queryCounties() {
		countyList = weatherDB.loadCounties(Integer.parseInt(selectedCity
				.getCityCode()));
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}
	}

	// 从XML文件里读取城市信息
	private void queryFromXml() {
		try {
			InputStream inputStream = getAssets().open("city.xml"); // 从assets文件夹里读取xml文件
			XmlPullParserFactory factory;
			factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			// setInput()方法将返回的 XML 数据设置进去就可以开始解析
			xmlPullParser.setInput(inputStream, "GBK");
			// getEventType()可以得到当前的解析事件
			int eventType = xmlPullParser.getEventType();

			String province_id = null; // 创建各实例以及对应的ID
			String city_id = null;
			String county_id = null;
			Province province = new Province();
			City city = new City();
			County county = new County();
			showProgressDialog();
			weatherDB.db.beginTransaction();// 开启事务
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = xmlPullParser.getName();
				switch (eventType) {
				// 开始解析结点
				case XmlPullParser.START_TAG: {
					if ("province".equals(nodeName)) {
						// province节点
						province_id = xmlPullParser.getAttributeValue(null,
								"id");
						province.setProvinceCode(province_id);
						province.setProvinceName(xmlPullParser
								.getAttributeValue(null, "name"));
						weatherDB.saveProvince(province); // 解析完每个节点都保存
					} else if ("city".equals(nodeName)) {
						// city节点
						city_id = xmlPullParser.getAttributeValue(null, "id");
						city.setCityCode(city_id);
						city.setCityName(xmlPullParser.getAttributeValue(null,
								"name"));
						city.setProvinceId(Integer.parseInt(province_id));
						weatherDB.saveCity(city);
					} else if ("county".equals(nodeName)) {
						// county节点
						county_id = xmlPullParser.getAttributeValue(null, "id");
						county.setCountyCode(county_id);
						county.setCountyName(xmlPullParser.getAttributeValue(
								null, "name"));
						county.setCityId(Integer.parseInt(city_id));
						weatherDB.saveCounty(county);
						//Log.d("MainActivity", "id is " + county_id);
					}
					break;
				}// 完成解析某个结点
				case XmlPullParser.END_TAG: {
					if ("province".equals(nodeName)) {
						closeProgressDialog();
						queryProvinces();
					}
					break;
				}
				default:
					break;
				}
				eventType = xmlPullParser.next();
			}
			inputStream.close(); // 关闭流
			weatherDB.db.setTransactionSuccessful();
			weatherDB.db.endTransaction();// 关闭事务
		} catch (Exception e) {
			// 解析错误时候提示
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this, "加载失败",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	// 显示进度框
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	// 关闭进度框
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	// 根据当前级别判断，返回列表（市，省，退出）
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}

}

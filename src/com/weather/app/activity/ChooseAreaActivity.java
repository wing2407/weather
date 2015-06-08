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

	public static final int LEVEL_PROVINCE = 0; // �б�״̬0,1,2
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherDB weatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList; // ʡ�б�
	private List<City> cityList; // ���б�
	private List<County> countyList; // ���б�

	private Province selectedProvince; // ѡ�е�ʡ��
	private City selectedCity; // ѡ�еĳ���
	private int currentLevel; // ѡ�еļ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

/*		//��ȡ��־λ�����Ϊtureֱ����ת
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
		queryProvinces(); // ����ʡ������
	}

	// ��ѯȫ������ʡ���������ݿ��ѯ��Ȼ��xml��ѯ
	private void queryProvinces() {
		provinceList = weatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromXml(); // ���̲߳�ѯxml�ļ�
		}
	}

	// ��ѯʡ�������У����ݿ��ѯ
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

	// ��ѯ���������أ����ݿ��ѯ
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

	// ��XML�ļ����ȡ������Ϣ
	private void queryFromXml() {
		try {
			InputStream inputStream = getAssets().open("city.xml"); // ��assets�ļ������ȡxml�ļ�
			XmlPullParserFactory factory;
			factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			// setInput()���������ص� XML �������ý�ȥ�Ϳ��Կ�ʼ����
			xmlPullParser.setInput(inputStream, "GBK");
			// getEventType()���Եõ���ǰ�Ľ����¼�
			int eventType = xmlPullParser.getEventType();

			String province_id = null; // ������ʵ���Լ���Ӧ��ID
			String city_id = null;
			String county_id = null;
			Province province = new Province();
			City city = new City();
			County county = new County();
			showProgressDialog();
			weatherDB.db.beginTransaction();// ��������
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String nodeName = xmlPullParser.getName();
				switch (eventType) {
				// ��ʼ�������
				case XmlPullParser.START_TAG: {
					if ("province".equals(nodeName)) {
						// province�ڵ�
						province_id = xmlPullParser.getAttributeValue(null,
								"id");
						province.setProvinceCode(province_id);
						province.setProvinceName(xmlPullParser
								.getAttributeValue(null, "name"));
						weatherDB.saveProvince(province); // ������ÿ���ڵ㶼����
					} else if ("city".equals(nodeName)) {
						// city�ڵ�
						city_id = xmlPullParser.getAttributeValue(null, "id");
						city.setCityCode(city_id);
						city.setCityName(xmlPullParser.getAttributeValue(null,
								"name"));
						city.setProvinceId(Integer.parseInt(province_id));
						weatherDB.saveCity(city);
					} else if ("county".equals(nodeName)) {
						// county�ڵ�
						county_id = xmlPullParser.getAttributeValue(null, "id");
						county.setCountyCode(county_id);
						county.setCountyName(xmlPullParser.getAttributeValue(
								null, "name"));
						county.setCityId(Integer.parseInt(city_id));
						weatherDB.saveCounty(county);
						//Log.d("MainActivity", "id is " + county_id);
					}
					break;
				}// ��ɽ���ĳ�����
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
			inputStream.close(); // �ر���
			weatherDB.db.setTransactionSuccessful();
			weatherDB.db.endTransaction();// �ر�����
		} catch (Exception e) {
			// ��������ʱ����ʾ
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this, "����ʧ��",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	// ��ʾ���ȿ�
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	// �رս��ȿ�
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	// ���ݵ�ǰ�����жϣ������б��У�ʡ���˳���
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

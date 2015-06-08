package com.weather.app.activity;

import com.weather.app.R;
import com.weather.app.util.HttpCallbackListener;
import com.weather.app.util.HttpUtil;
import com.weather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText; // ������ʾ������
	private TextView weatherTypeText; // ������ʾ����������Ϣ
	private TextView tempText; // ������ʾ����
	private TextView windyText; // ������ʾ��������
	private TextView currentDateText; // ������ʾ��ǰ����
	private Button futureButton; // ����δ������������İ�ť
	private Button switchCity; // �л����а�ť
	private Button refreshWeather; // ����������ť

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather);

		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);		
		weatherTypeText = (TextView) findViewById(R.id.weather_type);
		tempText = (TextView) findViewById(R.id.temp);
		windyText = (TextView) findViewById(R.id.windy);
		currentDateText = (TextView) findViewById(R.id.current_date);
		futureButton = (Button) findViewById(R.id.future);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// ���ؼ�����ʱ��ȥ��ѯ����
			tempText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyCode);
		} else {
			// û���ؼ�����ʱ��ֱ����ʾ��������
			showWeather();
		}
		futureButton.setOnClickListener(this);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			tempText.setText("ͬ����...");
			String countyCode = getIntent().getStringExtra("county_code");
			if (!TextUtils.isEmpty(countyCode)) {
				queryWeatherInfo(countyCode);
			}
			break;
		case R.id.future:
			break;
		default:
			break;
		}
	}

	// ��ѯ������������Ӧ��������
	private void queryWeatherInfo(String countyCode) {
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=101"
				+ countyCode;
		queryFromServer(address);
	}

	// ���ݴ���ĵ�ַ������ȥ���������ѯ������Ϣ
	private void queryFromServer(final String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				// ������������ص�������Ϣ
				Utility.handleWeatherResponse(WeatherActivity.this, response);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showWeather();
					}
				});
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tempText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}

	// ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		tempText.setText(prefs.getString("temp", "")+"��");
		windyText.setText(prefs.getString("fengxiang", "") + "/"
				+ prefs.getString("fengli", ""));
		weatherTypeText.setText(prefs.getString("type0", ""));
		futureButton.setText("δ������  >");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}

}

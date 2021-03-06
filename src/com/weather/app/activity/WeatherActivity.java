package com.weather.app.activity;

import com.weather.app.R;
import com.weather.app.service.AutoUpdateService;
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
	private TextView cityNameText; // 用于显示城市名
	private TextView weatherTypeText; // 用于显示天气描述信息
	private TextView tempText; // 用于显示气温
	private TextView windyText; // 用于显示风力风向
	private TextView currentDateText; // 用于显示当前日期
	private TextView futureText; // 用于未来几天的天气的按钮
	private Button switchCity; // 切换城市按钮
	private Button refreshWeather; // 更新天气按钮
	private String weatherCode;

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
		futureText = (TextView) findViewById(R.id.future);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);

		weatherCode = getIntent().getStringExtra("weather_code");
		if (!TextUtils.isEmpty(weatherCode)) {
			// 有天气代号时就去查询天气
			tempText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherCode);
		} else {
			// 没有天气代号时就直接显示本地天气
			showWeather();
		}
		futureText.setOnClickListener(this);
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
			tempText.setText("同步中...");
			String weatherCode = getIntent().getStringExtra("weather_code");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		case R.id.future:
			Intent intent_future = new Intent(this, FutureWeatherActivity.class);
			// intent_future.putExtra("city_name", ci);
			startActivity(intent_future);
			break;
		default:
			break;
		}
	}

	// 查询天气代号所对应的天气。
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey="
				+ weatherCode;
		queryFromServer(address);
	}

	// 根据传入的地址和类型去向服务器查询天气信息
	private void queryFromServer(final String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				// 处理服务器返回的天气信息
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
						tempText.setText("同步失败");
					}
				});
			}
		});
	}

	// 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		tempText.setText(prefs.getString("temp", "") + "℃");
		windyText.setText(prefs.getString("fengxiang", "") + "/"
				+ prefs.getString("fengli", ""));
		weatherTypeText.setText(prefs.getString("type", ""));
		futureText.setText("未来五天  >");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
		intent.putExtra("weather_code", weatherCode);
		startService(intent);
	}

	@Override
	protected void onDestroy() {
		//退出时候关闭服务
		WeatherActivity.this.stopService(new Intent(WeatherActivity.this,
				AutoUpdateService.class));
		super.onDestroy();
	}
}

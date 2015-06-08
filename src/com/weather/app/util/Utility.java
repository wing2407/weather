package com.weather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utility {
	
	/*
	json格式类似为
	{"desc":"OK","status":1000,"data":{"wendu":"30","ganmao":"各项气象条件适宜，发生感冒机率较低。但请避免长期处于空调房间中，以防感冒。",
	"forecast":[{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 32℃","type":"多云","low":"低温 28℃","date":"9日星期二"},
	{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 32℃","type":"多云","low":"低温 28℃","date":"10日星期三"},
	{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 32℃","type":"雷阵雨","low":"低温 28℃","date":"11日星期四"},
	{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 32℃","type":"雷阵雨","low":"低温 27℃","date":"12日星期五"},
	{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 31℃","type":"雷阵雨","low":"低温 27℃","date":"13日星期六"}],
	"yesterday":{"fl":"微风","fx":"无持续风向","high":"高温 32℃","type":"多云","low":"低温 28℃","date":"8日星期一"},"city":"香港"}}
	*/
	
	// 解析服务器返回的JSON数据
	public static void handleWeatherResponse(Context context, String response) {
		try {
			// 将天气信息存到sharepreference
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(context).edit();
			editor.putBoolean("city_selected", true);
			
			//解析data对象并存到editor里
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("data");
		
			editor.putString("city_name", weatherInfo.getString("city"));
			editor.putString("temp", weatherInfo.getString("wendu"));
			editor.putString("ganmao", weatherInfo.getString("ganmao"));
			
			JSONObject jsonObject2 = weatherInfo.getJSONObject("yesterday");
			editor.putString("fengxiang", jsonObject2.getString("fx"));
			editor.putString("fengli", jsonObject2.getString("fl"));
			editor.putString("high", jsonObject2.getString("high"));
			editor.putString("type", jsonObject2.getString("type"));
			editor.putString("low", jsonObject2.getString("low"));
			editor.putString("data", jsonObject2.getString("date"));
			
			//解析forecast数组并存到editor里
			JSONArray jsonArray = weatherInfo.getJSONArray("forecast");
			for(int i = 0; i < jsonArray.length(); i++){
				JSONObject item = jsonArray.getJSONObject(i);							
				editor.putString("fengxiang" + i, item.getString("fengxiang"));
				editor.putString("fengli" + i, item.getString("fengli"));
				editor.putString("high" + i, item.getString("high"));
				editor.putString("type" + i, item.getString("type"));
				editor.putString("low" + i, item.getString("low"));
				editor.putString("date" + i, item.getString("date"));
				Log.d("text", i+item.getString("fengli"));
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
			editor.putString("current_date", sdf.format(new Date()));	//当前日期
			editor.commit();	//保存editor里的数据
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}

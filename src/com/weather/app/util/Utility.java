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
	json��ʽ����Ϊ
	{"desc":"OK","status":1000,"data":{"wendu":"30","ganmao":"���������������ˣ�������ð���ʽϵ͡�������ⳤ�ڴ��ڿյ������У��Է���ð��",
	"forecast":[{"fengxiang":"�޳�������","fengli":"΢�缶","high":"���� 32��","type":"����","low":"���� 28��","date":"9�����ڶ�"},
	{"fengxiang":"�޳�������","fengli":"΢�缶","high":"���� 32��","type":"����","low":"���� 28��","date":"10��������"},
	{"fengxiang":"�޳�������","fengli":"΢�缶","high":"���� 32��","type":"������","low":"���� 28��","date":"11��������"},
	{"fengxiang":"�޳�������","fengli":"΢�缶","high":"���� 32��","type":"������","low":"���� 27��","date":"12��������"},
	{"fengxiang":"�޳�������","fengli":"΢�缶","high":"���� 31��","type":"������","low":"���� 27��","date":"13��������"}],
	"yesterday":{"fl":"΢��","fx":"�޳�������","high":"���� 32��","type":"����","low":"���� 28��","date":"8������һ"},"city":"���"}}
	*/
	
	// �������������ص�JSON����
	public static void handleWeatherResponse(Context context, String response) {
		try {
			// ��������Ϣ�浽sharepreference
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(context).edit();
			editor.putBoolean("city_selected", true);
			
			//����data���󲢴浽editor��
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
			
			//����forecast���鲢�浽editor��
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
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
			editor.putString("current_date", sdf.format(new Date()));	//��ǰ����
			editor.commit();	//����editor�������
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}

package com.weather.app.util;

import android.text.TextUtils;

import com.weather.app.model.City;
import com.weather.app.model.County;
import com.weather.app.model.Province;
import com.weather.app.model.WeatherDB;

public class Utility {

	// �������ص�ʡ������
	public synchronized static boolean handleProvincesResponse(
			WeatherDB weatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// �������ݴ浽Province��
					weatherDB.saveProvince(province);
				}
				return true;
			}
		}

		return false;
	}

	// �������ص��м�����
	public static boolean handleCitiesResponse(WeatherDB weatherDB,
			String response, int provinceId) {

		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// �������ݴ浽City��
					weatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	// �������ص��ؼ�����
	public static boolean handleCountiesResponse(WeatherDB weatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// ���������������ݴ洢��County��
					weatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}

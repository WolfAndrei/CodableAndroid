package com.example.codable.mock

import com.example.codable.Codable
import com.example.codable.CodingKey


class AppInfoConfig : Codable() {
	var deviceUUID: String? = null
	var authToken: String? = null
	var cityInfo: CityInfo? = CityInfo("spb")
	var appInfoConfigVersion: Int = APP_INFO_CONFIG_VERSION
	
	enum class CodingKeys {
		@CodingKey("deviceUUID") DEVICE_UUID,
		@CodingKey("authToken") AUTH_TOKEN,
		@CodingKey("cityInfo") CITY_INFO,
		@CodingKey("appInfoConfigVersion") APP_INFO_CONFIG_VERSION
	}
	
	companion object {
		private const val LOG_TAG = "AppInfoConfig"
		private const val APP_INFO_CONFIG_VERSION = 1
	}
}
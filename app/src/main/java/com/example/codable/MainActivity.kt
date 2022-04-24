package com.example.codable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.codable.mock.AppInfoConfig

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		exampleOf("Forward cast") {
			val appInfo = AppInfoConfig()
			println("AppInfoConfig: ${appInfo.encode()}")
		}
		
		exampleOf("Inverse cast") {
			val jsonString = "{\"APP_INFO_CONFIG_VERSION\":2,\"AUTH_TOKEN\":\"long long token in hex\",\"CITY_INFO\":\"{\\\"CITY_TEXT_ID\\\":\\\"msk\\\"}\",\"DEVICE_UUID\":null}"
			val appInfo = Codable.decode(AppInfoConfig::class, jsonString) as? AppInfoConfig
			println("AppInfoConfig version: ${appInfo?.appInfoConfigVersion}")
			println("AppInfoConfig authToken: ${appInfo?.authToken}")
			println("AppInfoConfig cityInfo-textId: ${appInfo?.cityInfo?.cityTextId}")
			println("AppInfoConfig deviceUUID: ${appInfo?.deviceUUID}")
			println("AppInfoConfig back to string: ${appInfo?.encode()}")
		}
	}
}

fun exampleOf(msg: String, action: () -> Unit) {
	println("\n-----------BEGIN $msg-----------")
	action()
	println("-----------END $msg-----------\n")
}


package com.example.codable.mock

import com.example.codable.Codable
import com.example.codable.CodingKey

data class CityInfo(var cityTextId: String? = null) : Codable() {
	enum class CodingKeys {
		@CodingKey("cityTextId") CITY_TEXT_ID
	}
}
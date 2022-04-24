package com.example.codable

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

open class Codable {
	private fun getCodingInfo():  Map<String?, Triple<String?, KType?, Any?>> {
		val codingKeysEnum = this::class.java.declaredClasses.filterNot { it.isEnum && it.name == CODING_KEYS_ENUM_NAME }.firstOrNull()
		val fields = codingKeysEnum?.fields
		val classFields = this::class.declaredMemberProperties
		
		val dictionary = fields?.associate { field ->
			val codingKey = field.name
			val classFieldName = field.annotations.filterIsInstance<CodingKey>().firstOrNull()?.serializableField
			@Suppress("UNCHECKED_CAST")
			val classField = classFields.firstOrNull { it.name == classFieldName } as? KProperty1<Any, *>
			classFieldName to Triple(codingKey, classField?.returnType, classField?.get(this))
		}
		return dictionary ?: mapOf()
	}
	
	fun encode(): String {
		val sw = StringWriter()
		val jsonEncoder = JsonWriter(sw)
		jsonEncoder.apply {
			beginObject()
			val codingInfo = getCodingInfo()
			codingInfo.forEach {
				val type = it.value.second?.jvmErasure
				val value = it.value.third
				when (type) {
					String::class -> { name(it.value.first).value(value as? String) }
					Boolean::class -> { name(it.value.first).value(value as? Boolean ?: false) }
					Int::class -> { name(it.value.first).value(value as? Int) }
					Long::class -> { name(it.value.first).value(value as? Long) }
					Float::class -> { name(it.value.first).value(value as? Float) }
					Codable::class -> { name(it.value.first).value((value as? Codable)?.encode()) }
					else -> {
						if (type?.isSubclassOf(Codable::class) == true) {
							name(it.value.first).value((value as? Codable)?.encode())
						}
					}
				}
			}
			endObject()
		}
		return sw.buffer.toString()
	}
	
	@Suppress("UNCHECKED_CAST")
	companion object {
		private const val CODING_KEYS_ENUM_NAME = "CodingKeys"
		
		private fun getCodingInfo(type: KClass<*>): Map<String, Pair<String?, KType?>> {
			val codingKeysEnum = type.java.declaredClasses.filterNot { it.isEnum && it.name == CODING_KEYS_ENUM_NAME }.firstOrNull()
			val fields = codingKeysEnum?.fields
			val classFields = type.members
			val dictionary = fields?.associate { field ->
				val codingKey = field.name
				val classFieldName = field.annotations.filterIsInstance<CodingKey>().firstOrNull()?.serializableField
				val classField = classFields.firstOrNull { it.name == classFieldName } as? KProperty1<Any, *>
				codingKey to Pair(classFieldName, classField?.returnType)
			}
			return dictionary ?: mapOf()
		}
		
		private fun getClassProperty(classObject: Any, name: String?, type: KType?) :  KMutableProperty1<Any, *>? {
			val classProperties = classObject::class.declaredMemberProperties as? List<KMutableProperty1<Any, *>>
			return classProperties?.firstOrNull { it.name == name && it.returnType == type }
		}
		
		fun decode(type: KClass<*>, stringValue: String): Codable? {
			val codingInfo = getCodingInfo(type)
			val codable = type.createInstance()
			
			val jsonReader = JsonReader(StringReader(stringValue))
			jsonReader.beginObject()
			while (jsonReader.hasNext()) {
				if (jsonReader.peek() == JsonToken.NULL) {
					jsonReader.skipValue()
				} else {
					val name = jsonReader.nextName()
					try {
						val value = codingInfo.getOrNull(name)
						value ?: return null
						val classProperty = getClassProperty(codable, value.first, value.second) ?: return null
						when (val returnType = classProperty.returnType.jvmErasure) {
							String::class -> {
								(classProperty as? KMutableProperty1<Any, String>)?.set(codable, jsonReader.nextString())
							}
							Boolean::class -> {
								(classProperty as? KMutableProperty1<Any, Boolean>)?.set(codable, jsonReader.nextBoolean())
							}
							Int::class -> {
								(classProperty as? KMutableProperty1<Any, Int>)?.set(codable, jsonReader.nextInt())
							}
							Long::class -> {
								(classProperty as? KMutableProperty1<Any, Long>)?.set(codable, jsonReader.nextLong())
							}
							Double::class -> {
								(classProperty as? KMutableProperty1<Any, Double>)?.set(codable, jsonReader.nextDouble())
							}
							else -> {
								if (returnType.isSubclassOf(Codable::class)) {
									val obj = decode(returnType, jsonReader.nextString())
									obj?.let { (classProperty as? KMutableProperty1<Any, Any>)?.set(codable, it) }
								}
							}
						}
					} catch (ex: java.lang.Exception) {
					}
				}
			}
			jsonReader.endObject()
			return codable as? Codable
		}
	}
}
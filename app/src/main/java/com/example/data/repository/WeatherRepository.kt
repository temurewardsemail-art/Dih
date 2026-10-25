package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class WeatherData(
    val cityName: String = "San Francisco",
    val temperatureF: Int = 68,
    val condition: String = "Partly Cloudy",
    val conditionCode: String = "cloud_sun", // sun, cloud_sun, rain, snow, mist
    val highF: Int = 72,
    val lowF: Int = 54,
    val humidity: Int = 62
) {
    fun getTempDisplay(isCelsius: Boolean): String {
        return if (isCelsius) {
            val c = ((temperatureF - 32) * 5) / 9
            "${c}°C"
        } else {
            "${temperatureF}°F"
        }
    }
}

class WeatherRepository {
    private val cityDatabase = mapOf(
        "Atlanta" to WeatherData("Atlanta", 72, "Sunny & Clear", "sun", 76, 56, 52),
        "San Francisco" to WeatherData("San Francisco", 68, "Partly Cloudy", "cloud_sun", 72, 54, 62),
        "Los Angeles" to WeatherData("Los Angeles", 75, "Sunny & Warm", "sun", 80, 60, 45),
        "New York" to WeatherData("New York", 62, "Breezy Clear", "sun", 66, 50, 50),
        "Chicago" to WeatherData("Chicago", 56, "Scattered Clouds", "cloud_sun", 60, 48, 58),
        "Miami" to WeatherData("Miami", 82, "Tropical Sun", "sun", 86, 74, 70),
        "Dallas" to WeatherData("Dallas", 74, "Clear Sky", "sun", 78, 56, 48),
        "Denver" to WeatherData("Denver", 64, "Sunny Mountain Air", "sun", 68, 44, 35),
        "Phoenix" to WeatherData("Phoenix", 85, "Warm & Clear", "sun", 89, 65, 20),
        "Seattle" to WeatherData("Seattle", 55, "Misty Showers", "rain", 58, 47, 82),
        "Boston" to WeatherData("Boston", 60, "Cool Breeze", "cloud_sun", 64, 48, 55),
        "Las Vegas" to WeatherData("Las Vegas", 80, "Bright & Sunny", "sun", 84, 61, 22),
        "Orlando" to WeatherData("Orlando", 81, "Warm Sun", "sun", 85, 68, 65),
        "Honolulu" to WeatherData("Honolulu", 82, "Tropical Breeze", "sun", 85, 72, 60),
        "London" to WeatherData("London", 57, "Light Rain", "rain", 61, 49, 78),
        "Paris" to WeatherData("Paris", 59, "Overcast", "cloud_sun", 63, 51, 65),
        "Tokyo" to WeatherData("Tokyo", 64, "Clear Sky", "sun", 69, 52, 55),
        "Seoul" to WeatherData("Seoul", 61, "Clear & Crisp", "sun", 65, 48, 50),
        "Sydney" to WeatherData("Sydney", 73, "Pleasant & Sun", "sun", 78, 62, 52),
        "Toronto" to WeatherData("Toronto", 58, "Partly Cloudy", "cloud_sun", 62, 46, 54)
    )

    private val _currentWeather = MutableStateFlow(cityDatabase["Atlanta"] ?: cityDatabase["San Francisco"]!!)
    val currentWeather: StateFlow<WeatherData> = _currentWeather.asStateFlow()

    fun setCity(cityName: String) {
        val cleanName = cityName.trim()
        if (cleanName.isBlank()) return

        // Case-insensitive match against presets
        val matchedEntry = cityDatabase.entries.firstOrNull {
            it.key.equals(cleanName, ignoreCase = true) || cleanName.contains(it.key, ignoreCase = true)
        }

        if (matchedEntry != null) {
            _currentWeather.value = matchedEntry.value
        } else {
            // Capitalize each word properly for custom entered cities
            val formattedName = cleanName.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            _currentWeather.value = WeatherData(
                cityName = formattedName,
                temperatureF = 69,
                condition = "Clear Sky",
                conditionCode = "sun",
                highF = 73,
                lowF = 55,
                humidity = 55
            )
        }
    }

    fun getAvailableCities(): List<String> = cityDatabase.keys.toList()
}

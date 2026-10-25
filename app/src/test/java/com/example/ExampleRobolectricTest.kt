package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Personal TV Home", appName)
    }

    @Test
    fun `test weather repository city change`() {
        val weatherRepo = WeatherRepository()
        assertNotNull(weatherRepo.currentWeather.value)
        
        weatherRepo.setCity("New York")
        assertEquals("New York", weatherRepo.currentWeather.value.cityName)

        weatherRepo.setCity("Chicago")
        assertEquals("Chicago", weatherRepo.currentWeather.value.cityName)
        assertTrue(weatherRepo.currentWeather.value.getTempDisplay(false).contains("°F"))
    }

    @Test
    fun `test preferences repository defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsRepo = PreferencesRepository(context)
        val settings = prefsRepo.settings.value
        assertEquals(7, settings.priorityDockCount)
        assertTrue(settings.scenicGlassEnabled)
    }
}

package com.mario.skyeye.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.WeatherForecast
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test

@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherDaoTest {
    private lateinit var dao: WeatherDao
    private lateinit var db: AppDataBase
    private lateinit var currentWeatherResponse: CurrentWeatherResponse
    private lateinit var forecast: WeatherForecast
    private lateinit var favoriteLocation: FavoriteLocation

    @Before
    fun setup(){
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDataBase::class.java
        ).build()
        dao = db.weatherDao()
        currentWeatherResponse = mockk(relaxed = true)
        forecast = mockk(relaxed = true)
        favoriteLocation = FavoriteLocation(
            cityName = "London",
            latitude = 51.5074,
            longitude = -0.1278,
            currentWeatherResponse,
            forecast
        )
    }
    @After
    fun tearDown(){
        db.close()
    }
    @Test
    fun getAllFavoriteLocations_returnsLocations() = runTest {
        dao.insert(favoriteLocation)
        val locations = dao.getAllFavoriteLocations().firstOrNull()
        assertNotNull(locations)
        assertThat( locations?.size, `is`(1))
        assertThat(locations?.get(0)?.cityName,`is`("London"))
    }
    @Test
    fun getFavoriteLocationByCityName_returnsLocation() = runTest {
        dao.insert(favoriteLocation)
        val location = dao.getFavoriteLocationByCityName("London").firstOrNull()
        assertNotNull(location)
        assertThat(location?.cityName, `is`("London"))
        assertThat(location?.latitude, `is`(51.5074))
        assertThat(location?.longitude, `is`(-0.1278))
    }
    @Test
    fun deleteFavoriteLocation_deletesLocation_returnsRowCount() = runTest {
        dao.insert(favoriteLocation)
        dao.delete(favoriteLocation)
        val locations = dao.getAllFavoriteLocations().firstOrNull()
        assertNotNull(locations)
        assertThat(locations?.size, `is`(0))
    }
}
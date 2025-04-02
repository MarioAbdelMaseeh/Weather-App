package com.mario.skyeye.data.repo

import com.mario.skyeye.data.local.LocalDataSource
import com.mario.skyeye.data.models.Alarm
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.remote.RemoteDataSource
import com.mario.skyeye.data.sharedprefrence.AppPreference
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test
import kotlin.test.assertNotNull


class RepoImplTest {
    private lateinit var repoImpl: RepoImpl
    private lateinit var  remoteDataSourceImpl: RemoteDataSource
    private lateinit var localDataSourceImpl: LocalDataSource
    private lateinit var appPreference: AppPreference
    private lateinit var currentWeatherResponse: CurrentWeatherResponse
    private lateinit var forecast: WeatherForecast

    @Before
    fun setup() = runTest {
        localDataSourceImpl = mockk()
        remoteDataSourceImpl = mockk()
        appPreference = mockk()
        repoImpl = RepoImpl(remoteDataSourceImpl, localDataSourceImpl, appPreference)
        currentWeatherResponse = mockk(relaxed = true)
        forecast = mockk(relaxed = true)
    }
    @Test
    fun getAllFavoriteLocations_returnsLocations() = runTest {
        coEvery { localDataSourceImpl.getAllLocations() } returns flowOf(listOf(
            FavoriteLocation(
                cityName = "London",
                latitude = 51.5074,
                longitude = -0.1278,
                currentWeatherResponse = currentWeatherResponse,
                forecast
            ),
            FavoriteLocation(
                cityName = "Paris",
                latitude = 48.8566,
                longitude = 2.3522,
                currentWeatherResponse = currentWeatherResponse,
                forecast
            ),
            FavoriteLocation(
                cityName = "Rome",
                latitude = 41.9028,
                longitude = 12.4964,
                currentWeatherResponse = currentWeatherResponse,
                forecast
            )
        ))
        val locations = repoImpl.getAllLocations().firstOrNull()
        assertNotNull(locations)
        assertThat(locations.size, `is`(3))
        assertThat(locations[0]?.cityName, `is`("London"))
        assertThat(locations[1]?.cityName, `is`("Paris"))
        assertThat(locations[2]?.cityName, `is`("Rome"))
        coVerify { localDataSourceImpl.getAllLocations() }
    }

    @Test
    fun deleteFavoriteLocation_deletesLocation_returnsRowCount() = runTest {
        val location = mockk<FavoriteLocation>(relaxed = true)
        coEvery { localDataSourceImpl.deleteLocation(any()) } returns 1
        val result = repoImpl.deleteLocation(location)
        assertNotNull(result)
        assertThat(result, `is`(1))
        coVerify { localDataSourceImpl.deleteLocation(location) }
    }

    @Test
    fun insertFavoriteLocation_insertsLocation_returnsId() = runTest {
        val location = mockk<FavoriteLocation>(relaxed = true)
        coEvery { localDataSourceImpl.insertLocation(any()) } returns 1L
        val result = repoImpl.insertLocation(location)
        assertNotNull(result)
        assertThat(result, `is`(1L))
        coVerify { localDataSourceImpl.insertLocation(location) }
    }
    @Test
    fun getFavoriteLocationByCityName_returnsLocation() = runTest {
        coEvery { localDataSourceImpl.getFavoriteLocationByCityName(any()) } returns mockk(relaxed = true)
        val location = repoImpl.getFavoriteLocationByCityName("London")
        assertNotNull(location)
        coVerify { localDataSourceImpl.getFavoriteLocationByCityName("London") }
    }

    @Test
    fun insertAlarm_insertsAlarm_returnsId() = runTest {
        val alarm = mockk<Alarm>(relaxed = true)
        coEvery { localDataSourceImpl.insertAlarm(any()) } returns 1L
        val result = repoImpl.insertAlarm(alarm)
        assertNotNull(result)
        assertThat(result, `is`(1L))
        coVerify { localDataSourceImpl.insertAlarm(alarm) }
    }
    @Test
    fun updateAlarm_updatesAlarm_returnsUnit() = runTest {
        val alarm = mockk<Alarm>(relaxed = true)
        coEvery { localDataSourceImpl.updateAlarm(any()) } returns Unit
        repoImpl.updateAlarm(alarm)
        coVerify { localDataSourceImpl.updateAlarm(alarm) }
    }

    @Test
    fun deleteAlarm_deletesAlarm_returnsUnit() = runTest {
        val alarm = mockk<Alarm>(relaxed = true)
        coEvery { localDataSourceImpl.deleteAlarm(any()) } returns Unit
        repoImpl.deleteAlarm(alarm)
        coVerify { localDataSourceImpl.deleteAlarm(alarm) }
    }
    @Test
    fun getAllAlarms_returnsAlarms() = runTest {
        coEvery { localDataSourceImpl.getAllAlarms() } returns mockk(relaxed = true)
        val result = repoImpl.getAllAlarms()
        assertNotNull(result)
        coVerify { localDataSourceImpl.getAllAlarms() }
    }

    @Test
    fun deleteAlarmByLabel_deletesAlarm_returnsUnit() = runTest {
        coEvery { localDataSourceImpl.deleteAlarmByLabel(any()) } returns Unit
        repoImpl.deleteAlarmByLabel("label")
        coVerify { localDataSourceImpl.deleteAlarmByLabel("label") }
        coVerify(exactly = 0) { localDataSourceImpl.deleteAlarmByLabel("label1") }
    }
    @Test
    fun getAlarmByCreatedAt_returnsAlarm() = runTest {
        coEvery { localDataSourceImpl.getAlarmByCreatedAt(any()) } returns mockk(relaxed = true)
        val result = repoImpl.getAlarmByCreatedAt(1L)
        assertNotNull(result)
        coVerify { localDataSourceImpl.getAlarmByCreatedAt(1L) }
    }
    @Test
    fun getCurrentWeather_returnsCurrentWeather() = runTest {
        coEvery { remoteDataSourceImpl.getCurrentWeather(any(), any(), any()) } returns mockk(relaxed = true)
        val result = repoImpl.getCurrentWeather(1.0, 1.0, "metric")
        assertNotNull(result)
        coVerify { remoteDataSourceImpl.getCurrentWeather(1.0, 1.0, "metric") }
    }
    @Test
    fun getWeatherForecast_returnsWeatherForecast() = runTest {
        coEvery { remoteDataSourceImpl.getWeatherForecast(any(), any(), any()) } returns mockk(relaxed = true)
        val result = repoImpl.getWeatherForecast(1.0, 1.0, "metric")
        assertNotNull(result)
        coVerify { remoteDataSourceImpl.getWeatherForecast(1.0, 1.0, "metric") }
    }
}
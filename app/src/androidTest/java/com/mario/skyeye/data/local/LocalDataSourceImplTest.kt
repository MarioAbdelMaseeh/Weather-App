package com.mario.skyeye.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.mario.skyeye.data.models.Alarm
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class LocalDataSourceImplTest {
    private lateinit var localDataSourceImpl: LocalDataSourceImpl
    private lateinit var weatherDao: WeatherDao
    private lateinit var alarmDao: AlarmDao
    private lateinit var db: AppDataBase
    private lateinit var currentWeatherResponse: CurrentWeatherResponse
    private lateinit var forecast: WeatherForecast
    private lateinit var favoriteLocation: FavoriteLocation

    @Before
    fun setup(){
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDataBase::class.java
        ).allowMainThreadQueries().build()

        alarmDao = db.alarmDao()
        weatherDao = db.weatherDao()
        localDataSourceImpl = LocalDataSourceImpl(weatherDao, alarmDao)
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
    fun getAllFavoriteLocations() = runTest {
        localDataSourceImpl.insertLocation(favoriteLocation)
        val locations = localDataSourceImpl.getAllLocations().firstOrNull()
        assertNotNull(locations)
        assertThat( locations?.size, `is`(1))
        assertThat(locations?.get(0)?.cityName,`is`("London"))
    }

    @Test
    fun getFavoriteLocationByCityName() = runTest {
        localDataSourceImpl.insertLocation(favoriteLocation)
        val location = localDataSourceImpl.getFavoriteLocationByCityName("London").firstOrNull()
        assertNotNull(location)
        assertThat(location?.cityName, `is`("London"))
        assertThat(location?.latitude, `is`(51.5074))
        assertThat(location?.longitude, `is`(-0.1278))
    }

    @Test
    fun deleteFavoriteLocation() = runTest {
        localDataSourceImpl.insertLocation(favoriteLocation)
        localDataSourceImpl.deleteLocation(favoriteLocation)
        val locations = localDataSourceImpl.getAllLocations().firstOrNull()
        assertNotNull(locations)
        assertThat(locations?.size, `is`(0))
    }

    @Test
    fun insertAlarm() = runTest {
        val alarm = Alarm(
            label = "Morning Alarm",
            triggerTime = 10,
            isEnabled = true,
            repeatInterval = 0,
            createdAt =20
        )
        val id = localDataSourceImpl.insertAlarm(alarm)
        assertThat(id, `is`(20L))

        val alarms = localDataSourceImpl.getAllAlarms().firstOrNull()
        assertNotNull(alarms)
        assertThat(alarms?.size, `is`(1))
        assertThat(alarms?.get(0)?.label, `is`("Morning Alarm"))
        assertThat(alarms?.get(0)?.isEnabled, `is`(true))
        assertThat(alarms?.get(0)?.repeatInterval, `is`(0))
        assertThat(alarms?.get(0)?.createdAt, `is`(20))
        assertThat(alarms?.get(0)?.triggerTime, `is`(10))
    }
    @Test
    fun updateAlarm() = runTest {
        val sentAlarm = Alarm(
            label = "Morning Alarm",
            triggerTime = 10,
            isEnabled = true,
            repeatInterval = 0,
            createdAt =20
        )
        localDataSourceImpl.insertAlarm(sentAlarm)
        localDataSourceImpl.updateAlarm(sentAlarm.copy(isEnabled = false, label = "Evening Alarm"))
        val receivedAlarm = localDataSourceImpl.getAlarmByCreatedAt(20L).firstOrNull()
        assertNotNull(receivedAlarm)
        assertThat(receivedAlarm?.isEnabled, `is`(false))
        assertThat(receivedAlarm?.label, `is`("Evening Alarm"))
        assertThat(receivedAlarm?.repeatInterval, `is`(0))
        assertThat(receivedAlarm?.createdAt, `is`(20))
        assertThat(receivedAlarm?.triggerTime, `is`(10))
    }
    @Test
    fun deleteAlarm() = runTest {
        val alarm = Alarm(
            label = "Morning Alarm",
            triggerTime = 10,
            isEnabled = true,
            repeatInterval = 0,
            createdAt = 20
        )
        localDataSourceImpl.insertAlarm(alarm)
        val alarmsBeforeDelete = localDataSourceImpl.getAllAlarms().firstOrNull()
        assertNotNull(alarmsBeforeDelete)
        assertThat(alarmsBeforeDelete?.size, `is`(1))

        localDataSourceImpl.deleteAlarm(alarm)
        val alarms = localDataSourceImpl.getAllAlarms().firstOrNull()
        assertNotNull(alarms)
        assertThat(alarms?.size, `is`(0))
    }
    @Test
    fun getAllAlarms() = runTest {
        val alarm1 = Alarm(
            label = "Morning Alarm",
            triggerTime = 10,
            isEnabled = true,
            repeatInterval = 0,
            createdAt = 1
        )
        val alarm2 = Alarm(
            label = "Evening Alarm",
            triggerTime = 10,
            isEnabled = false,
            repeatInterval = 1,
            createdAt = 2
        )
        localDataSourceImpl.insertAlarm(alarm1)
        localDataSourceImpl.insertAlarm(alarm2)
        val alarms = localDataSourceImpl.getAllAlarms().firstOrNull()
        assertNotNull(alarms)
        assertThat(alarms?.size, `is`(2))
        assertThat(alarms?.get(0)?.label, `is`("Morning Alarm"))
        assertThat(alarms?.get(0)?.isEnabled, `is`(true))
        assertThat(alarms?.get(0)?.repeatInterval, `is`(0))
        assertThat(alarms?.get(0)?.createdAt, `is`(1))
        assertThat(alarms?.get(0)?.triggerTime, `is`(10))
        assertThat(alarms?.get(1)?.label, `is`("Evening Alarm"))
        assertThat(alarms?.get(1)?.isEnabled, `is`(false))
        assertThat(alarms?.get(1)?.repeatInterval, `is`(1))
        assertThat(alarms?.get(1)?.createdAt, `is`(2))
    }

    @Test
    fun deleteAlarmByLabel() = runTest {
        val alarm1 = Alarm(
            label = "Morning Alarm",
            triggerTime = 10,
            isEnabled = true,
            repeatInterval = 0,
            createdAt = 1
        )
        val alarm2 = Alarm(
            label = "Evening Alarm",
            triggerTime = 10,
            isEnabled = false,
            repeatInterval = 1,
            createdAt = 2
        )
        localDataSourceImpl.insertAlarm(alarm1)
        localDataSourceImpl.insertAlarm(alarm2)
        localDataSourceImpl.deleteAlarmByLabel("Morning Alarm")
        val alarms = localDataSourceImpl.getAllAlarms().firstOrNull()
        assertNotNull(alarms)
        assertThat(alarms?.size, `is`(1))
        assertThat(alarms?.get(0)?.label, `is`("Evening Alarm"))
        assertThat(alarms?.get(0)?.isEnabled, `is`(false))
        assertThat(alarms?.get(0)?.repeatInterval, `is`(1))
        assertThat(alarms?.get(0)?.createdAt, `is`(2))
    }

}
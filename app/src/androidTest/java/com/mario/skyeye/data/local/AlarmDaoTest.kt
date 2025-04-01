package com.mario.skyeye.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.mario.skyeye.data.models.Alarm
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.flow.firstOrNull
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class AlarmDaoTest {
    private lateinit var dao: AlarmDao
    private lateinit var db: AppDataBase

    @Before
    fun setup(){
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDataBase::class.java
        ).build()
        dao = db.alarmDao()

    }
    @After
    fun tearDown(){
        db.close()
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
        val id = dao.insertAlarm(alarm)
        assertThat(id, `is`(20L))

        val alarms = dao.getAllAlarms().firstOrNull()
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
        dao.insertAlarm(sentAlarm)
        dao.updateAlarm(sentAlarm.copy(isEnabled = false, label = "Evening Alarm"))
        val receivedAlarm = dao.getAlarmByCreatedAt(20L).firstOrNull()
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
        dao.insertAlarm(alarm)
        val alarmsBeforeDelete = dao.getAllAlarms().firstOrNull()
        assertNotNull(alarmsBeforeDelete)
        assertThat(alarmsBeforeDelete?.size, `is`(1))

        dao.deleteAlarm(alarm)
        val alarms = dao.getAllAlarms().firstOrNull()
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
        dao.insertAlarm(alarm1)
        dao.insertAlarm(alarm2)
        val alarms = dao.getAllAlarms().firstOrNull()
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
        dao.insertAlarm(alarm1)
        dao.insertAlarm(alarm2)
        dao.deleteAlarmByLabel("Morning Alarm")
        val alarms = dao.getAllAlarms().firstOrNull()
        assertNotNull(alarms)
        assertThat(alarms?.size, `is`(1))
        assertThat(alarms?.get(0)?.label, `is`("Evening Alarm"))
        assertThat(alarms?.get(0)?.isEnabled, `is`(false))
        assertThat(alarms?.get(0)?.repeatInterval, `is`(1))
        assertThat(alarms?.get(0)?.createdAt, `is`(2))
    }
}
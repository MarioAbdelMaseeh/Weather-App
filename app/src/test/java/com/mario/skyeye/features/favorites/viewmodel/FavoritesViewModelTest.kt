package com.mario.skyeye.features.favorites.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mario.skyeye.data.models.CurrentWeatherResponse
import com.mario.skyeye.data.models.FavoriteLocation
import com.mario.skyeye.data.models.Response
import com.mario.skyeye.data.models.WeatherForecast
import com.mario.skyeye.data.repo.Repo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FavoritesViewModelTest {

    private lateinit var viewModel: FavoritesViewModel
    private val repo: Repo = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var currentWeatherResponse: CurrentWeatherResponse
    private lateinit var forecastResponse: WeatherForecast


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FavoritesViewModel(repo)
        currentWeatherResponse = mockk(relaxed = true)
        forecastResponse = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchFavoriteLocations should emit Loading then Success with sorted locations`() = runTest {

        val testLocations = listOf(
            FavoriteLocation(
                cityName = "Berlin",
                latitude = 52.5200,
                longitude = 13.4050,
                currentWeatherResponse = currentWeatherResponse,
                forecastResponse = forecastResponse
            ),
            FavoriteLocation(cityName = "Amsterdam",
                latitude = 52.3702,
                longitude = 4.8952,
                currentWeatherResponse = currentWeatherResponse,
                forecastResponse = forecastResponse
            ),
            FavoriteLocation(cityName = "home"
                , latitude = 0.0,
                longitude = 0.0,
                currentWeatherResponse = currentWeatherResponse,
                forecastResponse = forecastResponse
            )
        )

        // Arrange

        coEvery { repo.getAllLocations() } returns flowOf(testLocations)

        // Act
        viewModel.fetchFavoriteLocations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val expected = listOf(
            FavoriteLocation(
                cityName = "Berlin",
                latitude = 52.5200,
                longitude = 13.4050,
                currentWeatherResponse = currentWeatherResponse,
                forecastResponse = forecastResponse
            ),
            FavoriteLocation(cityName = "Amsterdam",
                latitude = 52.3702,
                longitude = 4.8952,
                currentWeatherResponse = currentWeatherResponse,
                forecastResponse = forecastResponse
            )
        ).sortedBy { it.cityName }
        assertEquals(
            Response.Success(expected),
            viewModel.favoriteLocations.value
        )
        coVerify { repo.getAllLocations() }
    }

    @Test
    fun `fetchFavoriteLocations should emit Loading then Failure on error`() = runTest {
        // Arrange
        val errorMessage = "Network error"
        coEvery { repo.getAllLocations() } returns flow { throw RuntimeException(errorMessage) }

        // Act
        viewModel.fetchFavoriteLocations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(
            Response.Failure(errorMessage),
            viewModel.favoriteLocations.value
        )
    }

    @Test
    fun `insertLocation should call repo and refresh locations`() = runTest {
        // Arrange
        val newLocation = FavoriteLocation(cityName = "Paris",
            latitude = 48.8566,
            longitude = 2.3522,
            currentWeatherResponse = currentWeatherResponse,
            forecastResponse = forecastResponse)

        coEvery { repo.insertLocation(newLocation) } returns 1L
        coEvery { repo.getAllLocations() } returns flowOf(emptyList())

        // Act
        viewModel.insertLocation(newLocation)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) {
            repo.insertLocation(newLocation)
        }
        coVerify(exactly = 1) {
            repo.getAllLocations()
        }
    }

    @Test
    fun `deleteLocation should call repo and refresh locations`() = runTest {
        // Arrange
        val locationToDelete = FavoriteLocation(cityName = "London"
            , latitude = 51.5074,
            longitude = -0.1278,
            currentWeatherResponse = currentWeatherResponse,
            forecastResponse = forecastResponse)

        coEvery { repo.deleteLocation(locationToDelete) } returns 1
        coEvery { repo.getAllLocations() } returns flowOf(emptyList())

        // Act
        viewModel.deleteLocation(locationToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) {
            repo.deleteLocation(locationToDelete)
        }
        coVerify(exactly = 1) {
            repo.getAllLocations()
        }
    }

    @Test
    fun `factory should create ViewModel with given repository`() {
        // Arrange
        val factory = FavoritesFactory(repo)

        // Act
        val createdViewModel = factory.create(FavoritesViewModel::class.java)

        // Assert
        assertEquals(viewModel::class.java, createdViewModel::class.java)
    }
}
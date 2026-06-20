package io.github.lucaspaixaodev.garageservice.infra.output.gateway.garage

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageApiException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarageClientTest {

    private val builder = mockk<RestClient.Builder>()
    private val restClient = mockk<RestClient>()
    private val uriSpec = mockk<RestClient.RequestHeadersUriSpec<*>>()
    private val responseSpec = mockk<RestClient.ResponseSpec>()
    private lateinit var client: GarageClient

    @BeforeEach
    fun setUp() {
        every { builder.baseUrl(any<String>()) } returns builder
        every { builder.build() } returns restClient
        every { restClient.get() } returns uriSpec
        every { uriSpec.uri("/garage") } returns uriSpec
        every { uriSpec.retrieve() } returns responseSpec
        client = GarageClient(restClientBuilder = builder, baseUrl = "http://garage-api")
    }

    @Test
    fun `fetch maps the api response into domain data`() {
        val response =
            GarageResponse(
                garage =
                    listOf(
                        GarageResponse.GarageItem(
                            sector = "A",
                            basePrice = BigDecimal("40.5"),
                            maxCapacity = 10,
                            openHour = "00:00",
                            closeHour = "23:59",
                            durationLimitMinutes = 1440,
                        ),
                    ),
                spots =
                    listOf(
                        GarageResponse.SpotItem(id = 1, sector = "A", lat = -23.561684, lng = -46.655981, occupied = true),
                        GarageResponse.SpotItem(id = 2, sector = "A", lat = -23.561664, lng = -46.655961, occupied = false),
                    ),
            )
        every { responseSpec.body(GarageResponse::class.java) } returns response

        val data = client.fetch()

        assertEquals(1, data.garages.size)
        assertEquals("A", data.garages.single().sector)
        assertEquals(BigDecimal("40.5"), data.garages.single().basePrice)
        assertEquals(2, data.spots.size)
        assertEquals(1, data.spots.first().externalId)
        assertEquals(-23.561684, data.spots.first().latitude)
        assertTrue(data.spots.first().occupied)
        assertFalse(data.spots[1].occupied)
    }

    @Test
    fun `fetch throws EmptyResponse when the body is null`() {
        every { responseSpec.body(GarageResponse::class.java) } returns null

        assertFailsWith<GarageApiException.EmptyResponse> { client.fetch() }
    }

    @Test
    fun `fetch throws FetchFailed when the api call fails`() {
        every { responseSpec.body(GarageResponse::class.java) } throws RestClientException("connection refused")

        val exception = assertFailsWith<GarageApiException.FetchFailed> { client.fetch() }

        assertTrue(exception.message!!.startsWith("Failed to fetch data from the garage API"))
    }
}

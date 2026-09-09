package com.ktcloud.travelplanner.maps.controller

import com.ktcloud.travelplanner.place.port.PlaceLocationPort
import com.ktcloud.travelplanner.route.port.RouteCalculation
import com.ktcloud.travelplanner.route.port.RouteCalculationPort
import com.ktcloud.travelplanner.route.port.RouteCalculationWaypoint
import com.ktcloud.travelplanner.route.port.RouteLegCalculation
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InternalMapsControllerTest {
	private val routeCalculationPort = mock(RouteCalculationPort::class.java)
	private val placeLocationPort = mock(PlaceLocationPort::class.java)
	private val controller = InternalMapsController(routeCalculationPort, placeLocationPort)

	@Test
	fun `preview request keeps waypoint order and maps response`() {
		val request = RoutePreviewRequest(
			waypoints = listOf(
				RoutePreviewWaypoint("place-a", 37.5665, 126.9780),
				RoutePreviewWaypoint("place-b", 37.5700, 126.9900),
				RoutePreviewWaypoint("place-c", 37.5750, 126.9950),
			),
		)
		val result = RouteCalculation(
			encodedPolyline = null,
			encodedPolylines = listOf("segment-a-b", "segment-b-c"),
			totalDistanceMeters = 2400,
			totalDurationSeconds = 1800,
			legs = listOf(RouteLegCalculation(1200, 900), RouteLegCalculation(1200, 900)),
			warnings = listOf("주의"),
		)
		val expectedWaypoints = listOf(
			RouteCalculationWaypoint("place-a", 37.5665, 126.9780),
			RouteCalculationWaypoint("place-b", 37.5700, 126.9900),
			RouteCalculationWaypoint("place-c", 37.5750, 126.9950),
		)
		`when`(routeCalculationPort.calculatePreviewRoute(expectedWaypoints)).thenReturn(result)

		val response = controller.calculatePreviewRoute(request)

		verify(routeCalculationPort).calculatePreviewRoute(
			expectedWaypoints,
		)
		assertEquals(listOf("segment-a-b", "segment-b-c"), response.encodedPolylines)
		assertEquals(2400, response.totalDistanceMeters)
		assertEquals(1800, response.totalDurationSeconds)
		assertEquals(listOf(1200L, 1200L), response.legs.map { it.distanceMeters })
		assertEquals(listOf("주의"), response.warnings)
	}

	@Test
	fun `preview response preserves empty route`() {
		val request = RoutePreviewRequest(
			waypoints = listOf(
				RoutePreviewWaypoint("place-a", 37.5665, 126.9780),
				RoutePreviewWaypoint("place-b", 37.5700, 126.9900),
			),
		)
		val expectedWaypoints = listOf(
			RouteCalculationWaypoint("place-a", 37.5665, 126.9780),
			RouteCalculationWaypoint("place-b", 37.5700, 126.9900),
		)
		`when`(routeCalculationPort.calculatePreviewRoute(expectedWaypoints)).thenReturn(
			RouteCalculation(null, emptyList(), 0, 0, emptyList(), emptyList()),
		)

		val response = controller.calculatePreviewRoute(request)

		assertTrue(response.encodedPolylines.isEmpty())
		assertTrue(response.legs.isEmpty())
		assertEquals(0, response.totalDistanceMeters)
		assertEquals(0, response.totalDurationSeconds)
	}
}

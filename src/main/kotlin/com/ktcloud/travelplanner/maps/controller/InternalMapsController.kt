package com.ktcloud.travelplanner.maps.controller

import com.ktcloud.travelplanner.place.port.PlaceLocationPort
import com.ktcloud.travelplanner.route.model.TransportationType
import com.ktcloud.travelplanner.route.port.RouteCalculationPort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/maps")
class InternalMapsController(
	private val routeCalculationPort: RouteCalculationPort,
	private val placeLocationPort: PlaceLocationPort,
) {
	@PostMapping("/routes/calculate")
	fun calculateRoute(@RequestBody request: RouteCalculationRequest): RouteCalculationResponse {
		val result = routeCalculationPort.calculateRoute(request.googlePlaceIds, request.transportationType)
		return RouteCalculationResponse(
			encodedPolyline = result.encodedPolyline,
			encodedPolylines = result.encodedPolylines,
			totalDistanceMeters = result.totalDistanceMeters,
			totalDurationSeconds = result.totalDurationSeconds,
			legs = result.legs.map { RouteLegResponse(it.distanceMeters, it.durationSeconds) },
			warnings = result.warnings,
		)
	}

	@GetMapping("/places/{googlePlaceId}/location")
	fun findLocation(@PathVariable googlePlaceId: String): ResponseEntity<PlaceLocationResponse> =
		placeLocationPort.findLocation(googlePlaceId)
			?.let { ResponseEntity.ok(PlaceLocationResponse(it.latitude.toPlainString(), it.longitude.toPlainString())) }
			?: ResponseEntity.notFound().build()
}

data class RouteCalculationRequest(val googlePlaceIds: List<String>, val transportationType: TransportationType)
data class RouteCalculationResponse(val encodedPolyline: String?, val encodedPolylines: List<String>, val totalDistanceMeters: Long, val totalDurationSeconds: Long, val legs: List<RouteLegResponse>, val warnings: List<String>)
data class RouteLegResponse(val distanceMeters: Long, val durationSeconds: Long)
data class PlaceLocationResponse(val latitude: String, val longitude: String)

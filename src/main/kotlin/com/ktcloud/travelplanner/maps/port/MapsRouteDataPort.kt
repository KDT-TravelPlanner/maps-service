package com.ktcloud.travelplanner.maps.port

import com.ktcloud.travelplanner.timeline.model.TimelineItem
import com.ktcloud.travelplanner.travel.model.Travel
import java.util.UUID

interface MapsRouteDataPort {
    fun findReadableTravel(travelId: UUID, requesterId: UUID): Travel
    fun findWaypoints(travelId: UUID, dayNumber: Int): List<TimelineItem>
}

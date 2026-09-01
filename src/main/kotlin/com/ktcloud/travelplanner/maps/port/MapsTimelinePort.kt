package com.ktcloud.travelplanner.maps.port

import java.util.UUID

interface MapsTimelinePort {
    fun findMapItems(travelId: UUID, dayNumber: Int): List<MapsTimelineItem>
}

data class MapsTimelineItem(
    val timelineItemId: UUID,
    val visitOrder: Int,
    val name: String,
    val googlePlaceId: String?,
)

package com.ktcloud.travelplanner.maps.port

import java.util.UUID

interface MapsTravelPort {
    fun findReadableTravel(travelId: UUID, requesterId: UUID): MapsTravelReference
}

data class MapsTravelReference(val travelId: UUID, val travelDays: Int)

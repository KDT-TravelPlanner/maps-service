package com.ktcloud.travelplanner.maps.port

import java.util.UUID

interface MapsMembershipPort {
    fun canRead(travelId: UUID, userId: UUID): Boolean
}

package com.ktcloud.travelplanner.maps.adapter

import com.ktcloud.travelplanner.maps.port.MapsRouteDataPort
import com.ktcloud.travelplanner.membership.repository.TravelMemberRepository
import com.ktcloud.travelplanner.route.service.RouteAccessDeniedException
import com.ktcloud.travelplanner.route.service.RouteTravelNotFoundException
import com.ktcloud.travelplanner.timeline.repository.TimelineItemRepository
import com.ktcloud.travelplanner.travel.model.Travel
import com.ktcloud.travelplanner.travel.repository.TravelRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class JpaMapsRouteDataAdapter(
    private val travelRepository: TravelRepository,
    private val travelMemberRepository: TravelMemberRepository,
    private val timelineItemRepository: TimelineItemRepository,
) : MapsRouteDataPort {
    override fun findReadableTravel(travelId: UUID, requesterId: UUID): Travel {
        val travel = travelRepository.findById(travelId).orElseThrow(::RouteTravelNotFoundException)
        if (travel.owner.id != requesterId &&
            travelMemberRepository.findAcceptedRole(travelId, requesterId) == null
        ) throw RouteAccessDeniedException()
        return travel
    }

    override fun findWaypoints(travelId: UUID, dayNumber: Int) =
        timelineItemRepository.findAllByTravelIdAndDayNumberOrderByVisitOrderAsc(travelId, dayNumber.toShort())
}

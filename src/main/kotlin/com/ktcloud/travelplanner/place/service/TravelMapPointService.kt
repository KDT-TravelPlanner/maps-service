package com.ktcloud.travelplanner.place.service

import com.ktcloud.travelplanner.global.exception.DomainException
import com.ktcloud.travelplanner.global.exception.ErrorCode
import com.ktcloud.travelplanner.maps.port.MapsTravelPort
import com.ktcloud.travelplanner.maps.port.MapsTimelinePort
import com.ktcloud.travelplanner.maps.port.MapsTravelReference
import com.ktcloud.travelplanner.maps.port.MapsTimelineItem
import com.ktcloud.travelplanner.membership.repository.TravelMemberRepository
import com.ktcloud.travelplanner.place.dto.TravelMapPointResponse
import com.ktcloud.travelplanner.place.dto.TravelMapPointsResponse
import com.ktcloud.travelplanner.timeline.repository.TimelineItemRepository
import com.ktcloud.travelplanner.travel.model.Travel
import com.ktcloud.travelplanner.travel.repository.TravelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TravelMapPointService(
        private val mapsTravelPort: MapsTravelPort,
        private val mapsTimelinePort: MapsTimelinePort,
        private val placeLocationService: PlaceLocationService,
) {
        constructor(
                travelRepository: TravelRepository,
                travelMemberRepository: TravelMemberRepository,
                timelineItemRepository: TimelineItemRepository,
                placeLocationService: PlaceLocationService,
        ) : this(
                LegacyMapsTravelPort(travelRepository, travelMemberRepository),
                LegacyMapsTimelinePort(timelineItemRepository),
                placeLocationService,
        )
        @Transactional(readOnly = true)
        fun getMapPoints(
                travelId: UUID,
                requesterId: UUID,
                dayNumber: Int,
        ): TravelMapPointsResponse {
                val travel = mapsTravelPort.findReadableTravel(travelId, requesterId)

                if (dayNumber !in 1..travel.travelDays) {
                        throw InvalidMapDayNumberException()
                }

                val points = mutableListOf<TravelMapPointResponse>()
                val unmappedTimelineItemIds = mutableListOf<UUID>()
                val unresolvedTimelineItemIds = mutableListOf<UUID>()

                val timelineItems = mapsTimelinePort.findMapItems(travelId, dayNumber)

                timelineItems.forEach { timelineItem ->
                        val googlePlaceId = timelineItem.googlePlaceId
                                ?.takeIf { it.isNotBlank() }

                        if (googlePlaceId == null) {
                                unmappedTimelineItemIds += timelineItem.timelineItemId
                                return@forEach
                        }

                        val location = placeLocationService.getLocation(googlePlaceId)

                        if (location == null) {
                                unresolvedTimelineItemIds += timelineItem.timelineItemId
                                return@forEach
                        }

                        points += TravelMapPointResponse(
                                timelineItemId = timelineItem.timelineItemId,
                                visitOrder = timelineItem.visitOrder,
                                name = timelineItem.name,
                                googlePlaceId = googlePlaceId,
                                latitude = location.latitude,
                                longitude = location.longitude,
                        )
                }

                return TravelMapPointsResponse(
                        travelId = travelId,
                        dayNumber = dayNumber,
                        points = points,
                        unmappedTimelineItemIds = unmappedTimelineItemIds,
                        unresolvedTimelineItemIds = unresolvedTimelineItemIds,
                )
        }

}

private class LegacyMapsTravelPort(
        private val travelRepository: TravelRepository,
        private val travelMemberRepository: TravelMemberRepository,
) : MapsTravelPort {
        override fun findReadableTravel(travelId: UUID, requesterId: UUID): MapsTravelReference {
                val travel = travelRepository.findById(travelId).orElseThrow(::MapPointTravelNotFoundException)
                if (travel.owner.id != requesterId &&
                        travelMemberRepository.findAcceptedRole(travelId, requesterId) == null
                ) throw MapPointAccessDeniedException()
                return MapsTravelReference(travel.id, travel.travelDays)
        }
}

private class LegacyMapsTimelinePort(
        private val repository: TimelineItemRepository,
) : MapsTimelinePort {
        override fun findMapItems(travelId: UUID, dayNumber: Int): List<MapsTimelineItem> =
                repository.findAllByTravelIdOrderByDayNumberAscVisitOrderAsc(travelId)
                        .filter { it.dayNumber == dayNumber.toShort() || it.dayNumber == null }
                        .map { MapsTimelineItem(it.id, it.visitOrder.toInt(), it.name, it.googlePlaceId) }
}

class MapPointTravelNotFoundException :
        DomainException(ErrorCode.RESOURCE_NOT_FOUND)

class MapPointAccessDeniedException :
        DomainException(ErrorCode.ACCESS_DENIED)

class InvalidMapDayNumberException :
        DomainException(
                ErrorCode.INVALID_REQUEST,
                "여행 일차가 올바르지 않습니다.",
        )

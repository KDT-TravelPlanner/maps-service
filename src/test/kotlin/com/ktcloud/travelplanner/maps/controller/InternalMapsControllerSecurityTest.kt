package com.ktcloud.travelplanner.maps.controller

import com.ktcloud.travelplanner.global.security.ApiSecurityErrorHandler
import com.ktcloud.travelplanner.place.port.PlaceLocationPort
import com.ktcloud.travelplanner.route.port.RouteCalculationPort
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.beans.factory.annotation.Autowired

@ActiveProfiles("test")
@WebMvcTest(InternalMapsController::class)
@Import(ApiSecurityErrorHandler::class, InternalMapsControllerSecurityTest.TestSecurityConfiguration::class)
class InternalMapsControllerSecurityTest(
	@Autowired private val mockMvc: MockMvc,
) {
	@MockitoBean
	lateinit var routeCalculationPort: RouteCalculationPort

	@MockitoBean
	lateinit var placeLocationPort: PlaceLocationPort

	@Test
	fun `preview endpoint requires authentication`() {
		mockMvc.post("/internal/v1/maps/routes/preview") {
			contentType = org.springframework.http.MediaType.APPLICATION_JSON
			content = """{"waypoints": []}"""
		}
			.andExpect {
				status { isUnauthorized() }
			}
	}

	@Configuration(proxyBeanMethods = false)
	class TestSecurityConfiguration {
		@Bean
		fun securityFilterChain(
			http: HttpSecurity,
			apiSecurityErrorHandler: ApiSecurityErrorHandler,
		): SecurityFilterChain {
			http
				.securityMatcher("/internal/**")
				.csrf { it.disable() }
				.exceptionHandling {
					it.authenticationEntryPoint(apiSecurityErrorHandler)
				}
				.authorizeHttpRequests { it.anyRequest().authenticated() }
			return http.build()
		}
	}
}

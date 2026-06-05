package com.zoni.place.service

import com.zoni.common.BenefitType
import com.zoni.common.ErrorCode
import com.zoni.common.PlaceDataSource
import com.zoni.common.ZoniException
import com.zoni.place.domain.Place
import com.zoni.place.dto.request.BenefitSaveRequest
import com.zoni.place.dto.request.PlaceSaveRequest
import com.zoni.place.repository.PlaceRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PlaceServiceTest {

    @Mock lateinit var placeRepository: PlaceRepository
    @Mock lateinit var kakaoMapClient: KakaoMapClient
    @Mock lateinit var placeCacheService: PlaceCacheService

    lateinit var placeService: PlaceService

    @BeforeEach
    fun setUp() {
        placeService = PlaceService(placeRepository, kakaoMapClient, placeCacheService)
    }

    // ─── savePlace ────────────────────────────────────────────────────────

    @Test
    fun `장소 저장 - 정상 저장`() {
        val request = placeSaveRequest()
        given(placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse("kakao-123", 1L)).willReturn(false)
        given(placeRepository.save(any<Place>())).willReturn(place())

        val result = placeService.savePlace(1L, "jay", request)

        assertThat(result.name).isEqualTo("테스트 카페")
        verify(placeRepository).save(any())
        verify(placeCacheService).evictPopularCache()
    }

    @Test
    fun `장소 저장 - 중복 저장 시 PLACE_ALREADY_SAVED 예외`() {
        val request = placeSaveRequest()
        given(placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse("kakao-123", 1L)).willReturn(true)

        assertThatThrownBy { placeService.savePlace(1L, "jay", request) }
            .isInstanceOf(ZoniException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PLACE_ALREADY_SAVED)
    }

    // ─── saveBenefitPlace ─────────────────────────────────────────────────

    @Test
    fun `혜택 장소 등록 - 정상 등록`() {
        val request = benefitSaveRequest()
        given(placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse("kakao-456", 1L)).willReturn(false)
        given(placeRepository.save(any<Place>())).willReturn(benefitPlace())

        val result = placeService.saveBenefitPlace(1L, "jay", request)

        assertThat(result.benefitType).isEqualTo(BenefitType.USER_VERIFIED)
        assertThat(result.discountInfo).isEqualTo("청년문화패스 50% 할인")
        verify(placeRepository).save(any())
        verify(placeCacheService).evictBenefitCache(BenefitType.USER_VERIFIED)
    }

    @Test
    fun `혜택 장소 등록 - 중복 저장 시 PLACE_ALREADY_SAVED 예외`() {
        val request = benefitSaveRequest()
        given(placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse("kakao-456", 1L)).willReturn(true)

        assertThatThrownBy { placeService.saveBenefitPlace(1L, "jay", request) }
            .isInstanceOf(ZoniException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PLACE_ALREADY_SAVED)
    }

    @Test
    fun `혜택 장소 등록 - targetAgeMin이 targetAgeMax보다 크면 INVALID_AGE_RANGE 예외`() {
        val request = benefitSaveRequest(targetAgeMin = 35, targetAgeMax = 19)
        given(placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(any(), any())).willReturn(false)

        assertThatThrownBy { placeService.saveBenefitPlace(1L, "jay", request) }
            .isInstanceOf(ZoniException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_AGE_RANGE)
    }

    // ─── getPlace ─────────────────────────────────────────────────────────

    @Test
    fun `장소 상세 조회 - viewCount 증가 확인`() {
        val p = place()
        given(placeRepository.findByIdAndIsDeletedFalse(1L)).willReturn(p)

        placeService.getPlace(1L)

        assertThat(p.viewCount).isEqualTo(1)
    }

    @Test
    fun `장소 상세 조회 - 존재하지 않는 장소는 PLACE_NOT_FOUND 예외`() {
        given(placeRepository.findByIdAndIsDeletedFalse(999L)).willReturn(null)

        assertThatThrownBy { placeService.getPlace(999L) }
            .isInstanceOf(ZoniException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PLACE_NOT_FOUND)
    }

    // ─── deletePlace ──────────────────────────────────────────────────────

    @Test
    fun `장소 삭제 - 정상 소프트 삭제`() {
        val p = place()
        given(placeRepository.findByIdAndIsDeletedFalse(1L)).willReturn(p)

        placeService.deletePlace(userId = 1L, placeId = 1L)

        assertThat(p.isDeleted).isTrue()
    }

    @Test
    fun `장소 삭제 - 본인 아닌 유저가 삭제 시도 시 FORBIDDEN 예외`() {
        val p = place(userId = 1L)
        given(placeRepository.findByIdAndIsDeletedFalse(1L)).willReturn(p)

        assertThatThrownBy { placeService.deletePlace(userId = 2L, placeId = 1L) }
            .isInstanceOf(ZoniException::class.java)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.FORBIDDEN)
    }

    // ─── getBenefitPlaces ─────────────────────────────────────────────────

    @Test
    fun `혜택 장소 목록 조회 - benefitType 지정 시 해당 유형 조회`() {
        val page = PageImpl(listOf(benefitPlace()))
        given(placeRepository.findByBenefitTypeAndIsDeletedFalse(any(), any<Pageable>())).willReturn(page)

        val result = placeService.getBenefitPlaces(BenefitType.YOUTH_CULTURE_PASS, 0, 10)

        assertThat(result.places).hasSize(1)
        assertThat(result.places[0].benefitType).isEqualTo(BenefitType.USER_VERIFIED) // fixture
        verify(placeRepository).findByBenefitTypeAndIsDeletedFalse(any(), any())
    }

    @Test
    fun `혜택 장소 목록 조회 - benefitType null이면 전체 혜택 장소 조회`() {
        val page = PageImpl(listOf(benefitPlace()))
        given(placeRepository.findByBenefitTypeIsNotNullAndIsDeletedFalse(any<Pageable>())).willReturn(page)

        val result = placeService.getBenefitPlaces(null, 0, 10)

        assertThat(result.places).hasSize(1)
        verify(placeRepository).findByBenefitTypeIsNotNullAndIsDeletedFalse(any())
    }

    // ─── getBenefitPlacesForAge ───────────────────────────────────────────

    @Test
    fun `연령 기반 혜택 장소 조회 - 정상 조회`() {
        val page = PageImpl(listOf(benefitPlace()))
        given(placeRepository.findBenefitPlacesForAge(any(), any<Pageable>())).willReturn(page)

        val result = placeService.getBenefitPlacesForAge(age = 25, page = 0, size = 10)

        assertThat(result.places).hasSize(1)
        verify(placeRepository).findBenefitPlacesForAge(any(), any())
    }

    // ─── helpers ──────────────────────────────────────────────────────────

    private fun placeSaveRequest() = PlaceSaveRequest(
        kakaoPlaceId = "kakao-123",
        name         = "테스트 카페",
        category     = "카페",
        address      = "서울시 마포구",
        roadAddress  = "서울시 마포구 와우산로",
        x            = 126.9,
        y            = 37.5
    )

    private fun benefitSaveRequest(
        targetAgeMin: Int? = 19,
        targetAgeMax: Int? = 34
    ) = BenefitSaveRequest(
        kakaoPlaceId = "kakao-456",
        name         = "홍대 공연장",
        category     = "공연장",
        address      = "서울시 마포구",
        roadAddress  = "서울시 마포구 와우산로",
        x            = 126.92,
        y            = 37.55,
        benefitType  = BenefitType.USER_VERIFIED,
        discountInfo = "청년문화패스 50% 할인",
        targetAgeMin = targetAgeMin,
        targetAgeMax = targetAgeMax
    )

    private fun place(userId: Long = 1L) = Place(
        id           = 1L,
        kakaoPlaceId = "kakao-123",
        name         = "테스트 카페",
        category     = "카페",
        address      = "서울시 마포구",
        roadAddress  = "서울시 마포구 와우산로",
        x            = 126.9,
        y            = 37.5,
        userId       = userId,
        nickname     = "jay",
        createdAt    = LocalDateTime.now()
    )

    private fun benefitPlace() = Place(
        id           = 2L,
        kakaoPlaceId = "kakao-456",
        name         = "홍대 공연장",
        category     = "공연장",
        address      = "서울시 마포구",
        roadAddress  = "서울시 마포구 와우산로",
        x            = 126.92,
        y            = 37.55,
        userId       = 1L,
        nickname     = "jay",
        benefitType  = BenefitType.USER_VERIFIED,
        discountInfo = "청년문화패스 50% 할인",
        targetAgeMin = 19,
        targetAgeMax = 34,
        dataSource   = PlaceDataSource.USER_INPUT,
        createdAt    = LocalDateTime.now()
    )
}

package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.ErrorResponse;
import wooteco.subway.dto.line.LineRequest;
import wooteco.subway.dto.line.LineResponse;
import wooteco.subway.dto.section.SectionRequest;
import wooteco.subway.dto.station.StationRequest;

@DisplayName("지하철 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

    private static final String LINE_NAME = "2호선";
    private static final String LINE_COLOR = "bg-green-600";

    private Station yeoksam;
    private Station seolleung;
    private Station samseong;

    @BeforeEach
    void setUpData() {
        yeoksam = createStation(new StationRequest("역삼역")).as(Station.class);
        seolleung = createStation(new StationRequest("선릉역")).as(Station.class);
        samseong = createStation(new StationRequest("삼성역")).as(Station.class);
    }

    @Test
    @DisplayName("기존 구간 사이에 새로운 상행 구간을 등록한다.")
    void CreateSection_NewUpSection_OK() {
        // given
        final SectionRequest request = new SectionRequest(yeoksam.getId(), seolleung.getId(), 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));

        final LineResponse expectedResponse = LineResponse.of(
                new Line(lineId, LINE_NAME, LINE_COLOR),
                List.of(this.yeoksam, seolleung, samseong)
        );

        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final LineResponse actualResponse = findLineById(lineId);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private ExtractableResponse<Response> postCreateSection(final SectionRequest request, final long lineId) {
        return RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post(LINE_PATH_PREFIX + SLASH + lineId + SECTION_PATH_PREFIX)
                .then().log().all()
                .extract();
    }

    private LineResponse findLineById(final long lineId) {
        return RestAssured.given().log().all()
                .get(LINE_PATH_PREFIX + SLASH + lineId)
                .then().log().all()
                .extract()
                .as(LineResponse.class);
    }

    @Test
    @DisplayName("기존 구간 사이에 새로운 하행 구간을 등록한다.")
    void CreateSection_NewDownSection_OK() {
        // given
        final SectionRequest request = new SectionRequest(seolleung.getId(), samseong.getId(), 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));

        final LineResponse expectedResponse = LineResponse.of(
                new Line(lineId, LINE_NAME, LINE_COLOR),
                List.of(this.yeoksam, seolleung, samseong)
        );

        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final LineResponse actualResponse = findLineById(lineId);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @DisplayName("등록하려는 구간의 길이가 기존 구간의 길이보다 더 길거나 같으면 400을 반환한다.")
    @ValueSource(ints = {10, 11})
    void CreateSection_InvalidDistance_BadRequest(final int newSectionDistance) {
        // given
        final SectionRequest request = new SectionRequest(yeoksam.getId(), seolleung.getId(), newSectionDistance);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));

        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final ErrorResponse actualResponse = actual.as(ErrorResponse.class);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actualResponse.getMessage()).isEqualTo("기존 구간의 길이 보다 작지 않습니다.");
    }

    @Test
    @DisplayName("등록하려는 노선에 상행, 하행 역이 이미 모두 포함되어 있으면 400을 반환한다.")
    void CreateSection_BothStationAlreadyIncluded_BadRequest() {
        // given
        final SectionRequest request = new SectionRequest(yeoksam.getId(), samseong.getId(), 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));
        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final ErrorResponse actualResponse = actual.as(ErrorResponse.class);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actualResponse.getMessage()).isEqualTo("상행역과 하행역 중 하나의 역만 노선에 포함되어 있어야 합니다.");
    }

    @Test
    @DisplayName("등록하려는 노선에 상행, 하행 역이 모두 포함되어 있지 않으면 400을 반환한다.")
    void CreateSection_BothStationNotIncluded_BadRequest() {
        // given
        final long upStationId = createAndGetStationId(new StationRequest("답십리역"));
        final long downStationId = createAndGetStationId(new StationRequest("왕십리역"));
        final SectionRequest request = new SectionRequest(upStationId, downStationId, 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));
        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final ErrorResponse actualResponse = actual.as(ErrorResponse.class);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(actualResponse.getMessage()).isEqualTo("상행역과 하행역 중 하나의 역만 노선에 포함되어 있어야 합니다.");
    }

    @Test
    @DisplayName("새로운 상행 종점을 등록한다.")
    void CreateSection_NewUpStation_OK() {
        // given
        final SectionRequest request = new SectionRequest(seolleung.getId(), yeoksam.getId(), 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));

        final LineResponse expectedResponse = LineResponse.of(
                new Line(lineId, LINE_NAME, LINE_COLOR),
                List.of(this.yeoksam, seolleung, samseong)
        );

        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final LineResponse actualResponse = findLineById(lineId);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("새로운 하행 종점을 등록한다.")
    void CreateSection_NewDownStation_OK() {
        // given
        final SectionRequest request = new SectionRequest(samseong.getId(), seolleung.getId(), 7);
        final long lineId = createAndGetLineId(new LineRequest(
                LINE_NAME,
                LINE_COLOR,
                yeoksam.getId(),
                samseong.getId(),
                10
        ));

        final LineResponse expectedResponse = LineResponse.of(
                new Line(lineId, LINE_NAME, LINE_COLOR),
                List.of(this.yeoksam, seolleung, samseong)
        );

        // when
        final ExtractableResponse<Response> actual = postCreateSection(request, lineId);
        final LineResponse actualResponse = findLineById(lineId);

        // then
        assertThat(actual.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }
}
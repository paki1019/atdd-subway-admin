package nextstep.subway.section;

import static nextstep.subway.line.LineAcceptanceTest.*;
import static nextstep.subway.line.LineAcceptanceTestFixture.*;
import static nextstep.subway.section.SectionAcceptanceTestFixture.*;
import static nextstep.subway.station.StationAcceptanceTest.*;
import static nextstep.subway.station.StationAcceptanceTestFixture.*;
import static nextstep.subway.utils.JsonPathUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;

@DisplayName("지하철역 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {
    private Integer WANGSIPLI_ID;
    private Integer JUKJUN_ID;

    private Integer LINE_BUNDANG_ID_거리_10;

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철역 사이에 새로운 지하철 역을 구간으로 등록하면
     * Then 지하철 노선 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("역 사이에 새로운 역 등록")
    @Test
    void addSection() {
        // given
        왕십리_죽전_분당선_등록();
        String SEOHYUN = "서현역";
        Integer SEOHYUN_ID = ID_추출(지하철역_생성(SEOHYUN));

        // when
        Map<String, String> params = 구간_등록_요청_파라미터(WANGSIPLI_ID, SEOHYUN_ID, "5");
        지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        ExtractableResponse<Response> response = 지하철_노선_목록_조회();

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(노선목록_역목록_이름_추출(response)).contains(WANGSIPLI, SEOHYUN, JUKJUN)
        );
    }

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철 노선에 새로운 지하철 역을 상행 종점으로 구간 등록하면
     * Then 지하철 노선 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("새로운 역을 상행 종점으로 등록")
    @Test
    void addSection_upStation() {
        // given
        왕십리_죽전_분당선_등록();

        String CHEONGRYANGRI = "청량리역";
        Integer CHEONGRYANGRI_ID = ID_추출(지하철역_생성(CHEONGRYANGRI));

        // when
        Map<String, String> params = 구간_등록_요청_파라미터(CHEONGRYANGRI_ID, WANGSIPLI_ID, "5");
        지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        ExtractableResponse<Response> response = 지하철_노선_목록_조회();
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(노선목록_역목록_이름_추출(response)).contains(CHEONGRYANGRI, WANGSIPLI, JUKJUN)
        );
    }

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철 노선에 새로운 지하철 역을 하행 종점으로 구간 등록하면
     * Then 지하철 노선 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("새로운 역을 하행 종점으로 등록")
    @Test
    void addSection_downStation() {
        // given
        왕십리_죽전_분당선_등록();

        String GIHEUNG = "기흥역";
        Integer GIHEUNG_ID = ID_추출(지하철역_생성(GIHEUNG));

        // when
        Map<String, String> params = 구간_등록_요청_파라미터(GIHEUNG_ID, WANGSIPLI_ID, "5");
        지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        ExtractableResponse<Response> response = 지하철_노선_목록_조회();
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(노선목록_역목록_이름_추출(response)).contains(WANGSIPLI, JUKJUN, GIHEUNG)
        );
    }

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철 노선에 기존 역 사이 길이보다 크거나 같은 구간을 등록하면
     * Then 등록을 할 수 없다
     */
    @DisplayName("기존 역 사이 길이보다 크거나 같을 경우")
    @ParameterizedTest
    @ValueSource(strings = {"20", "10"})
    void addSection_distance_over(String distance) {
        // given
        왕십리_죽전_분당선_등록();
        Integer SEOHYUN_ID = ID_추출(지하철역_생성("서현역"));

        // when
        Map<String, String> params = 구간_등록_요청_파라미터(WANGSIPLI_ID, SEOHYUN_ID, distance);
        ExtractableResponse<Response> response = 지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철 노선에 이미 등록되어있는 지하철 구간을 등록하면
     * Then 등록을 할 수 없다
     */
    @DisplayName("상행역 하행역 이미 모두 등록되어있는 경우")
    @Test
    void addSection_alreadyExists() {
        // given
        왕십리_죽전_분당선_등록();

        // when
        Map<String, String> params = 구간_등록_요청_파라미터(WANGSIPLI_ID, JUKJUN_ID, "5");
        ExtractableResponse<Response> response = 지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 역과 노선이 등록되어있고
     * When 지하철 노선에 포함되어 있지 않은 지하철 역을 구간으로 등록하면
     * Then 등록을 할 수 없다
     */
    @DisplayName("상행역 하행역 둘 중 하나도 포함되어있지 않은 경우")
    @Test
    void addSection_notContains() {
        // given
        왕십리_죽전_분당선_등록();
        Integer HONGDAE_ID = ID_추출(지하철역_생성("HONGDAE"));
        Integer JAMSIL_ID = ID_추출(지하철역_생성("JAMSIL"));

        // when 
        Map<String, String> params = 구간_등록_요청_파라미터(HONGDAE_ID, JAMSIL_ID, "5");
        ExtractableResponse<Response> response = 지하철_구간_등록(LINE_BUNDANG_ID_거리_10, params);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private Map<String, String> 구간_등록_요청_파라미터(Integer upStationId, Integer downStationId, String distance) {
        Map<String, String> params = new HashMap<>();
        params.put("upStationId", upStationId.toString());
        params.put("downStationId", downStationId.toString());
        params.put("distance", distance);
        return params;
    }

    private Integer ID_추출(ExtractableResponse<Response> response) {
        return extractInteger(response, "$.id");
    }

    private List<String> 노선목록_역목록_이름_추출(ExtractableResponse<Response> response) {
        return extractList(response, "$[*].stations[*].name");
    }

    private void 왕십리_죽전_분당선_등록() {
        WANGSIPLI_ID = ID_추출(지하철역_생성(WANGSIPLI));
        JUKJUN_ID = ID_추출(지하철역_생성(JUKJUN));
        LINE_BUNDANG_ID_거리_10 = ID_추출(지하철_노선_생성_거리_10(LINE_BUNDANG, WANGSIPLI_ID, JUKJUN_ID));
    }
}

package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Station;

class SectionDaoTest extends DaoTest {

    private Long lineId;
    private Long upStationId;
    private Long downStationId;

    @BeforeEach
    void setUpData() {
        final Line line = new Line("2호선", "bg-green-600");
        lineId = lineDao.insert(line)
                .orElseThrow()
                .getId();

        final Station upStation = new Station("선릉역");
        upStationId = stationDao.insert(upStation)
                .orElseThrow()
                .getId();

        final Station downStation = new Station("삼성역");
        downStationId = stationDao.insert(downStation)
                .orElseThrow()
                .getId();
    }

    @Test
    @DisplayName("구간 객체를 저장하면 ID를 반환한다.")
    void Insert() {
        // given
        final Section section = new Section(lineId, upStationId, downStationId, 10);

        // when
        final Long actual = sectionDao.insert(section);

        // then
        assertThat(actual).isNotNull();
    }
}
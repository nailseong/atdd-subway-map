package wooteco.subway.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Distance;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.Stations;
import wooteco.subway.dto.section.SectionCreationRequest;
import wooteco.subway.dto.section.SectionDeletionRequest;
import wooteco.subway.exception.IllegalInputException;
import wooteco.subway.exception.line.NoSuchLineException;
import wooteco.subway.exception.section.NoSuchSectionException;
import wooteco.subway.exception.station.NoSuchStationException;

@Service
@Transactional
public class SectionService {

    private static final int VALID_STATION_COUNT = 1;

    private final LineDao lineDao;
    private final StationDao stationDao;
    private final SectionDao sectionDao;

    public SectionService(final LineDao lineDao, final StationDao stationDao, final SectionDao sectionDao) {
        this.lineDao = lineDao;
        this.stationDao = stationDao;
        this.sectionDao = sectionDao;
    }

    public void save(final SectionCreationRequest request) {
        lineDao.findById(request.getLineId())
                .orElseThrow(NoSuchLineException::new);
        validateStationCount(request);

        sectionDao.findBy(request.getLineId(), request.getUpStationId(), request.getDownStationId())
                .ifPresentOrElse(existingSection -> insertBetween(request, existingSection),
                        () -> extendEndStation(request)
                );
    }

    private void validateStationCount(final SectionCreationRequest request) {
        final Stations stations = new Stations(stationDao.findAllByLineId(request.getLineId()));
        final int stationCount = stations.calculateMatchCount(request.getUpStationId(), request.getDownStationId());
        if (stationCount != VALID_STATION_COUNT) {
            throw new IllegalInputException("상행역과 하행역 중 하나의 역만 노선에 포함되어 있어야 합니다.");
        }
    }

    private void insertBetween(final SectionCreationRequest request, final Section existingSection) {
        sectionDao.deleteById(existingSection.getId());
        final Section newSection = toSection(request);
        existingSection.assign(newSection)
                .forEach(sectionDao::insert);
    }

    private Section toSection(final SectionCreationRequest request) {
        final Line line = lineDao.findById(request.getLineId())
                .orElseThrow(NoSuchLineException::new);
        final Station upStation = stationDao.findById(request.getUpStationId())
                .orElseThrow(NoSuchStationException::new);
        final Station downStation = stationDao.findById(request.getDownStationId())
                .orElseThrow(NoSuchStationException::new);
        return new Section(
                line,
                upStation,
                downStation,
                new Distance(request.getDistance())
        );
    }

    private void extendEndStation(final SectionCreationRequest request) {
        sectionDao.findByLineIdAndUpStationId(request.getLineId(), request.getDownStationId())
                .ifPresent(section -> extendSection(request));

        sectionDao.findByLineIdAndDownStationId(request.getLineId(), request.getUpStationId())
                .ifPresent(section -> extendSection(request));
    }

    private void extendSection(final SectionCreationRequest request) {
        final Section newUpSection = toSection(request);
        sectionDao.insert(newUpSection);
    }

    public void delete(final SectionDeletionRequest request) {
        lineDao.findById(request.getLineId())
                .orElseThrow(NoSuchLineException::new);

        final Sections sections = sectionDao.findAllByLineId(request.getLineId());
        final Station stationToDelete = stationDao.findById(request.getStationId())
                .orElseThrow(NoSuchSectionException::new);
        final Sections deletableSections = sections.findDeletableSections(stationToDelete);
        deleteAll(deletableSections);

        if (deletableSections.needMerge()) {
            final Section mergedSection = deletableSections.toMergedSection();
            sectionDao.insert(mergedSection);
        }
    }

    private void deleteAll(final Sections deletableSections) {
        final List<Long> sectionIds = deletableSections.getValue()
                .stream()
                .map(Section::getId)
                .collect(Collectors.toList());
        sectionDao.deleteByIdIn(sectionIds);
    }
}

package wooteco.subway.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import wooteco.subway.exception.IllegalInputException;

public class SectionDomain {

    private final Long id;
    private final Line line;
    private final Station upStation;
    private final Station downStation;
    private final Distance distance;

    public SectionDomain(final Long id, final Line line, final Station upStation, final Station downStation,
                         final Distance distance) {
        validate(upStation, downStation);
        this.id = id;
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public SectionDomain(final Line line, final Station upStation, final Station downStation, final Distance distance) {
        this(null, line, upStation, downStation, distance);
    }

    private void validate(final Station upStation, final Station downStation) {
        if (upStation.equals(downStation)) {
            throw new IllegalInputException("두 종점이 동일합니다.");
        }
    }

    public List<SectionDomain> assign(final SectionDomain newSection) {
        distance.checkCanAssign(newSection.distance);

        final Distance assignedDistance = distance.minus(newSection.distance);
        if (upStation.equals(newSection.upStation)) {
            return List.of(
                    newSection,
                    new SectionDomain(line, newSection.downStation, downStation, assignedDistance)
            );
        }
        return List.of(
                new SectionDomain(line, upStation, newSection.upStation, assignedDistance),
                newSection
        );
    }

    public SectionDomain merge(final SectionDomain section) {
        final Station criteriaStation = findDuplicateStation(section);
        final Distance mergedDistance = distance.plus(section.distance);
        if (criteriaStation.equals(upStation)) {
            return new SectionDomain(
                    line,
                    section.upStation,
                    downStation,
                    mergedDistance
            );
        }
        return new SectionDomain(
                line,
                upStation,
                section.downStation,
                mergedDistance
        );
    }

    private Station findDuplicateStation(final SectionDomain section) {
        final Set<Station> stationIds = new HashSet<>();
        stationIds.add(upStation);
        stationIds.add(downStation);
        if (stationIds.contains(section.upStation)) {
            return section.upStation;
        }
        return section.downStation;
    }

    public boolean contains(final Station station) {
        return upStation.equals(station) || downStation.equals(station);
    }

    public boolean hasSameUpStation(final Station upStation) {
        return this.upStation.equals(upStation);
    }

    public Long getLineId() {
        return line.getId();
    }

    public Long getUpStationId() {
        return upStation.getId();
    }

    public Station getUpStation() {
        return upStation;
    }

    public Long getDownStationId() {
        return downStation.getId();
    }

    public Station getDownStation() {
        return downStation;
    }

    public Line getLine() {
        return line;
    }

    public int getDistance() {
        return distance.getValue();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SectionDomain that = (SectionDomain) o;
        return Objects.equals(id, that.id) && Objects.equals(line, that.line)
                && Objects.equals(upStation, that.upStation) && Objects.equals(downStation,
                that.downStation) && Objects.equals(distance, that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, line, upStation, downStation, distance);
    }

    @Override
    public String toString() {
        return "SectionDomain{" +
                "id=" + id +
                ", line=" + line +
                ", upStation=" + upStation +
                ", downStation=" + downStation +
                ", distance=" + distance +
                '}';
    }
}

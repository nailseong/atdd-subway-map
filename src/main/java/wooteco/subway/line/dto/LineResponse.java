package wooteco.subway.line.dto;

import java.util.Collections;
import java.util.List;
import wooteco.subway.station.dto.StationResponse;

public class LineResponse {

    private Long id;
    private String name;
    private String color;
    private List<StationResponse> stations;

    public LineResponse() {
    }

    public LineResponse(final Long id, final String name, final String color) {
        this(id, name, color, Collections.emptyList());
    }

    public LineResponse(final Long id, final String name, final String color,
        final List<StationResponse> stations) {

        this.id = id;
        this.name = name;
        this.color = color;
        this.stations = stations;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<StationResponse> getStations() {
        return stations;
    }
}
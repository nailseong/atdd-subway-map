package wooteco.subway.admin.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.data.annotation.Id;

public class Line {
	@Id
	private Long id;
	private String name;
	private String color;
	private LocalTime startTime;
	private LocalTime endTime;
	private int intervalTime;
	private Set<LineStation> stations;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Line() {
	}

	public Line(Long id, String name, String color, LocalTime startTime, LocalTime endTime,
			int intervalTime) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.startTime = startTime;
		this.endTime = endTime;
		this.intervalTime = intervalTime;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public Line(String name, String color, LocalTime startTime, LocalTime endTime,
			int intervalTime) {
		this(null, name, color, startTime, endTime, intervalTime);
	}

	public void update(Line line) {
		if (line.getName() != null) {
			this.name = line.getName();
		}
		if (line.getColor() != null) {
			this.color = line.getColor();
		}
		if (line.getStartTime() != null) {
			this.startTime = line.getStartTime();
		}
		if (line.getEndTime() != null) {
			this.endTime = line.getEndTime();
		}
		if (line.getIntervalTime() != 0) {
			this.intervalTime = line.getIntervalTime();
		}

		this.updatedAt = LocalDateTime.now();
	}

	public void addLineStation(LineStation lineStation) {
		stations.add(lineStation);
	}

	public LineStation removeLineStationById(Long stationId) {
		LineStation lineStation = stations.stream()
				.filter(station -> station.getStationId() == stationId)
				.findFirst()
				.orElseThrow(NoSuchElementException::new);
		stations.remove(lineStation);
		return lineStation;
	}

	public List<Long> getLineStationsId() {
		// TODO: 구현
		return new ArrayList<>();
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

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public Set<LineStation> getStations() {
		return stations;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setLineStations(Set<LineStation> stations) {
		this.stations = stations;
	}

	@Override
	public String toString() {
		return "Line{" +
				"id=" + id +
				", name='" + name + '\'' +
				", color='" + color + '\'' +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", intervalTime=" + intervalTime +
				", stations=" + stations +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				'}';
	}
}

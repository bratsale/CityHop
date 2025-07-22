package project.pj25.algorithm; // Ili package project.pj25.algorithm; ako želiš da bude bliže algoritmu

import project.pj25.model.*;
import java.time.LocalTime;
import java.util.Objects;

// Ova klasa predstavlja stanje čvora u Dijkstra algoritmu (stanica + vrieme dolaska)
public class NodeState implements Comparable<NodeState> {
    private Station station;
    private LocalTime arrivalTime;
    private Path currentPath;      // Putanja do ove stanice (od početne do ove)

    public NodeState(Station station, LocalTime arrivalTime, Path currentPath) {
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.currentPath = currentPath;
    }

    public Station getStation() {
        return station;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    // Za poređenje u PriorityQueue-u
    @Override
    public int compareTo(NodeState other) {
        // Trenutno poređenje po ukupnom vremenu putovanja do ove tačke
        return this.currentPath.getTotalTravelTime().compareTo(other.currentPath.getTotalTravelTime());
    }

    // Važno za HashMapu 'visited' ili 'distances'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeState nodeState = (NodeState) o;
        // Dva NodeState-a su ista ako se odnose na istu stanicu
        return Objects.equals(station.getId(), nodeState.station.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(station.getId());
    }
}
package org.contikios.cooja.util;

import org.contikios.cooja.Mote;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.radiomediums.UDGM;

import java.util.*;
import java.util.stream.Collectors;

public class NodeGraph {
    private Map<Mote, Map<Mote, Integer>> matrix;
    private Map<Mote, Boolean> activeMotes;

    public NodeGraph(Simulation simulation) {
        refreshMatrix(simulation);
    }

    public void refreshMatrix(Simulation simulation) {
        // Get the motes in the simulation
        Mote[] motes = simulation.getMotes();

        // Assume that the simulation is using UDGM for a radio medium
        UDGM radioMedium = (UDGM) simulation.getRadioMedium();

        if (this.matrix == null || getMatrixSize() != motes.length) {
            this.matrix = new HashMap<>(motes.length);
        }

        if (this.activeMotes == null) {
            this.activeMotes = new HashMap<>(motes.length);
        }

        for (Mote src : motes) {
            Map<Mote, Integer> row;

            if (matrix.containsKey(src)) {
                row = matrix.get(src);
            } else {
                row = new HashMap<>(motes.length);
            }

            for (Mote dest : motes) {
                if (dest.getID() == src.getID()) {
                    row.put(dest, 1);
                }

                if (radioMedium.getRxSuccessProbability(src.getInterfaces().getRadio(),
                        dest.getInterfaces().getRadio()) == 1.0) {
                    row.put(dest, 1);
                } else {
                    row.put(dest, 0);
                }
            }

            matrix.put(src, row);
            activeMotes.put(src, true);
        }
    }

    public void toggleMote(Mote mote) {
        // Iterate over the map and set t
        for (Mote m : this.matrix.keySet()) {
            Map<Mote, Integer> row = this.matrix.get(m);

            if (m.getID() == mote.getID()) {
                for (Mote k : row.keySet()) {
                    row.put(k, -1*row.get(k));
                }
            } else {
                for (Mote k : row.keySet()) {
                    if (k.getID() == mote.getID()) {
                        row.put(k, -1*row.get(k));
                    }
                }
            }
        }

        boolean status = this.activeMotes.get(mote);
        this.activeMotes.put(mote, !status);
    }

    private Set<Mote> getActiveMotes() {
        return this.activeMotes.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public boolean isConnected() {
        Set<Mote> visited = new HashSet<>(matrix.size());

        Deque<Mote> path = new ArrayDeque<>();
        path.push(matrix.keySet().iterator().next());

        while (!path.isEmpty()) {
            Mote current = path.peek();

            visited.add(current);
            // Valid neighbours are ones that have not been visited yet
            Set<Mote> neighbours = getNeighbours(current);
            neighbours.removeAll(visited);

            if (neighbours.isEmpty()) {
                path.pop();
            } else {
                path.push(neighbours.iterator().next());
            }
        }

        return visited.size() == getActiveMotes().size();
    }

    public Set<Mote> get1HopNeighbours(Mote mote) {
        HashSet<Mote> neighbours = new HashSet<>();

        Map<Mote, Integer> row = this.matrix.get(mote);

        for (Mote m : row.keySet()) {
            if (row.get(m) != 0) {
                neighbours.add(m);
            }
        }

        return neighbours;
    }

    public Set<Mote> getNeighbours(Mote mote) {
        HashSet<Mote> neighbours = new HashSet<>();

        Map<Mote, Integer> row = this.matrix.get(mote);

        for (Mote m : row.keySet()) {
            if (row.get(m) > 0) {
                neighbours.add(m);
            }
        }

        return neighbours;
    }

    @Override
    public String toString() {
        return matrix.toString();
    }

    private int getMatrixSize() {
        if (this.matrix != null) {
            return matrix.size();
        } else {
            return -1;
        }
    }
}
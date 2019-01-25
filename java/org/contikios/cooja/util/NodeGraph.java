package org.contikios.cooja.util;

import org.contikios.cooja.Mote;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.radiomediums.UDGM;

import java.util.HashMap;
import java.util.Map;

public class NodeGraph {
    private Map<Mote, Map<Mote, Integer>> matrix;

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
        }
    }

    private int getMatrixSize() {
        if (this.matrix != null) {
            return matrix.size();
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return matrix.toString();
    }

    // TODO: Add APIs for adding/removing nodes to the graph, and updating if a
    // mote has been made offline or not.
}
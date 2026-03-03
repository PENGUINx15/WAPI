package me.penguinx13.wapi;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Tree {

    private final Block start;
    private final Set<Block> logs = new HashSet<>();
    private final Set<Block> leaves = new HashSet<>();

    private static final int LEAF_RADIUS = 3;
    private static final int MAX_HEIGHT = 50;

    public Tree(Block start) {
        this.start = start;
    }

    public boolean collect() {
        if (!Tag.LOGS.isTagged(start.getType())) return false;

        collectLogs();
        collectLeaves();

        return !logs.isEmpty() && !leaves.isEmpty();
    }

    private void collectLogs() {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            logs.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        Block neighbor = current.getRelative(dx, dy, dz);

                        if (!visited.contains(neighbor) &&
                                Tag.LOGS.isTagged(neighbor.getType()) &&
                                Math.abs(neighbor.getY() - start.getY()) <= MAX_HEIGHT) {
                            queue.add(neighbor);
                            visited.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private void collectLeaves() {
        for (Block log : logs) {
            for (int dx = -LEAF_RADIUS; dx <= LEAF_RADIUS; dx++) {
                for (int dy = -LEAF_RADIUS; dy <= LEAF_RADIUS; dy++) {
                    for (int dz = -LEAF_RADIUS; dz <= LEAF_RADIUS; dz++) {
                        Block b = log.getRelative(dx, dy, dz);
                        if (isLeaf(b.getType())) {
                            leaves.add(b);
                        }
                    }
                }
            }
        }
    }

    private boolean isLeaf(Material mat) {
        return Tag.LEAVES.isTagged(mat);
    }

    public Set<Block> getLogs() {
        return logs;
    }

    public Set<Block> getLeaves() {
        return leaves;
    }
}

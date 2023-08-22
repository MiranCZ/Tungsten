package me.miran.path.calculators;

import baritone.api.utils.BetterBlockPos;
import me.miran.path.Node;
import me.miran.path.calculators.HeuristicCalculator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BaritoneCalculator implements HeuristicCalculator {

    private final List<BetterBlockPos> path;
    private final HashMap<BlockPos,Integer> indexOfMap;
    public BaritoneCalculator(List<BetterBlockPos> path) {
        this.path = path;
        indexOfMap  = new HashMap<>(path.size());
    }

    @Override
    public double calculate(Node parent, Node start, Node child, Vec3d target) {
        BetterBlockPos pos = getLowestDistPos(child);
        double lowestDist = getDistanceIgnoreY(pos.x,pos.z,child.agent.posX,child.agent.posZ);

        if (child.agent.onGround) {
            lowestDist += Math.abs(child.agent.posY-pos.y);
        }

        int index;
        if (indexOfMap.containsKey(pos)) {
            index = indexOfMap.get(pos);
        }else {
            index = path.indexOf(pos);
            indexOfMap.put(pos,index);
        }

        return -index+lowestDist+ child.pathCost/5d;
    }

    private static double getDistanceIgnoreY(double x, double z, double x1, double z1) {
        return Math.abs(x-x1) + Math.abs(z-z1);
    }

    private BetterBlockPos getLowestDistPos(Node node) {
        double lowestDist = Double.POSITIVE_INFINITY;
        BetterBlockPos lowestDistPos = null;

        for (BetterBlockPos pos : path) {
            double dist = getDistanceIgnoreY(pos.x,pos.z,node.agent.posX,node.agent.posZ);
            if (dist < lowestDist) {
                lowestDist = dist;
                lowestDistPos = pos;
            }

        }

        return lowestDistPos;
    }
}

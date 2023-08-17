package me.miran.path.calculators;

import baritone.api.utils.BetterBlockPos;
import me.miran.path.Node;
import me.miran.path.calculators.HeuristicCalculator;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BaritoneCalculator implements HeuristicCalculator {

    private final List<BetterBlockPos> path;
    public BaritoneCalculator(List<BetterBlockPos> path) {
        this.path = path;
    }

    @Override
    public double calculate(Node parent, Node start, Node child, Vec3d target) {
        BetterBlockPos pos = getLowestDistPos(path,child);
        double lowestDist = getDistanceIgnoreY(pos.x,pos.z,child.agent.posX,child.agent.posZ);

        if (child.agent.onGround) {
            lowestDist += Math.abs(child.agent.posY-pos.y);
        }

        double heuristic = -path.indexOf(pos)+lowestDist+ child.pathCost/5d;

        if (child.agent.horizontalCollision) {
            //massive collision punish
            double d = 40+ (Math.abs(parent.agent.velZ-child.agent.velY)+Math.abs(parent.agent.velX-child.agent.velX))*120;
            heuristic += d;
        }

        return heuristic;
    }

    private static double getDistanceIgnoreY(double x, double z, double x1, double z1) {
        return Math.abs(x-x1) + Math.abs(z-z1);
    }

    private static BetterBlockPos getLowestDistPos(List<BetterBlockPos> path, Node node) {
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

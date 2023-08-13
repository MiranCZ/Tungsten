package me.miran.path;

import net.minecraft.util.math.Vec3d;

public class Calculators {

    public static HeuristicCalculator GREEDY = (parent, start, child, target) -> {
        double heuristic = 20.0D * child.agent.getPos().distanceTo(target);

        if (child.agent.touchingWater) {
            heuristic = Integer.MAX_VALUE;//we hate water
        }
        if (child.agent.horizontalCollision) {
            //massive collision punish
            double d = 25+ (Math.abs(parent.agent.velZ-child.agent.velY)+Math.abs(parent.agent.velX-child.agent.velX))*120;
            //System.out.println(d);
            heuristic += d;
        }

        return heuristic+child.pathCost;
    };

    public static HeuristicCalculator A_STAR = (parent, start, child, target) -> {
        Vec3d childPos = child.agent.getPos();

        double yScore = Math.abs(childPos.y-target.y);
        if (!child.agent.onGround) {
            yScore = 0;//don't discourage jumping
        }
        double heuristic = child.pathCost + (Math.abs(childPos.x-target.x)+yScore+Math.abs(childPos.z-target.z))*3;

        if (child.agent.touchingWater) {
            heuristic = Integer.MAX_VALUE;//we hate water
        }

        return heuristic;
    };

}

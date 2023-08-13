package me.miran.path;

import net.minecraft.util.math.Vec3d;

public interface HeuristicCalculator {

    double calculate(Node parent, Node start, Node child, Vec3d target);

}

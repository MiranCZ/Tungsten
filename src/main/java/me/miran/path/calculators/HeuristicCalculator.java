package me.miran.path.calculators;

import me.miran.path.Node;
import net.minecraft.util.math.Vec3d;

public interface HeuristicCalculator {

    double calculate(Node parent, Node start, Node child, Vec3d target);

}

package me.miran.mixin.baritone;

import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.Goal;
import baritone.bs;
import baritone.bv;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import baritone.h;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(h.class)
public interface PathingBehaviourInvoker {

    @Invoker("a")
    public bs createPathfinder(BlockPos var0, Goal var1, IPath var2, bv var3);

}

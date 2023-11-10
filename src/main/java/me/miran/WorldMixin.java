package me.miran;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;


public interface WorldMixin {

    @Unique
    void tungsten$setBlockList(HashMap<BlockPos, BlockState> map);
    @Unique
    void tungsten$setCalcThread(Thread thread);
    @Unique
    void tungsten$clear();

}

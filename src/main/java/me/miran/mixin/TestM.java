package me.miran.mixin;

import me.miran.WorldMixin;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(World.class)
public class TestM implements WorldMixin {

    @Unique
    private Thread thread;
    @Unique
    private HashMap<BlockPos,BlockState> map;

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void a(BlockPos pos, CallbackInfoReturnable<BlockState> cir){
       /* if (pos.getY() == 63) {
            cir.setReturnValue(Blocks.DIAMOND_BLOCK.getDefaultState());
            return;
        }*/
      /*  if (Thread.currentThread().equals(thread)) {
            System.out.println("called from thread, wtf");
        }*/

        HashMap<BlockPos,BlockState> map = this.map;
        if(thread == null || map == null || Thread.currentThread() != thread || !map.containsKey(pos)) return;


        cir.setReturnValue(map.get(pos));
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    public void b(BlockPos pos, CallbackInfoReturnable<FluidState> cir){
        HashMap<BlockPos,BlockState> map = this.map;
        if(thread == null || map == null || Thread.currentThread() != thread || !map.containsKey(pos)) return;

        cir.setReturnValue(map.get(pos).getFluidState());
    }



    @Override
    public void tungsten$setBlockList(HashMap<BlockPos, BlockState> map) {
     //   System.out.println("set blocklist");
        this.map = map;
    }

    @Override
    public void tungsten$setCalcThread(Thread thread) {
     //   System.out.println("set thread");
        this.thread = thread;
    }

    @Override
    public void tungsten$clear() {
        map = null;
        thread = null;
      //  System.out.println("cleared");
    }
}

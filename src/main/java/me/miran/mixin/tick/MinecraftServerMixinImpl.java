package me.miran.mixin.tick;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.miran.tick.MinecraftServerMixin;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixinImpl implements MinecraftServerMixin {


    @Mutable
    @Shadow
    @Final
    public static int field_33206;


    @Shadow private long timeReference;

    @Shadow protected abstract void startTickMetrics();

    private static long TICK_TIME = 50L;

    public void setMsPerTick(int ms) {
        TICK_TIME = ms;
        field_33206 = ms;
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(JJ)J",ordinal = 0))
    public long modifyDelayedTasksMaxNextTickTime(long a, long b) {
        a = a - 50L + TICK_TIME;

        return Math.max(a, b);
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startTickMetrics()V",ordinal = 0))
    public void modifyNextTickTime(MinecraftServer instance){
        this.timeReference = timeReference -50L + TICK_TIME;
        startTickMetrics();
    }


}

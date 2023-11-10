package me.miran.mixin.tick;


import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderTickCounter.class)
public interface TimerAccessor {


	@Accessor @Mutable
	public void setTickTime(float T);
	
}

package me.miran.mixin;

import me.miran.Main;
import me.miran.world.VoxelWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

	@Shadow @Nullable public ClientWorld world;

	@Inject(at = @At("HEAD"), method = "tick")
	private void tick(CallbackInfo info) {
		if(this.world == null) {
			Main.WORLD = null;
		} else if(Main.WORLD == null) {
			Main.WORLD = new VoxelWorld(this.world);
		} else if(Main.WORLD.parent != this.world) {
			Main.WORLD = new VoxelWorld(this.world);
		}
	}

}

package me.miran.mixin.minecraft;

import com.mojang.authlib.GameProfile;
import me.miran.Main;
import me.miran.agent.Agent;
import me.miran.command.RecordPathCommand;
import me.miran.path.PathFinder;
import me.miran.render.Color;
import me.miran.render.Cuboid;
import me.miran.render.Line;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

	public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
		super(world, profile, publicKey);
	}

	private Vec3d prevPos = new Vec3d(0,0,0);

	@Inject(method = "tick", at = @At("HEAD"))
	public void start(CallbackInfo ci) {
		if(Main.EXECUTOR.isRunning()) {
			Main.EXECUTOR.tick((ClientPlayerEntity)(Object)this, MinecraftClient.getInstance().options);
		}

		if(!this.getAbilities().flying) {
			Agent.INSTANCE = Agent.of((ClientPlayerEntity)(Object)this);
		//	Agent.INSTANCE.tick(this.world);
		}

		if(Main.startPathing) {
			Main.startPathing = false;
			PathFinder.find(this.world, Main.TARGET);
		}
		if (RecordPathCommand.recording) {
			Main.RENDERERS.add(new Line(getPos(), prevPos,new Color(0,0,255)));
			Main.RENDERERS.add(new Cuboid(getPos().subtract(0.05D, 0.05D, 0.05D), new Vec3d(0.1D, 0.1D, 0.1D), new Color(0,0,255)));
		}

		prevPos = getPos();
	}

	@Inject(method = "tick", at = @At(value = "RETURN"))
	public void end(CallbackInfo ci) {
		if(!this.getAbilities().flying && Agent.INSTANCE != null) {
			//Agent.INSTANCE.compare((ClientPlayerEntity)(Object)this, true);
		}
	}

	@Inject(method="getPitch", at=@At("RETURN"), cancellable = true)
	public void getPitch(float tickDelta, CallbackInfoReturnable<Float> ci) {
		if(Main.EXECUTOR.isRunning()) {
			ci.setReturnValue(super.getPitch(tickDelta));
		}
	}

	@Inject(method="getYaw", at=@At("RETURN"), cancellable = true)
	public void getYaw(float tickDelta, CallbackInfoReturnable<Float> ci) {
		if(Main.EXECUTOR.isRunning()) {
			ci.setReturnValue(super.getYaw(tickDelta));
		}
	}

}

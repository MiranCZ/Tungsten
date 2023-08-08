package me.miran.mixin;

import me.miran.Main;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk extends Chunk {

	public MixinWorldChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome,
	                       long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
		super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
	}

	@Shadow public abstract World getWorld();
	@Shadow public abstract BlockState getBlockState(BlockPos pos);
	@Shadow public abstract FluidState getFluidState(BlockPos pos);

	@Inject(method = "loadFromPacket", at = @At("RETURN"))
	private void loadFromPacket(PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfo ci) {
		if(Main.WORLD == null || this.getWorld() != Main.WORLD.parent) {
			return;
		}

		/*
		int minX = this.getPos().x << 4;
		int minY = this.getWorld().getBottomY();
		int minZ = this.getPos().z << 4;
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for(int x = minX; x < minX + 16; x++) {
			for(int z = minX; z < minZ + 16; z++) {
				for(int y = minY; y < minY + this.getWorld().getHeight(); y++) {
					pos.set(x, y, z);
					BlockState block = this.getBlockState(pos);
					FluidState fluid = this.getFluidState(pos);
					ExampleMod.WORLD.setBlockAndFluidState(pos, block, fluid);
				}
			}
		}*/
	}

}

package eu.midnightdust.visualoverhaul.mixin;

import eu.midnightdust.visualoverhaul.config.VOConfig;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlastFurnaceBlock.class)
public abstract class MixinBlastFurnaceBlock {
    @Inject(at = @At("TAIL"), method = "animateTick")
    public void vo$randomDisplayTick(BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.getValue(AbstractFurnaceBlock.LIT) && VOConfig.blast_furnace_particles) {
            double d = (double)pos.getX() + 0.5D;
            double e = pos.getY();
            double f = (double)pos.getZ() + 0.5D;

            Direction direction = state.getValue(AbstractFurnaceBlock.FACING);
            Direction.Axis axis = direction.getAxis();
            double h = random.nextDouble() * 0.6D - 0.3D;
            double i = axis == Direction.Axis.X ? (double)direction.getStepX() * 0.4D : h;
            double j = random.nextDouble() * 6.0D / 16.0D;
            double k = axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.4D : h;
            world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
        }
    }

}

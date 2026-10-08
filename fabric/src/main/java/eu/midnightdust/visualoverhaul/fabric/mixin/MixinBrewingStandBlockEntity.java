package eu.midnightdust.visualoverhaul.fabric.mixin;
import eu.midnightdust.visualoverhaul.fabric.InventorySync;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity {
    @Unique private final InventorySync vo$sync = new InventorySync();
    @Inject(method="serverTick", at=@At("TAIL"))
    private static void vo$sync(ServerLevel level, BlockPos pos, BlockState state, BrewingStandBlockEntity entity, CallbackInfo ci) {
        if (level instanceof ServerLevel) ((MixinBrewingStandBlockEntity)(Object)entity).vo$sync.tick(entity, entity, UPDATE_TYPE_POTION_BOTTLES);
    }
}

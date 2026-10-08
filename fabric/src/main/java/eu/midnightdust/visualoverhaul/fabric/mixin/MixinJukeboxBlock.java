package eu.midnightdust.visualoverhaul.fabric.mixin;
import eu.midnightdust.visualoverhaul.fabric.InventorySync;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(JukeboxBlockEntity.class)
public abstract class MixinJukeboxBlock {
    @Unique private final InventorySync vo$sync = new InventorySync();
    @Inject(method="tick", at=@At("TAIL"))
    private static void vo$sync(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity entity, CallbackInfo ci) {
        if (level instanceof ServerLevel) ((MixinJukeboxBlock)(Object)entity).vo$sync.tick(entity, entity, UPDATE_TYPE_RECORD);
    }
    // Empty jukeboxes have no vanilla ticker: send removal immediately.
    @Inject(method="setTheItem", at=@At("TAIL"))
    private void vo$itemChanged(net.minecraft.world.item.ItemStack stack, CallbackInfo ci) {
        var entity=(JukeboxBlockEntity)(Object)this;
        if(entity.getLevel() instanceof ServerLevel) vo$sync.tick(entity,entity,UPDATE_TYPE_RECORD);
    }
}

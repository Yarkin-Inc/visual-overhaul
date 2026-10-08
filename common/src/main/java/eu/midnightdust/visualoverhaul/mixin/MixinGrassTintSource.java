package eu.midnightdust.visualoverhaul.mixin;

import eu.midnightdust.visualoverhaul.VisualOverhaulClient;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrassColorSource.class)
public abstract class MixinGrassTintSource {
    @Inject(at = @At("RETURN"), method = "calculate", cancellable = true)
    public void vo$modifyGrassTint(ItemStack stack, ClientLevel world, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        // Dynamic Grass Item colors
        if (VOConfig.coloredItems) {
            cir.setReturnValue(VisualOverhaulClient.grassColor);
        }
    }
}

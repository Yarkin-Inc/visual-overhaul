package eu.midnightdust.visualoverhaul.mixin;

import eu.midnightdust.visualoverhaul.VisualOverhaulClient;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Constant.class)
public abstract class MixinConstantTintSource {
    @Inject(at = @At("RETURN"), method = "calculate", cancellable = true)
    public void vo$modifyLeafTint(ItemStack stack, ClientLevel world, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        // Dynamic Leaf Item colors
        if (VOConfig.coloredItems && Objects.equals(-12012264, cir.getReturnValue())) {
            cir.setReturnValue(VisualOverhaulClient.foliageColor);
        }
    }
}

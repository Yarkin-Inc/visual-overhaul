package eu.midnightdust.visualoverhaul.mixin;

import eu.midnightdust.visualoverhaul.VisualOverhaulClient;
import eu.midnightdust.visualoverhaul.config.VOConfig;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(net.minecraft.client.color.item.Potion.class)
public class MixinPotionTintSource {
    @Unique private static final List<Holder<Potion>> WATER_POTIONS = List.of(Potions.WATER, Potions.MUNDANE, Potions.THICK, Potions.AWKWARD);

    @Inject(at = @At("RETURN"), method = "calculate", cancellable = true)
    public void vo$modifyWaterTint(ItemStack stack, ClientLevel world, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        // Dynamic Potion Item colors
        if (VOConfig.coloredItems) {
            var contents = stack.getComponents().get(DataComponents.POTION_CONTENTS);
            if (contents == null && !stack.is(net.minecraft.world.item.Items.WATER_BUCKET)) return;
            if (contents != null && (contents.customColor().isPresent() || contents.hasEffects())) return;
            if (contents != null && contents.potion().isPresent()) {
                if (!WATER_POTIONS.contains(contents.potion().get()))
                    return; // Skip all potions with effects
            }
            if (cir.getReturnValue() == -1) cir.setReturnValue(ARGB.opaque(VisualOverhaulClient.potionColor));
            else cir.setReturnValue(VisualOverhaulClient.potionColor);
        }
    }
}

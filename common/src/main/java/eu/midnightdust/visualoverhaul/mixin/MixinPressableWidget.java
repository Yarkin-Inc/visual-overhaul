package eu.midnightdust.visualoverhaul.mixin;
import eu.midnightdust.visualoverhaul.IconicButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractButton.class)
public abstract class MixinPressableWidget {
    @Unique private final IconicButtons vo$icons=new IconicButtons();
    @Inject(method="extractWidgetRenderState",at=@At("TAIL"))
    private void vo$icons(GuiGraphicsExtractor gui,int x,int y,float delta,CallbackInfo ci) {
        var widget=(AbstractWidget)(Object)this;
        vo$icons.renderIcons(gui,widget,widget.getAlpha());
    }
}

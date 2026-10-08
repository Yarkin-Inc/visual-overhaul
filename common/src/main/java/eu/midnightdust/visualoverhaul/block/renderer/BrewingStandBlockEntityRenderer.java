package eu.midnightdust.visualoverhaul.block.renderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
public final class BrewingStandBlockEntityRenderer implements BlockEntityRenderer<BrewingStandBlockEntity, BrewingStandBlockEntityRenderer.State> {
    private final ItemModelResolver items;
    public BrewingStandBlockEntityRenderer(BlockEntityRendererProvider.Context context) { items = context.itemModelResolver(); }
    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState[] bottles = {new ItemStackRenderState(), new ItemStackRenderState(), new ItemStackRenderState()};
    }
    public State createRenderState() { return new State(); }
    public void extractRenderState(BrewingStandBlockEntity entity, State state, float tick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay damage) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tick, camera, damage);
        for (int i=0; i<3; i++) items.updateForTopItem(state.bottles[i], VOConfig.brewingstand ? entity.getItem(i) : ItemStack.EMPTY, ItemDisplayContext.GROUND, entity.getLevel(), null, i);
    }
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i=0; i<3; i++) {
            pose.pushPose();
            if (i==0) { pose.translate(.86f,.23f,.5f); pose.rotate(Axis.YP.rotationDegrees(180)); }
            if (i==1) { pose.rotate(Axis.YP.rotationDegrees(315)); pose.translate(.32f,.23f,0); }
            if (i==2) { pose.rotate(Axis.YP.rotationDegrees(45)); pose.translate(-.39f,.23f,.705f); }
            pose.scale(1.15f,1.15f,1.15f);
            state.bottles[i].submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);
            pose.popPose();
        }
    }
}

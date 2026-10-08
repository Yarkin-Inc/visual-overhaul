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
import eu.midnightdust.visualoverhaul.FakeBlocks;
import eu.midnightdust.visualoverhaul.util.SoundTest;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
public final class JukeboxBlockEntityRenderer implements BlockEntityRenderer<JukeboxBlockEntity,JukeboxBlockEntityRenderer.State> {
    public JukeboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
    public static final class State extends BlockEntityRenderState {
        ItemQuads disc=ItemQuads.EMPTY, top=ItemQuads.EMPTY;
        float rotation;
    }
    public State createRenderState() { return new State(); }
    public void extractRenderState(JukeboxBlockEntity entity, State state,float tick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay damage) {
        BlockEntityRenderer.super.extractRenderState(entity,state,tick,camera,damage);
        state.disc=ItemQuads.EMPTY; state.top=ItemQuads.EMPTY;
        var level=entity.getLevel();
        if(!VOConfig.jukebox || level==null) return;
        state.lightCoords=net.minecraft.util.LightCoordsUtil.getLightCoords(level,entity.getBlockPos().above());
        var stack=jukeboxItems.getOrDefault(entity.getBlockPos(),entity.getTheItem());
        Identifier disc = stack.isEmpty() ? null : RoundDiscRenderer.getModelId(stack);
        var sound=SoundTest.getSound(entity.getBlockPos());
        if(disc==null && sound!=null) disc=Identifier.tryParse(sound.toString().replace('.', '_'));
        if(disc!=null) state.disc=RoundDiscRenderer.getModel(disc);
        state.rotation=(level.getGameTime()%6480+tick)*50f/9f;
        if(VOConfig.jukebox_fake_block && !level.getBlockState(entity.getBlockPos().above()).isFaceSturdy(level,entity.getBlockPos().above(),Direction.DOWN))
            state.top=FakeBlocks.getModel(id(disc==null ? "jukebox_top_stopped" : "jukebox_top_playing"));
    }
    public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        pose.pushPose(); pose.translate(.5f,1.03f,.5f); pose.scale(.75f,.75f,.75f); pose.rotate(Axis.YP.rotationDegrees(state.rotation));
        pose.translate(-.5f,0,-.5f);
        submit(state.disc,state,pose,collector); pose.popPose();
        pose.pushPose(); pose.translate(0,1,0); submit(state.top,state,pose,collector); pose.popPose();
    }
    private static void submit(ItemQuads quads,State state,PoseStack pose,SubmitNodeCollector collector) {
        if(!quads.isEmpty()) collector.submitItem(pose,ItemDisplayContext.NONE,state.lightCoords,OverlayTexture.NO_OVERLAY,0,new int[0],quads,ItemStackRenderState.FoilType.NONE);
    }
}

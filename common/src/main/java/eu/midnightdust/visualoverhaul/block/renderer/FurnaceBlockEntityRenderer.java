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
import eu.midnightdust.visualoverhaul.block.model.FurnaceWoodenPlanksModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
public final class FurnaceBlockEntityRenderer<E extends AbstractFurnaceBlockEntity> implements BlockEntityRenderer<E,FurnaceBlockEntityRenderer.State> {
    private final ItemModelResolver items;
    private final FurnaceWoodenPlanksModel planks;
    public FurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        items = context.itemModelResolver();
        planks = new FurnaceWoodenPlanksModel(context.bakeLayer(FurnaceWoodenPlanksModel.WOODEN_PLANKS_MODEL_LAYER));
    }
    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState food = new ItemStackRenderState(), fuel = new ItemStackRenderState();
        Identifier woodTexture;
        float yaw, foodY, fuelY, woodY;
    }
    public State createRenderState() { return new State(); }
    public void extractRenderState(E entity, State state, float tick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay damage) {
        BlockEntityRenderer.super.extractRenderState(entity,state,tick,camera,damage);
        var block = entity.getBlockState();
        var facing = block.getValue(AbstractFurnaceBlock.FACING);
        state.yaw = 180-facing.toYRot();
        boolean smoker = block.is(Blocks.SMOKER), blast = block.is(Blocks.BLAST_FURNACE);
        state.foodY = .58f-(smoker ? .06f : blast ? .25f : 0);
        state.fuelY = .08f+(smoker ? .06f : blast ? .24f : 0);
        state.woodY = -1.3f+(smoker ? .06f : blast ? .2f : 0);
        if(entity.getLevel()!=null) state.lightCoords=net.minecraft.util.LightCoordsUtil.getLightCoords(entity.getLevel(),entity.getBlockPos().relative(facing));
        var food = VOConfig.furnace ? (entity.getItem(0).isEmpty() ? entity.getItem(2) : entity.getItem(0)) : ItemStack.EMPTY;
        var fuel = VOConfig.furnace ? entity.getItem(1) : ItemStack.EMPTY;
        state.woodTexture = null;
        if (!fuel.isEmpty() && (fuel.is(ItemTags.LOGS_THAT_BURN) || fuel.is(ItemTags.PLANKS))) {
            var sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(Block.byItem(fuel.getItem()).defaultBlockState()).particleMaterial().sprite();
            state.woodTexture = sprite.contents().name().withPath(p -> "textures/"+p+".png");
        }
        items.updateForTopItem(state.food,food,ItemDisplayContext.GROUND,entity.getLevel(),null,0);
        items.updateForTopItem(state.fuel,state.woodTexture == null ? fuel : ItemStack.EMPTY,ItemDisplayContext.GROUND,entity.getLevel(),null,1);
    }
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        submitItem(state.food,state.foodY,state,pose,collector);
        submitItem(state.fuel,state.fuelY,state,pose,collector);
        if(state.woodTexture != null) {
            pose.pushPose(); pose.translate(.5f,state.woodY,.5f); pose.rotate(Axis.YP.rotationDegrees(state.yaw));
            collector.submitModelPart(planks.getPart(),pose,RenderTypes.entityCutout(state.woodTexture),state.lightCoords,OverlayTexture.NO_OVERLAY,null);
            pose.popPose();
        }
    }
    private static void submitItem(ItemStackRenderState item,float y,State state,PoseStack pose,SubmitNodeCollector collector) {
        pose.pushPose(); pose.translate(.5f,y,.5f); pose.rotate(Axis.YP.rotationDegrees(state.yaw));
        pose.translate(0,0,-.4f); pose.rotate(Axis.XP.rotationDegrees(90));
        item.submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0); pose.popPose();
    }
}

package eu.midnightdust.visualoverhaul.block.model;

import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import com.mojang.blaze3d.vertex.PoseStack;

import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.id;

public class FurnaceWoodenPlanksModel {
    private final ModelPart bb_main;
    public static final ModelLayerLocation WOODEN_PLANKS_MODEL_LAYER = new ModelLayerLocation(id("wooden_planks"), "main");

    public FurnaceWoodenPlanksModel(ModelPart root) {
        bb_main = root;
        bb_main.setPos(0.0F, 24.0F, 0.0F);
    }
    public ModelPart getPart() {
        return bb_main;
    }

    public static LayerDefinition getLayerDefinition() {
        return LayerDefinition.create(getMeshDefinition(), 16, 16);
    }

    public static MeshDefinition getMeshDefinition(){
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -3.0F, 0.0F, 10.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(6.0F, 1.0F, -2.0F,0.0F, -0.5672F, 0.0F));
        modelPartData.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -2.5F, 0.0F, 10.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(5.0F, 0.0F, -5.0F,0.0F, -0.1309F, 0.0F));
        modelPartData.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -2.0F, 0.0F, 10.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(5.0F, -1.0F, -7.0F,0.0F, 0.2618F, 0.0F));
        return modelData;
    }

//    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
//        bb_main.render(matrices, vertices, light, overlay);
//    }
}

package eu.midnightdust.visualoverhaul.fabric;

import eu.midnightdust.visualoverhaul.FakeBlocks;
import eu.midnightdust.visualoverhaul.IconicButtons;
import eu.midnightdust.visualoverhaul.VisualOverhaulClient;
import eu.midnightdust.visualoverhaul.block.model.FurnaceWoodenPlanksModel;
import eu.midnightdust.visualoverhaul.block.renderer.BrewingStandBlockEntityRenderer;
import eu.midnightdust.visualoverhaul.block.renderer.FurnaceBlockEntityRenderer;
import eu.midnightdust.visualoverhaul.block.renderer.JukeboxBlockEntityRenderer;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import eu.midnightdust.visualoverhaul.packet.HelloPacket;
import eu.midnightdust.visualoverhaul.packet.UpdateItemsPacket;
import eu.midnightdust.visualoverhaul.util.VOColorUtil;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.packs.PackType;

import static eu.midnightdust.visualoverhaul.VisualOverhaulClient.*;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;

public class VisualOverhaulClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VisualOverhaulClient.onInitializeClient();

        ModelLayerRegistry.registerModelLayer(FurnaceWoodenPlanksModel.WOODEN_PLANKS_MODEL_LAYER, FurnaceWoodenPlanksModel::getLayerDefinition);


        BlockEntityRenderers.register(BlockEntityTypes.BREWING_STAND, BrewingStandBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypes.JUKEBOX, JukeboxBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypes.FURNACE, FurnaceBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypes.SMOKER, FurnaceBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntityTypes.BLAST_FURNACE, FurnaceBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(UpdateItemsPacket.PACKET_ID,
                (payload, context) -> context.client().execute(() -> {
                    if (payload.blockTypeID().equals(UPDATE_TYPE_RECORD)) jukeboxItems.put(payload.pos(), payload.inv().getFirst());
                    else if (context.client().level != null && context.client().level.getBlockEntity(payload.pos()) != null) {
                        if (payload.blockTypeID().equals(UPDATE_TYPE_POTION_BOTTLES) && context.client().level.getBlockEntity(payload.pos()) instanceof BrewingStandBlockEntity brewingStand) {
                            for (int i = 0; i <= 4; i++) {
                                brewingStand.setItem(i, payload.inv().get(i));
                            }
                        } else if (payload.blockTypeID().equals(UPDATE_TYPE_FURNACE_ITEMS) && context.client().level.getBlockEntity(payload.pos()) instanceof AbstractFurnaceBlockEntity furnace) {
                            for (int i = 0; i <= 2; i++) {
                                furnace.setItem(i, payload.inv().get(i));
                            }
                        }
                    }
                }));

        // Register builtin resourcepacks
        FabricLoader.getInstance().getModContainer("visualoverhaul").ifPresent(modContainer -> {
            ResourceLoader.registerBuiltinPack(id("nobrewingbottles"), modContainer, PackActivationType.DEFAULT_ENABLED);
            ResourceLoader.registerBuiltinPack(id("fancyfurnace"), modContainer, PackActivationType.DEFAULT_ENABLED);
            ResourceLoader.registerBuiltinPack(id("coloredwaterbucket"), modContainer, PackActivationType.DEFAULT_ENABLED);
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null && ClientPlayNetworking.canSend(HelloPacket.PACKET_ID)) {
                sender.sendPacket(new HelloPacket(client.player.getUUID()));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            jukeboxItems.clear(); eu.midnightdust.visualoverhaul.util.SoundTest.soundPos.clear();
        });
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
            jukeboxItems.clear();
            eu.midnightdust.visualoverhaul.util.SoundTest.soundPos.clear();
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.level != null) jukeboxItems.keySet().removeIf(pos ->
                !(client.level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity));
        });
        // Biome-colored Items
        {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (VOConfig.coloredItems && client.level != null && client.player != null) {
                    waterColor = net.minecraft.util.ARGB.opaque(BiomeColors.getAverageWaterColor(client.level, client.player.blockPosition()));
                    foliageColor = net.minecraft.util.ARGB.opaque(BiomeColors.getAverageFoliageColor(client.level, client.player.blockPosition()));
                    grassColor = net.minecraft.util.ARGB.opaque(BiomeColors.getAverageGrassColor(client.level, client.player.blockPosition()));
                    potionColor = VOColorUtil.convertRgbToArgb(waterColor, 200);
                } else {
                    waterColor = 4159204;
                    foliageColor = -8934609;
                    grassColor = -8934609;
                    potionColor = -13083194;
                }
            });
        }
        if (VOConfig.coloredLilypad) {
            BlockColorRegistry.register(java.util.List.of(BlockTintSources.foliage()), Blocks.LILY_PAD);
        }

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id("visual_resources"),
                (ResourceManagerReloadListener) manager -> {
                    IconicButtons.reload(manager);
                    FakeBlocks.reload(manager);
                    eu.midnightdust.visualoverhaul.block.renderer.RoundDiscRenderer.clear();
                });
    }
}

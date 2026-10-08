package eu.midnightdust.visualoverhaul;

import eu.midnightdust.visualoverhaul.block.renderer.RoundDiscRenderer;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import eu.midnightdust.visualoverhaul.packet.UpdateItemsPacket;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import static eu.midnightdust.visualoverhaul.VisualOverhaulCommon.*;

public class VisualOverhaulGameTest implements FabricClientGameTest {
    private static final BlockPos FURNACE = new BlockPos(0,-60,0), SMOKER = new BlockPos(2,-60,0), BLAST = new BlockPos(4,-60,0), BREWING = new BlockPos(6,-60,0), JUKEBOX = new BlockPos(8,-60,0);
    @Override public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            VOConfig.buttonIcons = true;
            check(client.getResourcePackRepository().getSelectedIds().containsAll(List.of("visualoverhaul:fancyfurnace","visualoverhaul:nobrewingbottles","visualoverhaul:coloredwaterbucket")),"All builtin resource packs enabled");
            check(!RoundDiscRenderer.getModel(net.minecraft.resources.Identifier.withDefaultNamespace("music_disc_cat")).isEmpty(),"Disc model baked");
            check(!FakeBlocks.getModel(id("jukebox_top_playing")).isEmpty(),"Playing jukebox model baked");
            check(!FakeBlocks.getModel(id("jukebox_top_stopped")).isEmpty(),"Stopped jukebox model baked");
        });
        context.waitTicks(50);
        context.takeScreenshot("visualoverhaul-buttons");
        try (var world = context.worldBuilder().create()) {
            world.getServer().runCommand("tp @a 4.5 -60 -8 0 10");
            world.getConnection().waitForChunksDownload();
            world.getServer().runOnServer(server -> {
                var level=server.overworld();
                level.setBlockAndUpdate(FURNACE,Blocks.FURNACE.defaultBlockState());
                level.setBlockAndUpdate(SMOKER,Blocks.SMOKER.defaultBlockState());
                level.setBlockAndUpdate(BLAST,Blocks.BLAST_FURNACE.defaultBlockState());
                level.setBlockAndUpdate(BREWING,Blocks.BREWING_STAND.defaultBlockState());
                level.setBlockAndUpdate(JUKEBOX,Blocks.JUKEBOX.defaultBlockState());
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(FURNACE)).setItem(0,new ItemStack(Items.RAW_IRON,32));
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(FURNACE)).setItem(1,new ItemStack(Items.OAK_PLANKS,64));
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(SMOKER)).setItem(0,new ItemStack(Items.BEEF,32));
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(SMOKER)).setItem(1,new ItemStack(Items.COAL,64));
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(BLAST)).setItem(0,new ItemStack(Items.RAW_GOLD,32));
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(BLAST)).setItem(1,new ItemStack(Items.BIRCH_PLANKS,64));
                var brewing=(BrewingStandBlockEntity)level.getBlockEntity(BREWING);
                for(int i=0;i<3;i++) {
                    var bottle=new ItemStack(Items.POTION);
                    bottle.set(DataComponents.POTION_CONTENTS,new PotionContents(i==0 ? Potions.WATER : Potions.HEALING));
                    brewing.setItem(i,bottle);
                }
                ((JukeboxBlockEntity)level.getBlockEntity(JUKEBOX)).setTheItem(new ItemStack(Items.MUSIC_DISC_CAT));
            });
            context.waitFor(client -> client.level.getBlockEntity(FURNACE) instanceof AbstractFurnaceBlockEntity furnace && furnace.getItem(0).is(Items.RAW_IRON));
            context.waitFor(client -> client.level.getBlockEntity(BREWING) instanceof BrewingStandBlockEntity brewing && brewing.getItem(2).is(Items.POTION));
            context.waitFor(client -> jukeboxItems.getOrDefault(JUKEBOX,ItemStack.EMPTY).is(Items.MUSIC_DISC_CAT));
            context.runOnClient(client -> {
                var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),client.level.registryAccess());
                try {
                    var source=new ItemStack(Items.DIAMOND,2);
                    var packet=new UpdateItemsPacket(UPDATE_TYPE_FURNACE_ITEMS,FURNACE,List.of(source,ItemStack.EMPTY,new ItemStack(Items.IRON_INGOT)));
                    source.setCount(9);
                    UpdateItemsPacket.codec.encode(buffer,packet);
                    var decoded=UpdateItemsPacket.codec.decode(buffer);
                    check(decoded.pos().equals(FURNACE) && decoded.inv().getFirst().getCount()==2 && decoded.inv().get(1).isEmpty(),"Packet snapshot and round trip");
                    check(buffer.readableBytes()==0,"Packet consumes exactly its payload");
                } finally {buffer.release();}
            });
            world.getConnection().waitForChunksRender();
            context.waitTicks(30);
            context.takeScreenshot("visualoverhaul-blocks");
            context.runOnClient(client -> {
                var potionTint=new net.minecraft.client.color.item.Potion(-1);
                var bucket=new ItemStack(Items.WATER_BUCKET);
                check(potionTint.calculate(bucket,client.level,client.player)==net.minecraft.util.ARGB.opaque(VisualOverhaulClient.potionColor),"Water bucket biome tint");
                var bottle=new ItemStack(Items.POTION);
                bottle.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.HEALING));
                VOConfig.coloredItems=false;
                int original=potionTint.calculate(bottle,client.level,client.player);
                VOConfig.coloredItems=true;
                check(potionTint.calculate(bottle,client.level,client.player)==original,"Effect potion retains its original color");
            });
            var reload=context.computeOnClient(client -> client.reloadResourcePacks());
            context.waitFor(client -> reload.isDone(),1200);
            check(!reload.isCompletedExceptionally(),"Resource reload succeeded");
            context.waitTicks(5);
            context.runOnClient(client -> check(!FakeBlocks.getModel(id("jukebox_top_playing")).isEmpty(),"Model rebuilt after resource reload"));
            world.getServer().runCommand("tp @a 1000 -60 1000");
            context.waitFor(client -> client.level.getBlockEntity(BREWING)==null,1200);
            world.getServer().runCommand("tp @a 4.5 -60 -8 0 10");
            context.waitFor(client -> client.level.getBlockEntity(BREWING) instanceof BrewingStandBlockEntity brewing && brewing.getItem(2).is(Items.POTION),1200);
            context.waitFor(client -> jukeboxItems.getOrDefault(JUKEBOX,ItemStack.EMPTY).is(Items.MUSIC_DISC_CAT),1200);

            world.getServer().runOnServer(server -> {
                var level=server.overworld();
                ((JukeboxBlockEntity)level.getBlockEntity(JUKEBOX)).setTheItem(ItemStack.EMPTY);
                ((BrewingStandBlockEntity)level.getBlockEntity(BREWING)).setItem(2,ItemStack.EMPTY);
                ((AbstractFurnaceBlockEntity)level.getBlockEntity(FURNACE)).setItem(0,ItemStack.EMPTY);
            });
            context.waitFor(client -> jukeboxItems.getOrDefault(JUKEBOX,ItemStack.EMPTY).isEmpty());
            context.waitFor(client -> ((BrewingStandBlockEntity)client.level.getBlockEntity(BREWING)).getItem(2).isEmpty());
            context.waitFor(client -> ((AbstractFurnaceBlockEntity)client.level.getBlockEntity(FURNACE)).getItem(0).isEmpty());
            context.takeScreenshot("visualoverhaul-empty");
        }
        context.runOnClient(client -> check(jukeboxItems.isEmpty(),"Disconnect clears jukebox cache"));
        LOGGER.info("Visual Overhaul 26.3: models, inventory synchronization, packet codec, colors, resource reload, chunk revisit and disconnect: PASS");
    }
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}

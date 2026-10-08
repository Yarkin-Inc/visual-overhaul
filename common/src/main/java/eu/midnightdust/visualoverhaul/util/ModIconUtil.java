package eu.midnightdust.visualoverhaul.util;
import java.io.*;
import java.nio.file.*;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
public final class ModIconUtil {
    private final String modid;
    public ModIconUtil(String modid) { this.modid=modid; }
    public static Path getPath(String modid) {
        var mod=FabricLoader.getInstance().getModContainer(modid).orElseThrow();
        return mod.findPath(mod.getMetadata().getIconPath(16).orElseThrow()).orElseThrow();
    }
    public DynamicTexture createModIcon() {
        try(var stream=Files.newInputStream(getPath(modid))) {
            var image=NativeImage.read(stream);
            return new DynamicTexture(() -> "Visual Overhaul mod icon: "+modid,image);
        } catch(IOException | RuntimeException e) { return null; }
    }
}

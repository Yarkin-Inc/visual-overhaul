package eu.midnightdust.visualoverhaul;
import eu.midnightdust.visualoverhaul.config.VOConfig;
import eu.midnightdust.visualoverhaul.util.ModIconUtil;
import eu.midnightdust.visualoverhaul.util.VOColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.*;
import java.util.*;
public final class IconicButtons {
    private static final Map<Identifier,Identifier> ICONS=new HashMap<>();
    private static final Set<Identifier> MOD_ICONS=new HashSet<>();
    private static int generation;
    private int seenGeneration=-1;
    private Component previous;
    private Identifier icon;
    private void init(AbstractWidget widget) {
        previous=widget.getMessage(); seenGeneration=generation; icon=null;
        if(!(previous.getContents() instanceof TranslatableContents text)) return;
        String key=text.getKey().toLowerCase(Locale.ROOT);
        var client=Minecraft.getInstance();
        if(key.endsWith(".midnightconfig.title")) {
            var mod=key.substring(0,key.length()-".midnightconfig.title".length());
            var id=Identifier.tryParse("visualoverhaul:mod_icons/"+mod);
            if(id==null) return;
            if(!MOD_ICONS.contains(id)) {
                var texture=new ModIconUtil(mod).createModIcon();
                if(texture==null) return;
                client.getTextureManager().register(id,texture); MOD_ICONS.add(id);
            }
            icon=id; return;
        }
        var id=Identifier.tryParse("iconic:textures/gui/icons/"+key+".png");
        if(id!=null) {
            if(client.getResourceManager().getResource(id).isPresent()) icon=id;
            else icon=ICONS.get(id);
        }
    }
    public void renderIcons(GuiGraphicsExtractor gui,AbstractWidget widget,float alpha) {
        if(!VOConfig.buttonIcons || widget.getWidth()<=20) return;
        if(previous!=widget.getMessage() || seenGeneration!=generation) init(widget);
        if(icon==null) return;
        var client=Minecraft.getInstance();
        int center=gui.guiWidth()/2;
        boolean limited=client.font.width(widget.getMessage())>widget.getWidth()*.75f;
        var position=VOConfig.buttonIconPosition;
        boolean left=position==VOConfig.IconPosition.LEFT || (position==VOConfig.IconPosition.BOTH && (!limited || widget.getX()<center)) || (position==VOConfig.IconPosition.LOCATION && widget.getX()<center);
        boolean right=position==VOConfig.IconPosition.RIGHT || (position==VOConfig.IconPosition.BOTH && (!limited || widget.getX()>=center)) || (position==VOConfig.IconPosition.LOCATION && widget.getRight()>center);
        int inset=VOConfig.zoomIconOnHover && widget.isHoveredOrFocused() && widget.active ? 1 : 2;
        int size=Math.min(20,widget.getHeight())-inset*2;
        int color=VOColorUtil.alphaAndBrightness(alpha,widget.active ? 1 : .3f);
        if(left) gui.blit(RenderPipelines.GUI_TEXTURED,icon,widget.getX()+inset,widget.getY()+inset,0,0,size,size,size,size,size,size,color);
        if(right) gui.blit(RenderPipelines.GUI_TEXTURED,icon,widget.getRight()-size-inset,widget.getY()+inset,0,0,size,size,size,size,size,size,color);
    }
    public static void reload(ResourceManager manager) {
        ICONS.clear(); generation++;
        manager.listResources("textures/gui/icons",id -> id.getNamespace().equals("iconic") && id.getPath().endsWith(".properties")).forEach((id,resource) -> {
            try {
                var target=resolve(manager,id,new HashSet<>());
                if(target!=null) ICONS.put(id.withPath(p -> p.substring(0,p.length()-11)+".png"),target);
            } catch(IOException | RuntimeException e) { VisualOverhaulCommon.LOGGER.warn("Cannot load button icon {}",id,e); }
        });
    }
    private static Identifier resolve(ResourceManager manager,Identifier properties,Set<Identifier> seen) throws IOException {
        if(!seen.add(properties)) return null;
        var png=properties.withPath(p -> p.replace(".properties",".png"));
        if(manager.getResource(png).isPresent()) return png;
        var resource=manager.getResource(properties);
        if(resource.isEmpty()) return null;
        var data=new Properties();
        try(var stream=resource.get().open()) { data.load(stream); }
        if(data.getProperty("properties")!=null) return resolve(manager,Identifier.parse(data.getProperty("properties")),seen);
        var texture=Identifier.tryParse(data.getProperty("texture",""));
        return texture!=null && manager.getResource(texture).isPresent() ? texture : null;
    }
}

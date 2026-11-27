package dev.crystallmoon.astra;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AstraScreen extends Screen {
    private static final int BG=0xE910131A, PANEL=0xF51B202A, CARD=0xE9252C38;
    private static final int ACCENT=0xFF9BC7FF, WHITE=0xFFF4F7FC, MUTED=0xFF9AA7B8;
    private static final String[] CATEGORIES={"All","HUD","Player","World","Items","Visual","Network"};
    private static final int PAGE_SIZE=12;
    private String category="All", selectedModule=null;
    private int page=0;
    public AstraScreen(){super(Component.literal("Astra Menu"));}

    @Override protected void init(){
        clearWidgets();
        int left=Math.max(12,(width-900)/2), top=Math.max(12,(height-520)/2);
        for(int i=0;i<CATEGORIES.length;i++){
            String name=CATEGORIES[i]; int y=top+112+i*39;
            addRenderableWidget(Button.builder(Component.literal((category.equals(name)?"◆  ":"   ")+name),b->{
                category=name; selectedModule=null; page=0; init();
            }).bounds(left+18,y,145,31).build());
        }
        if(selectedModule!=null){
            addRenderableWidget(Button.builder(Component.literal("‹  BACK TO MODULES"),b->{selectedModule=null;init();})
                    .bounds(left+190,top+455,160,28).build());
            return;
        }
        List<String> modules=filteredModules();
        int pages=Math.max(1,(modules.size()+PAGE_SIZE-1)/PAGE_SIZE);
        page=Math.max(0,Math.min(page,pages-1));
        for(int i=page*PAGE_SIZE;i<Math.min(modules.size(),(page+1)*PAGE_SIZE);i++){
            String name=modules.get(i); int local=i-page*PAGE_SIZE, col=local%2, row=local/2;
            int x=left+190+col*330, y=top+157+row*55;
            addRenderableWidget(Button.builder(Component.literal((AstraModules.isEnabled(name)?"●  ":"○  ")+name),b->{
                AstraModules.toggle(name);
                b.setMessage(Component.literal((AstraModules.isEnabled(name)?"●  ":"○  ")+name));
            }).bounds(x,y,282,38).build());
            addRenderableWidget(Button.builder(Component.literal("⚙"),b->{selectedModule=name;init();})
                    .bounds(x+288,y,34,38).build());
        }
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page--;init();})
                .bounds(left+190,top+455,38,28).build());
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page++;init();})
                .bounds(left+236,top+455,38,28).build());
    }

    private List<String> filteredModules(){
        List<String> result=new ArrayList<>();
        for(String name:AstraModules.all().keySet())
            if(category.equals("All")||categoryOf(name).equals(category)) result.add(name);
        return result;
    }
    private static String categoryOf(String n){
        if(List.of("Coordinates","Keystrokes","FPS Counter","CPS Counter","Direction","Clock","Health HUD","Ping Display","Hunger HUD","Saturation HUD","Experience HUD","Speedometer","Status Effects","Sprint Status","Armor Status","Health Bar","Absorption","Air HUD","Coordinates HUD","Crosshair","FPS Graph","Ping Graph","Memory Usage","Time Played","Session Timer","Real Time").contains(n))return "HUD";
        if(List.of("Movement Speed","Jump Height","Velocity","Horizontal Speed","Vertical Speed","Elytra Speed","Horse Speed","Boat Speed","Mount Health","Vehicle HUD","Fall Distance","Combat Timer","Last Damage","Target HUD","Nearby Players","Player Counter").contains(n))return "Player";
        if(List.of("World Time","Dimension","Light Level","Biome","Chunk Coordinates","Block Coordinates","Nether Coordinates","Facing","Yaw Pitch","Compass","Weather","Rain Status","Moon Phase","Day Counter","World Age","Difficulty","Game Mode","Dimension ID","Spawnable Light","Chunk Updates","Block Info","Target Block","Looking At","Block Resistance","Mining Speed","Harvest Level","Light Overlay","Breaking Progress").contains(n))return "World";
        if(List.of("Durability","Held Item","Offhand Item","Hotbar","Inventory Status","Empty Slots","Totem Counter","Arrow Counter","Food Counter","Potion Counter","Armor Durability","Tool Durability","Item Counter").contains(n))return "Items";
        if(List.of("Boss Bar","Arrow Trajectory","Effect Icons","Effect Duration","Potion Timers").contains(n))return "Visual";
        if(List.of("Server Address","Server Brand","Server TPS","Packet Rate","Network Status","Java Version","Client Version","Nearby Entities","Entity Counter","Mob Counter","Dropped Items","Projectile Counter").contains(n))return "Network";
        return "All";
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        int l=Math.max(12,(width-900)/2), t=Math.max(12,(height-520)/2), r=Math.min(width-12,l+900), b=Math.min(height-12,t+520);
        g.fill(0,0,width,height,0xA9000000); g.fill(l,t,r,b,BG); g.fill(l,t,r,t+82,PANEL);
        g.fill(l,t+82,l+174,b,PANEL); g.fill(l,t+81,r,t+83,ACCENT); g.fill(l+173,t+82,l+175,b,0x553F526B);
        g.text(font,"ASTRA",l+22,t+18,WHITE); g.text(font,"CLIENT",l+91,t+21,ACCENT);
        g.text(font,"A clean space for your Minecraft setup",l+22,t+48,MUTED);
        g.text(font,"RIGHT SHIFT  /  CLOSE",r-172,t+24,MUTED); g.text(font,"LIBRARY",l+22,t+97,MUTED);
        int enabled=0; for(boolean state:AstraModules.all().values())if(state)enabled++;
        g.text(font,"ASTRA MODULE LIBRARY",l+190,t+94,WHITE);
        g.text(font,enabled+" ENABLED  ·  "+AstraModules.all().size()+" AVAILABLE",r-255,t+94,ACCENT);
        if(selectedModule!=null){
            g.fill(l+190,t+145,r-20,b-62,CARD); g.text(font,"MODULE SETTINGS",l+214,t+170,ACCENT);
            g.text(font,selectedModule,l+214,t+202,WHITE);
            g.text(font,"Status: "+(AstraModules.isEnabled(selectedModule)?"Enabled":"Disabled"),l+214,t+235,MUTED);
            g.text(font,"Use the module tile to toggle this feature.",l+214,t+273,WHITE);
            g.text(font,"Additional per-module controls will appear here when supported.",l+214,t+296,MUTED);
        }else{
            List<String> modules=filteredModules(); int pages=Math.max(1,(modules.size()+PAGE_SIZE-1)/PAGE_SIZE);
            g.text(font,category+"  /  "+modules.size()+" MODULES",l+190,t+119,WHITE);
            g.text(font,"Select a tile to toggle it. Open the gear for module details.",l+190,t+137,MUTED);
            g.text(font,"PAGE "+(page+1)+" OF "+pages,l+190,b-42,MUTED);
        }
        g.text(font,"ASTRA  •  BUILT FOR YOUR CLIENT",l+22,b-24,MUTED);
        g.text(font,"CONFIG SAVES AUTOMATICALLY",r-222,b-24,MUTED);
        super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean isPauseScreen(){return false;}
}

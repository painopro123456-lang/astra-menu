package dev.crystallmoon.astra;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class AstraScreen extends Screen {
 private static final int PANEL=0xF0181A1F,PANEL_ALT=0xFF22252B,ACCENT=0xFF8DB5FF,TEXT=0xFFF1F3F6,MUTED=0xFF9DA4AF;
 private static final int PAGE_SIZE=24;
 private int page=0;
 public AstraScreen(){super(Component.literal("Astra Menu"));}
 @Override protected void init(){
  clearWidgets(); String[] names=AstraModules.all().keySet().toArray(String[]::new);
  int pages=(names.length+PAGE_SIZE-1)/PAGE_SIZE; page=Math.max(0,Math.min(page,pages-1));
  int left=Math.max(12,(width-760)/2),top=Math.max(12,(height-430)/2);
  for(int i=page*PAGE_SIZE;i<Math.min(names.length,(page+1)*PAGE_SIZE);i++){
   final String name=names[i]; int local=i-page*PAGE_SIZE;
   int x=left+18+(local%3)*244,y=top+104+(local/3)*38;
   addRenderableWidget(Button.builder(label(name),b->{AstraModules.toggle(name);b.setMessage(label(name));}).bounds(x,y,226,30).build());
  }
  addRenderableWidget(Button.builder(Component.literal("‹ Previous"),b->{page--;init();}).bounds(left+18,top+385,110,26).build());
  addRenderableWidget(Button.builder(Component.literal("Next ›"),b->{page++;init();}).bounds(left+632,top+385,110,26).build());
 }
 private static Component label(String n){return Component.literal((AstraModules.isEnabled(n)?"[ON]  ":"[OFF]  ")+n);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float pt){
  super.extractRenderState(g,mx,my,pt); int l=Math.max(12,(width-760)/2),t=Math.max(12,(height-430)/2),r=Math.min(width-12,l+760),b=Math.min(height-12,t+430);
  g.fill(0,0,width,height,0x99000000);g.fill(l,t,r,b,PANEL);g.fill(l,t,r,t+42,PANEL_ALT);g.fill(l,t,l+3,b,ACCENT);
  g.text(font,"ASTRA",l+18,t+15,TEXT);g.text(font,"CLIENT  /  MOD MENU",l+75,t+16,MUTED);g.text(font,"RIGHT SHIFT  ·  CLOSE",r-142,t+16,MUTED);
  g.text(font,"MODULE LIBRARY  /  "+AstraModules.all().size(),l+18,t+60,ACCENT);
  g.text(font,"Page "+(page+1)+" of "+((AstraModules.all().size()+PAGE_SIZE-1)/PAGE_SIZE)+"  ·  Toggle modules to enable or disable them",l+18,t+78,MUTED);
  g.text(font,"100-module library · Traditional Minecraft-style HUD direction",l+18,b-25,MUTED);
 }
 @Override public boolean isPauseScreen(){return false;}
}

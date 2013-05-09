import p5_remote.*;

RemoteServer server;
PImage img;

int draw_pimage_idx = 0;
int fade_duration = 40;
int fade_counter = 0;

boolean debug = false;

void setup() {
  size(640, 480);
  server = new RemoteServer(this, 12345);
  server.start();
}

void draw() {
  background(64, 64, 64);

  if (draw_pimage_idx >= 0) {
    drawPImage(draw_pimage_idx);
  }
  else {
  }

  if (debug) drawDebugStatus();
}

void drawPImage(int idx) {
  String [] names = server.getActiveConnectionNames();

  if (names.length == 0) {
    //println("active connection is zero...");
    return;
  }
  if (idx < 0 || names.length <= idx ) {
    println("idx is out of range...idx=" + idx);
    return;
  }
    
  img = server.getPImage(names[idx]);
  if (img == null) {
    println("pimage is null...idx=" + idx + "name=" + names[idx]);
    return;
  }


  if (fade_counter == 0) {
    tint(255, 255, 255, 255); 
  }
  else {
    int a = (int)(255 * (fade_duration - fade_counter) / (float)fade_duration);
    tint(255, 255, 255, a);
    fade_counter --;
  }

  image(img, 0, 0, width, height);
  g.removeCache(img); // issue 1391 : https://github.com/processing/processing/issues/1391

}

void drawDebugStatus() {
  tint(255, 255, 255, 255); 

  stroke(255, 255, 255);
  strokeWeight(2);
  fill(0, 0, 0, 128);
  
  rect(5, 5, 300, 160);
  
  fill(255, 255, 255, 255);
  text("debug information :", 10, 20);
  text("active_connection_num=" + server.getActiveConnectionNum(), 20, 40);
  text("average_fps=" + server.getAverageFps(), 20, 60);
  text("total_bps=" + server.getTotalBpsStr(), 20, 80);
}

void changeIdx(int idx) {
  int max_idx = server.getActiveConnectionNum();
  if (idx < 0 || max_idx <= idx) return;

  draw_pimage_idx = idx;
  fade_counter = fade_duration;
}

void changeNext() {
  draw_pimage_idx ++;
  if (server.getActiveConnectionNum() <= draw_pimage_idx) {
    draw_pimage_idx = 0;
  }
  fade_counter = fade_duration;
}

void changePrev() {
  draw_pimage_idx --;
  if (draw_pimage_idx < 0) {
    draw_pimage_idx = server.getActiveConnectionNum() - 1;
  }
  fade_counter = fade_duration;
}

void keyPressed() {
  println("keyCode=" + keyCode);
  if (keyCode == 'D') {
    debug = !debug;
  }
  else if (keyCode == RIGHT) {
    changeNext();
  }
  else if (keyCode == LEFT) {
    changePrev();
  }
  else if ('0' <= keyCode && keyCode <= '9') {
    changeIdx(keyCode - '0');
  }
}


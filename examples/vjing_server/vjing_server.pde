import p5_remote.*;

RemoteServer server;

int draw_pimage_idx = 0;
int transition_duration = 40;
int transition_counter = 0;

boolean debug = false;
boolean show_blank_image = false;
boolean show_all_image = false;
PImage blank_img;

void setup() {
  size(640, 480);
  server = new RemoteServer(this, 12345);
  server.start();

  blank_img = loadImage("blank_image.jpg");
}

void draw() {
  background(64, 64, 64);

  if (show_blank_image || server.getActiveConnectionNum() == 0) {
    drawBlankImage();
  } 
  else if (show_all_image) {
    drawAllImage();
  } 
  else {
    drawPImage(draw_pimage_idx);
  }

  if (debug) drawDebugStatus();
}

void keyPressed() {
  println("keyCode=" + keyCode);
  if (keyCode == 'D') {
    debug = !debug;
  }
  else if (keyCode == 'B') {
    show_blank_image = !show_blank_image;
    startTransition();
  }
  else if (keyCode == 'A') {
    show_blank_image = false;
    show_all_image = !show_all_image;
    startTransition();
  }
  else if (keyCode == RIGHT || keyCode == ' ') {
    changeNext();
  }
  else if (keyCode == LEFT) {
    changePrev();
  }
  else if ('0' <= keyCode && keyCode <= '9') {
    show_blank_image = false;
    show_all_image = false;
    changeIdx(keyCode - '0');
  }
}

void drawBlankImage() {
  processTransition();
  image(blank_img, 0, 0, width, height);
}

void drawAllImage() {
  processTransition();

  String [] names = server.getActiveConnectionNames();
  if (names.length == 0) {
    //println("active connection is zero...");
    drawBlankImage();
    return;
  }

  int n = names.length;
  int count_w = ceil(sqrt(n));
  int count_h = count_w; // keep aspect...
  
  float disp_w = width / count_w;
  float disp_h = height / count_h;

  int x = 0, y = 0;
  for (int i = 0; i < n; ++i) {
    PImage img = server.getPImage(names[i]);
    if (img != null) {
      image(img, x, y, disp_w, disp_h);
      g.removeCache(img); // issue 1391 : https://github.com/processing/processing/issues/1391
    }
    x += disp_w;
    if (width - disp_w < x) {
      x = 0;
      y += disp_h;
    }
  }
}

void drawPImage(int idx) {
  PImage img = null;

  String [] names = server.getActiveConnectionNames();
  if (names.length == 0) {
    //println("active connection is zero...");
    drawBlankImage();
    return;
  }
  if (idx < 0 || names.length <= idx ) {
    println("idx is out of range...idx=" + idx);
    drawBlankImage();
    return;
  }

  String target_name = names[idx];

  // check active
  if (server.isActive(target_name) == false) {
    drawBlankImage();
    return;
  }

  // get published PImage    
  img = server.getPImage(names[idx]);
  if (img == null) {
    println("pimage is null...idx=" + idx + "name=" + names[idx]);
    drawBlankImage();
    return;
  }

  processTransition();

  // draw PImage
  image(img, 0, 0, width, height);
  g.removeCache(img); // issue 1391 : https://github.com/processing/processing/issues/1391

  // information
  if (transition_counter > 0) {
    textSize(32);
    text(names[idx], 10, height - 20);
  }
}

void startTransition() {
  transition_counter = transition_duration;
}

void processTransition() {
  // transition effect
  if (transition_counter == 0) {
    tint(255, 255, 255, 255);
  }
  else {
    int a = (int)(255 * (transition_duration - transition_counter) / (float)transition_duration);
    tint(255, 255, 255, a);
    transition_counter --;
  }
}

void drawDebugStatus() {
  tint(255, 255, 255, 255); 

  stroke(255, 255, 255);
  strokeWeight(2);
  fill(0, 0, 0, 128);

  rect(5, 5, 300, 160);

  fill(255, 255, 255, 255);
  textSize(16);
  text("debug information :", 10, 20);
  text("active_connection_num=" + server.getActiveConnectionNum(), 20, 40);
  text("average_fps=" + server.getAverageFps(), 20, 60);
  text("total_bps=" + server.getTotalBpsStr(), 20, 80);
  text("current_idx=" + draw_pimage_idx, 20, 100);

  String [] names = server.getActiveConnectionNames();
  if (0 <= draw_pimage_idx && draw_pimage_idx < names.length ) {
    text("current_name=" + names[draw_pimage_idx], 20, 120);
  }
}

void changeIdx(int idx) {
  int max_idx = server.getActiveConnectionNum();
  if (idx < 0 || max_idx <= idx) return;

  draw_pimage_idx = idx;
  startTransition();
}

void changeNext() {
  draw_pimage_idx ++;
  if (server.getActiveConnectionNum() <= draw_pimage_idx) {
    draw_pimage_idx = 0;
  }
  startTransition();
}

void changePrev() {
  draw_pimage_idx --;
  if (draw_pimage_idx < 0) {
    draw_pimage_idx = server.getActiveConnectionNum() - 1;
  }
  startTransition();
}



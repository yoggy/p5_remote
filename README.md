p5_remote
=========

p5_remote is simple remote display library for Processsing, like AirPlay, Miracast... 

How to use
=========

client

<pre>
  import p5_remote.*;
  
  Remote remote;
  
  public void setup() {
    size(320, 240);
    
    // setup Remote instance
    //
    //   remote = new Remote(PApplet a, String publisher_name, String host, int port);
    //
    remote = new Remote(this, "publisher_name", "192.168.1.123", 12345);
  }
  
  public void draw() {
    // draw something
        .
        .
        .
  
    // publish to server
    remote.publish();
  }
</pre>

server
<pre>
  import p5_remote.*;
  
  RemoteServer server;
  
  void setup() {
    size(640, 480);
    
    // start server
    server = new RemoteServer(this, 12345);
    server.start();
  }
  
  void draw() {
    img = server.getPImage("publisher_name");
    if (img != null) {
      image(img, 0, 0);
      g.removeCache(img); // issue 1391 : https://github.com/processing/processing/issues/1391
    }
  }
</pre>


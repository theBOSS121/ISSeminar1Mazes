package src.lukauranic.maze;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import src.lukauranic.maze.geneticAlgorithm.GeneticAlgorithm;
import src.lukauranic.maze.graphics.Screen;
import src.lukauranic.maze.input.Keyboard;
import src.lukauranic.maze.input.Mouse;
import src.lukauranic.maze.util.Vec2;

public class Maze extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;
	
	public static float scale = 1.0f;
	public Thread thread;
	public boolean running = false;
	public JFrame frame;
	public static Screen screen;
	public GeneticAlgorithm game;	
	public static Mouse mouse;
	public static Keyboard key;
	
	public BufferedImage image;
	public int[] pixels;
	
	public static int width = 0;
	public static int height = 0;
	
	public Maze(String[] mazeString) {
		height = mazeString.length;
		width = mazeString[0].length();
		scale = 500/width;
		int[] maze = new int[width * height];
		Vec2 start = null, end = null;
		Vec2[] tressures = null;
		int tressuresCount = 0;
		int maxChromosomeLength = width * height - 1; // moves is one less than visited points
		for(int y = 0; y < height; y++) {
			String mazeRow = mazeString[y];
			for(int x = 0; x < width; x++) {
				if(mazeRow.charAt(x) == '#') {
					maze[x + y * width] = -1;
					maxChromosomeLength--;
				}else if(mazeRow.charAt(x) == '.') {
					maze[x + y * width] = 0;
				}else if(mazeRow.charAt(x) == 'S') {
					maze[x + y * width] = 1;
					start = new Vec2(x, y);
				}else if(mazeRow.charAt(x) == 'E') {
					maze[x + y * width] = 2;
					end = new Vec2(x, y);
				}else if(mazeRow.charAt(x) == 'T') {
					maze[x + y * width] = 3;
					tressuresCount++;
				}
			}
		}
		if(tressuresCount > 0) {
			tressures = new Vec2[tressuresCount];
			int i = 0;
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					if(maze[x + y * width] == 3) {
						tressures[i] = new Vec2(x, y);
						i++;
					}
				}
			}
		}		
		
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		Dimension size = new Dimension((int) (width * scale),(int)  (height * scale));
		
		setPreferredSize(size);
		screen = new Screen(width, height, maze);
		frame = new JFrame();
		game = new GeneticAlgorithm(maze, start, end, tressures, width, height, maxChromosomeLength);
		mouse = new Mouse();
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		key = new Keyboard();
		addKeyListener(key);
	}
	
	public void start() {
		running = true;
		thread = new Thread(this, "loopThread");
		thread.start();
	}
	
	public void stop() {
		try {
			thread.join();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}	
	
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 100.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		requestFocus();
		while (running) {
			
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1){
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
				frame.setTitle("Maze | " + updates + " ups, " + frames +" fps");
				updates = 0;
				frames = 0;
			}
		}
		stop();
	}
	
	public void update() {
		game.update();
		mouse.update();
		key.update();
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		screen.clear();
		game.render();
		
		for(int i = 0; i < screen.pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}
		
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.drawImage(image, 0, 0, (int) (width * scale), (int) (height * scale), null);
//		g.setColor(new Color(0xff193441, true));
//		g.setFont(new Font("Arial", 1, 24));
//		String p1 = "" + Game.scorep1;
//		int p1w = g.getFontMetrics().stringWidth(p1) / 2;
//		String p2 = "" + Game.scorep2;
//		int p2w = g.getFontMetrics().stringWidth(p2) / 2;
//		g.drawString(p1, getWidth() / 9 - p1w, 50);
//		g.drawString(p2, getWidth() / 9 * 8 - p2w, 50);
		
		g.dispose();
		bs.show();
	}
	
	public static void main(String[] args) {
		String[] mazeString = {
				"##E##",
		          "#...#",
		          "#...#",
		          "#S..#",
		          "#####"
		};		
		Maze m = new Maze(mazeString);
		m.frame.setResizable(false);
		m.frame.setTitle("Maze");
		m.frame.add(m);
		m.frame.pack();
		m.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m.frame.setVisible(true);
		m.frame.setLocationRelativeTo(null);
		m.frame.setAlwaysOnTop(true);
		m.start();
	}
}

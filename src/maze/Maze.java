package maze;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import maze.geneticAlgorithm.Chromosome;
import maze.geneticAlgorithm.GeneticAlgorithm;
import maze.graphics.Screen;
import maze.util.Vec2;

public class Maze extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;
	
	public static float scale = 1.0f;
	public Thread thread;
	public boolean running = false;
	public JFrame frame;
	public static Screen screen;
	public GeneticAlgorithm game;	
	public BufferedImage image;
	public int[] pixels;
	
	public static int width = 0;
	public static int height = 0;
	
	private int bestFitness = Integer.MIN_VALUE;
	private int bestScoreGeneration = 0;
	
	long startTime = 0;
	long bestTime = 0;
	
	public Maze(String[] mazeString) {
		height = mazeString.length;
		width = mazeString[0].length();
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
			maxChromosomeLength = width * height - 1;
		}		

		scale = 800/width;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		Dimension size = new Dimension((int) (width * scale),(int)  (height * scale));
		
		setPreferredSize(size);
		screen = new Screen(width, height, maze);
		frame = new JFrame();
		game = new GeneticAlgorithm(maze, start, end, tressures, width, height, maxChromosomeLength);
		
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
		startTime = System.currentTimeMillis(); 
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 10.0;
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
		
		if(game.population.get(0).fitness > bestFitness) {
			bestFitness = game.population.get(0).fitness;
			bestScoreGeneration = Chromosome.generation;
			bestTime = System.currentTimeMillis() - startTime;
		}
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
		g.setColor(new Color(0xff999999, true));
		g.setFont(new Font("Arial", 1, 18));
		String str = "Generation:" + Chromosome.generation + " Best fitness generation: " + bestScoreGeneration + " Fitness: " + game.population.get(0).fitness + ", time: " + bestTime + "ms";
		
		int strw = g.getFontMetrics().stringWidth(str) / 2;
		g.drawString(str, getWidth() / 2 - strw, 30);

//		String movesStr = "" + game.population.get(0).getMovesString();
//		int movesStrw = g.getFontMetrics().stringWidth(movesStr) / 2;
//		g.drawString(movesStr, getWidth() / 2 - movesStrw, 60);
		
		g.dispose();
		bs.show();
	}
	
	public static void main(String[] args) {
		String[] mazeString = {
				"####################",
		          "#..................#",
		          "#.##############.###",
		          "#.########.......###",
		          "#.#.T......######..#",
		          "#.##.##.##........##",
		          "#.#...#.##.######.##",
		          "#.###.#.##....T##.##",
		          "#.###..##########.##",
		          "#.####.###.........#",
		          "#.#....#...####.####",
		          "#.#.####.####.....##",
		          "#.#......#......####",
		          "#.###.####.#####...#",
		          "#.#T.....#.....#.###",
		          "#.######...#####.#.#",
		          "#.#.....##S....#.#.#",
		          "#.#########..#...T.#",
		          "#..........###.##.##",
		          "##########E#########"
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

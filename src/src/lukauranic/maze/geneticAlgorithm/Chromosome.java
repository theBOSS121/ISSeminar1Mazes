package src.lukauranic.maze.geneticAlgorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import src.lukauranic.maze.Maze;
import src.lukauranic.maze.util.Vec2;

public class Chromosome implements Comparable<Chromosome>{
	public static int generation = 0;
	Random rand = new Random();	
	
	int color = 0xffff0000;
	
	Vec2 start, end;
	Vec2[] tressures;
	int width, height;
	int[] maze;
	int[] moves;
	Vec2[] path;
	int fitness = Integer.MIN_VALUE;
	int maxChromosomeLength;
	
	public Chromosome(int[] moves, int maxChromosomeLength, int[] maze, Vec2 start, Vec2 end, Vec2[] tressures, int width, int height) {
		this.maze = maze;
		this.width = width;
		this.height = height;
		this.start = start;
		this.end = end;
		this.tressures = tressures;
		this.maxChromosomeLength = maxChromosomeLength;
		this.moves = new int[moves.length];
		for(int i = 0; i < this.moves.length; i++) {
			this.moves[i] = moves[i];
		}
		
		int r = rand.nextInt(16);
		color = 0xff << 16 | r << 12 | r << 8;		
		calculateFitness();
	}
	
	public Chromosome(int maxChromosomeLength, int[] maze, Vec2 start, Vec2 end, Vec2[] tressures, int width, int height) {
		this.maze = maze;
		this.width = width;
		this.height = height;
		this.start = start;
		this.end = end;
		this.tressures = tressures;
		this.maxChromosomeLength = maxChromosomeLength;
		int n = maxChromosomeLength; //rand.nextInt(maxChromosomeLength)+1;
		moves = new int[n];
//		generateRandomPath();
		generateRandomPathAvoidWalls();
		
		int r = rand.nextInt(16);
		color = 0xff << 16 | r << 12 | r << 8;
		
		calculateFitness();
	}
	
	public void calculateFitness() {
		getPath();
		if(path == null) {
			System.out.println("Path is null");
			fitness = Integer.MIN_VALUE;
		}
		int lastX = path[path.length-1].x;
		int lastY = path[path.length-1].y;
		int distanceToEnd = Math.abs(lastX - end.x) + Math.abs(lastY - end.y);
		
		int numberOfTimesThroughWall = 0;
		int numberOfThressures = 0;
		List<Vec2> tressuresOnPath = new ArrayList<>();

		for(int i = 0; i < path.length; i++) {
			if(path[i].x < 0 || path[i].y < 0 || path[i].x >= width || path[i].y >= height || maze[path[i].x + path[i].y * width] == -1) { // wall or out of maze
				numberOfTimesThroughWall++;
				continue;
			}
			if(maze[path[i].x + path[i].y * width] == 3) { // tressure
				boolean already = false;
				for(int j = 0; j < tressuresOnPath.size(); j++) {
					if(path[i].x == tressuresOnPath.get(j).x && path[i].y == tressuresOnPath.get(j).y) {
						already = true;
					}
				}
				if(!already) {
					numberOfThressures++;
					tressuresOnPath.add(new Vec2(path[i].x, path[i].y));
				}
			}
		}
				
		
		int wallPenalty = maxChromosomeLength;
		int distanceToEndPenalty = maxChromosomeLength/4;
		int tressureReward = maxChromosomeLength * 2;
//		fitness = -distanceToEnd*distanceToEndPenalty - numberOfTimesThroughWall * wallPenalty - path.length;
		fitness = -distanceToEnd*distanceToEndPenalty - numberOfTimesThroughWall * wallPenalty - path.length + numberOfThressures * tressureReward;
	}
	

	public void mutateRandomly() { // mutates random index of a move to a new random move
		int index = rand.nextInt(moves.length);
		moves[index] = rand.nextInt(4);
		
		calculateFitness();
	}
	
	public void mutateAvoidWalls() { // mutates random index of a move to a new random move and corrects path afterwards
		if(moves.length == 0) return;
		int index = rand.nextInt(moves.length);
//		System.out.println(index);
//		printMoves();
		moves[index] = rand.nextInt(4);
//		printMoves();
		Vec2 curr = new Vec2(path[index].x, path[index].y);
		boolean moved = false;
		boolean skipBackMove = false;
		int backMoveSkipFirstTimeProb = 90;
		int backMoveSkipNotFirstTimeProb = 70;
		for(int i = index; i < moves.length; i++) {
			moved = false;
			skipBackMove = false;
			int dir = moves[i];
			if(dir == 0 && maze[curr.x-1 + curr.y * width] != -1) {
				if(i > 0 && moves[i-1] == 1) {
					if(rand.nextInt(100) < backMoveSkipFirstTimeProb) skipBackMove = true;
				}
				if(!skipBackMove) {
					moves[i] = dir;
					curr.x--;
					moved = true;					
				}
			}else if(dir == 1 && maze[curr.x+1 + curr.y * width] != -1) {
				if(i > 0 && moves[i-1] == 0) {
					if(rand.nextInt(100) < backMoveSkipFirstTimeProb) skipBackMove = true;
				}
				if(!skipBackMove) {
					moves[i] = dir;
					curr.x++;
					moved = true;
				}
			}else if(dir == 2 && maze[curr.x + (curr.y-1) * width] != -1) {
				if(i > 0 && moves[i-1] == 3) {
					if(rand.nextInt(100) < backMoveSkipFirstTimeProb) skipBackMove = true;
				}
				if(!skipBackMove) {
					moves[i] = dir;
					curr.y--;
					moved = true;
				}
			}else if(dir == 3 && maze[curr.x + (curr.y+1) * width] != -1) {
				if(i > 0 && moves[i-1] == 2) {
					if(rand.nextInt(100) < backMoveSkipFirstTimeProb) skipBackMove = true;
				}
				if(!skipBackMove) {
					moves[i] = dir;
					curr.y++;
					moved = true;
				}
			}
						
			while(!moved) {
				skipBackMove = false;
				dir = rand.nextInt(4);
				if(dir == 0 && maze[curr.x-1 + curr.y * width] != -1) {
					if(i > 0 && moves[i-1] == 1) {
						if(rand.nextInt(100) < backMoveSkipNotFirstTimeProb) skipBackMove = true;
					}
					if(!skipBackMove) {
						moves[i] = dir;
						curr.x--;
						break;
					}
				}else if(dir == 1 && maze[curr.x+1 + curr.y * width] != -1) {
					if(i > 0 && moves[i-1] == 0) {
						if(rand.nextInt(100) < backMoveSkipNotFirstTimeProb) skipBackMove = true;
					}
					if(!skipBackMove) {
						moves[i] = dir;
						curr.x++;
						break;
					}
				}else if(dir == 2 && maze[curr.x + (curr.y-1) * width] != -1) {
					if(i > 0 && moves[i-1] == 3) {
						if(rand.nextInt(100) < backMoveSkipNotFirstTimeProb) skipBackMove = true;
					}
					if(!skipBackMove) {
						moves[i] = dir;
						curr.y--;
						break;
					}
				}else if(dir == 3 && maze[curr.x + (curr.y+1) * width] != -1) {
					if(i > 0 && moves[i-1] == 2) {
						if(rand.nextInt(100) < backMoveSkipNotFirstTimeProb) skipBackMove = true;
					}
					if(!skipBackMove) {
						moves[i] = dir;
						curr.y++;
						break;
					}
				}
			}
			if(curr.x == end.x && curr.y == end.y) {
				int[] temp = moves;
				moves = new int[i+1];
				for(int j = 0; j <= i; j++) {
					moves[j] = temp[j];
				}
				break;
			}
		}
//		printMoves();
		calculateFitness();
	}
	
	public void generateRandomPathAvoidWalls() {
		Vec2 curr = new Vec2(start.x, start.y); 
		for(int i = 0; i < moves.length; i++) {
			while(true) {
				int dir = rand.nextInt(4);
				if(dir == 0 && maze[curr.x-1 + curr.y * width] != -1) {
					moves[i] = dir;
					curr.x--;
					break;
				}else if(dir == 1 && maze[curr.x+1 + curr.y * width] != -1) {
					moves[i] = dir;
					curr.x++;
					break;
				}else if(dir == 2 && maze[curr.x + (curr.y-1) * width] != -1) {
					moves[i] = dir;
					curr.y--;
					break;
				}else if(dir == 3 && maze[curr.x + (curr.y+1) * width] != -1) {
					moves[i] = dir;
					curr.y++;
					break;
				}
			}
			if(curr.x == end.x && curr.y == end.y) {
				int[] temp = moves;
				moves = new int[i+1];
				for(int j = 0; j <= i; j++) {
					moves[j] = temp[j];
				}
				break;
			}
		}
	}
	
	public void generateRandomPath() {
		for(int i = 0; i < moves.length; i++) {
			moves[i] = rand.nextInt(4);
		}
	}
	
	public void printMoves() {
		System.out.println("Moves: (" + moves.length + "/" + maxChromosomeLength + ")");
		for(int i = 0; i < moves.length; i++) {
			if(moves[i] == 0) System.out.print("L");
			else if(moves[i] == 1) System.out.print("R");
			else if(moves[i] == 2) System.out.print("U");
			else if(moves[i] == 3) System.out.print("D");
		}
		System.out.println();
		for(int i = 0; i < moves.length; i++) {
			System.out.print(moves[i]);
		}
		System.out.println();
	}
	public void printPath() {
		System.out.println("Path: ");
		for(int i = 0; i < path.length; i++) {
			System.out.println(path[i].x + " " + path[i].y);
		}
		System.out.println();
	}
	
	public void getPath() {
		int x = start.x;
		int y = start.y;
		path = new Vec2[moves.length+1];
		path[0] = start;
		for(int i = 0; i < moves.length; i++) {
			if(moves[i] == 0) {
				x--;
			}else if(moves[i] == 1) {
				x++;
			}else if(moves[i] == 2) {
				y--;
			}else if(moves[i] == 3) {
				y++;
			}
			path[i+1] = new Vec2(x, y);
			if(x >= 0 && x < width && y >= 0 && y < height&& maze[x + y * width] == 2 && i != moves.length-1) { // end
				// make moves after end irrelevant by cutting array
				int[] temp = moves;
				moves = new int[i+1];
				for(int j = 0; j <= i; j++) {
					moves[j] = temp[j];
				}
				getPath();
				break;
			}
		}
	}
	
	public void update() {}
	public void render() {
		Maze.screen.renderPath(path, color);
	}

	@Override
	public String toString() {
		return "fitness:" + this.fitness;
	}
	
	@Override
	public int compareTo(Chromosome o) {
		return o.fitness - this.fitness;
	}
}

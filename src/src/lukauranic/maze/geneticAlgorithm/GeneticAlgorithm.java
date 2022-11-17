package src.lukauranic.maze.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import src.lukauranic.maze.util.Vec2;

public class GeneticAlgorithm {
	private Random rand = new Random();	
	
	private Vec2 start, end;
	private Vec2[] tressures;
	private int[] maze;
	
	int width, height;
	
	
	List<Chromosome> population;
	Vec2[] selectionIndexes;
	int maxChromosomeLength;
	
	int mutationProbability = 30; // percents
	int elitism = 10; // percents (maybe also the best should be mutated?)
	
	public GeneticAlgorithm(int[] maze, Vec2 start, Vec2 end, Vec2[] tressures, int width, int height, int maxChromosomeLength) {
		this.maze = maze;
		this.start = start;
		this.end = end;
		this.tressures = tressures;
		this.width = width;
		this.height = height;
		this.maxChromosomeLength = maxChromosomeLength;
		System.out.println("Start position: " + start.x + " " + start.y);
		System.out.println("End position: " + end.x + " " + end.y);
		if(tressures != null) {
			System.out.println("Tressure positions: ");
			for(int i = 0; i < tressures.length; i++) {
				System.out.println(tressures[i].x + " " + tressures[i].y);
			}			
		}
		
		int populationSize = 10000;
		generatePopulation(populationSize, maxChromosomeLength);
	}
	
	public void sortPopulationByFitness() { 
		Collections.sort(population); 
//		for(int i = 0; i < population.size(); i++) {
//			System.out.println(population.get(i).fitness);
//		}
	}
	
	public void generatePopulation(int populationSize, int maxChromosomeLength) {
		population = new ArrayList<>();
		for(int i = 0; i < populationSize; i++) {
			population.add(new Chromosome(maxChromosomeLength, maze, start, end, tressures, width, height));
		}
	}
	
	public void selectionRandomly() {
		selectionIndexes = new Vec2[population.size()];
		
		int parent1 = 0, parent2 = 0;
		for(int i = 0; i < selectionIndexes.length; i++) {
			parent1 = parent2;
			while(parent1 == parent2) {
				parent1 = rand.nextInt(population.size());
				parent2 = rand.nextInt(population.size());
			}
			selectionIndexes[i] = new Vec2(parent1, parent2);				
		}
//		System.out.println("Indexes");
//		for(int i = 0; i < selectionIndexes.length; i++) {
//			System.out.println(selectionIndexes[i].x + ", " + selectionIndexes[i].y);
//		}
	}
	
	public void selectionLinearlyBiased() {
		if(population.size() < 3) {
			selectionRandomly();
			return;
		}
		int startIndex = population.size() * elitism / 100;
		selectionIndexes = new Vec2[population.size()-startIndex];		
		int parent1 = 0, parent2 = 0;
		int sumFrom0ToN = (population.size()) * (population.size()-1) / 2 - 1;
		for(int i = 0; i < selectionIndexes.length; i++) {
			parent1 = parent2;
			while(parent1 == parent2) {
				parent1 = population.size()-1 - (int) (1/2 + Math.sqrt(1 + 8 * rand.nextInt(sumFrom0ToN))/2);
				parent2 = population.size()-1 - (int) (1/2 + Math.sqrt(1 + 8 * rand.nextInt(sumFrom0ToN))/2);
			}
			selectionIndexes[i] = new Vec2(parent1, parent2);				
		}
//		System.out.println("Indexes: (" + population.size() + ")");
//		for(int i = 0; i < selectionIndexes.length; i++) {
//			System.out.println(selectionIndexes[i].x + ", " + selectionIndexes[i].y);
//		}
	}	
	
	public void crossover() {
//		System.out.println("Crossover");
		int startIndex = population.size() * elitism / 100;
//		System.out.println(selectionIndexes.length + " " + startIndex + ", " + population.size());
		List<Chromosome> children = new ArrayList<>();
		for(int i = 0; i < startIndex; i++) {
			children.add(population.get(i));			
		}
		for(int i = 0; i < selectionIndexes.length; i++) {
			Chromosome parent1 = population.get(selectionIndexes[i].x);
			Chromosome parent2 = population.get(selectionIndexes[i].y);
			
			List<Vec2> intersections = new ArrayList<Vec2>();
			for(int j = 0; j < parent1.path.length; j++) {
				for(int k = 0; k < parent2.path.length; k++) {
					if(parent1.path[j].eq(parent2.path[k])) intersections.add(new Vec2(j, k));
				}
			}
			int[] moves = null;
			int randIntersection = rand.nextInt(intersections.size());
			int part1 = rand.nextInt(2);
			int part2 = rand.nextInt(2);
			if(part1 == 0 && part2 == 0) {
				moves = parent1.moves;
			}else if(part1 == 1 && part2 == 1) {
				moves = parent2.moves;
			}else if(part1 == 0 && part2 == 1) { // posibility of to big length
				int length = intersections.get(randIntersection).x + parent2.moves.length-intersections.get(randIntersection).y;
				length = Math.min(maxChromosomeLength, length);
				moves = new int[length];
				for(int a = 0; a < moves.length; a++) {
					if(a < intersections.get(randIntersection).x) {
						moves[a] = parent1.moves[a];						
					}else {
						int index =intersections.get(randIntersection).y + a - intersections.get(randIntersection).x;
						moves[a] = parent2.moves[index];
					}
				}
			}else if(part1 == 1 && part2 == 0) {
				int length = intersections.get(randIntersection).y + parent1.moves.length-intersections.get(randIntersection).x;
				length = Math.min(maxChromosomeLength, length);
				moves = new int[length];
				for(int a = 0; a < moves.length; a++) {
					if(a < intersections.get(randIntersection).y) {
						moves[a] = parent2.moves[a];
					}else {
						int index = intersections.get(randIntersection).x + a - intersections.get(randIntersection).y;
						moves[a] = parent1.moves[index];
					}
				}
			}
			children.add(new Chromosome(moves, maxChromosomeLength, maze, start, end, tressures, width, height));
			
//			parent1.printMoves();
//			parent2.printMoves();
//			System.out.println("child: " + intersections.get(randIntersection).x + ", " + intersections.get(randIntersection).y + ", " + part1 + ", " + part2);
//			children.get(children.size()-1).printMoves();
//			for(int c = 0; c < children.size(); c++) {
//				children.get(c).printMoves();
//			}
		}
//		System.out.println("new populationm " + children.size() + ", " + population.size() );
		population = children;
	}
	
	public void mutate() {
		int startIndex = population.size() * elitism / 100;
		for(int i = startIndex; i < population.size(); i++) {
			if(rand.nextInt(100) < mutationProbability) { //mutate
//				population.get(i).mutateRandomly();
				population.get(i).mutateAvoidWalls();
			}
		}
	}
	
	public void update() {
		sortPopulationByFitness();
//		selectionRandomly();
		selectionLinearlyBiased();
//		crossover();
		mutate();

//		System.out.println("Generation: " + Chromosome.generation + ", " + population.get(0).toString());
//		population.get(0).printMoves();
		Chromosome.generation++;
	}

	public void render() {
		for(int i = 0; i < 1; i++) {
//			System.out.println(population.get(i).fitness);
//			population.get(i).render();
		}
		for(int i = 0; i < population.size(); i++) {
			population.get(i).render();
		}
	}
}

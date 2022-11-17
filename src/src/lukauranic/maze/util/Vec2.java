package src.lukauranic.maze.util;

public class Vec2 {
	public int x, y;
	
	public Vec2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean eq(Vec2 v) {
		return this.x == v.x && this.y == v.y;
	}
}

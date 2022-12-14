package maze.util;

public class Vec2 {
	public int x, y;
	
	public Vec2(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Vec2)) return false;
        Vec2 v = (Vec2) o;
        return v.x == this.x && v.y == this.y; 
	}
}

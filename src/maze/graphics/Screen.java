package maze.graphics;

import maze.util.Vec2;

public class Screen {

	private int width, height;
	public int[] pixels;
	
	public int offX = 0, offY = 0;
	
	int[] maze;
	
	public Screen(int width, int height, int[] maze) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
		this.maze = maze;
	}

	public void clear() {
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(maze[x + y * width] == -1) {
					pixels[x + y * width] = 0;
				}else if(maze[x + y * width] == 0){
					pixels[x + y * width] = 0xffffffff;					
				}else if(maze[x + y * width] == 1) {
					pixels[x + y * width] = 0xff00ff00;
				}else if(maze[x + y * width] == 2) {
					pixels[x + y * width] = 0xff0000ff;
				}else if(maze[x + y * width] == 3) {
					pixels[x + y * width] = 0xffffff00;
				}
			}
		}
	}
	
	public void renderPath(Vec2[] path, int color) {
		for(int i = 0; i < path.length; i++) {
			if(path[i].x >= 0 && path[i].x < width && path[i].y >= 0 && path[i].y < height) {
				if(i == path.length-1) {
					pixels[path[i].x + path[i].y * width] = 0xff00ffff;
				}else if(i == 0 || maze[path[i].x + path[i].y * width] == 1) {
					pixels[path[i].x + path[i].y * width] = 0xff00ff00;
				}else if(maze[path[i].x + path[i].y * width] == 3) {
					pixels[path[i].x + path[i].y * width] = 0xffff00ff;
				}else {
					pixels[path[i].x + path[i].y * width] = color;//combineColor(0x77ff0000, path[i].x, path[i].y);					
				}
			}
		}
	}
}

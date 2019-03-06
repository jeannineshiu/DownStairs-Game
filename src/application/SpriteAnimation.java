package application;

import javafx.animation.Transition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

class SpriteAnimation extends Transition{
	private ImageView imageview;
	private int count;
	private int colums;
	private int offsetX;
	private int offsetY;
	private int width;
	private int height;
	
	public SpriteAnimation(ImageView imageview,
						   Duration duration,
						   int count,int columns,
						   int offsetX, int offsetY,
						   int width,int height) {
		
		this.imageview=imageview;
		this.count=count;
		this.colums=columns;
		this.offsetX=offsetX;
		this.offsetY=offsetY;
		this.width=width;
		this.height=height;
		setCycleDuration(duration);
	}
	
	//make each small image be rectangle size
	protected void interpolate(double k) {
		int index=Math.min((int)Math.floor(k*count), count-1);
		int x=(index%colums)*width+offsetX;
		int y=(index/colums)*height+offsetY;
		imageview.setViewport(new Rectangle2D(x, y, width, height));
	}
}

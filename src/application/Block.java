package application;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Block extends Group{
	int type,ID;
	double positionX,positionY;
	ImageView block;
	ImageView blocksolid;
	Image img_blocksolid =new Image(getClass().getResourceAsStream("blocksolid.png"));
	public Block(int ID,Image block,int type,double positionX,double positionY){
		this.ID=ID;
		this.type=type;
		this.positionX=positionX;
		this.positionY=positionY;
		this.block= new ImageView(block);
		this.block.setTranslateX(positionX);
		this.block.setTranslateY(positionY);
		this.blocksolid = new ImageView(img_blocksolid);
	}
}

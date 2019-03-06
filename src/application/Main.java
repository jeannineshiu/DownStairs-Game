package application;

import java.util.ArrayList;


import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
		
	Stage stage;
	Pane root;	
	Timeline gameLoop;	
	ImageView player;
	ImageView playersolid;
	ImageView ceiling;	
	Text Floor,HP;		
	MediaPlayer screaming,BGM;

	Image img_player = new Image(getClass().getResourceAsStream("player.png")); 
	Image img_ceiling = new Image(getClass().getResourceAsStream("ceiling.png")); 
	Image img_playersolid = new Image(getClass().getResourceAsStream("playersolid.png")); 
	Image nor_block = new Image(getClass().getResourceAsStream("norblock.png"));
	Image nail_block = new Image(getClass().getResourceAsStream("nailblock.png"));
	Media media_screaming = new Media(getClass().getResource("Gameover.mp3").toString()); 
	Media media_BGM = new Media(getClass().getResource("BGM3.mp3").toString()); 
	
	Animation animationLeft,animationLeft_hurt;
	Animation animationRight,animationRight_hurt;
	Animation animationAirLeft,animationAirLeft_hurt;
	Animation animationAirRight,animationAirRight_hurt;
	Animation animationStop,animationStop_hurt;
	Animation animationAir,animationAir_hurt;
	
	int Timecount,Score,blockID,HeartPoint=10,Pre_block=-1,Now_block=-1,ceiling_check=-1;
	double FPS=150,HurtTime;
	boolean GameOver=false,stop=false,hurt=false,nail=false;
	private final double width=300,height=500; // set screen size
	final double angleSpeed = 200 ;  //move speed
	final double minX = 0 , maxX = width-32 ; // move range
	
	ArrayList<Block> listOfBlocks = new ArrayList<>();	
			
    public void start(Stage primaryStage) {
    	stage = primaryStage;    	
    	root = new Pane();
    	//add ceiling floor and hp
    	ceiling = new ImageView(img_ceiling);
    	Floor = new Text("B");
    	HP = new Text("HP:"+HeartPoint);

    	root.setStyle("-fx-background-color:#000000");
    	
    	player = new ImageView(img_player);     
    	playersolid = new ImageView(img_playersolid);
    	Scene scene = new Scene(root,width,height); 
		primaryStage.setScene(scene);
		primaryStage.setTitle("小朋友下樓梯");
		primaryStage.show();				 
		
		//player animation
		animationLeft = new SpriteAnimation(player, Duration.millis(500),4,4,0,0,32,32);
		animationRight = new SpriteAnimation(player, Duration.millis(500),4,4,0,32,32,32);
		animationAirLeft = new SpriteAnimation(player, Duration.millis(500),4,4,0,32*2,32,32);
		animationAirRight = new SpriteAnimation(player, Duration.millis(500),4,4,0,32*3,32,32);
		animationStop = new SpriteAnimation(player, Duration.millis(500),1,1,32*8,0,32,32);
		animationAir = new SpriteAnimation(player, Duration.millis(500),4,4,0,32*4,32,32);
		
		animationLeft_hurt = new SpriteAnimation(player, Duration.millis(500),4,4,32*4,0,32,32);
		animationRight_hurt = new SpriteAnimation(player, Duration.millis(500),4,4,32*4,32,32,32);
		animationAirLeft_hurt = new SpriteAnimation(player, Duration.millis(500),4,4,32*4,32*2,32,32);
		animationAirRight_hurt = new SpriteAnimation(player, Duration.millis(500),4,4,32*4,32*3,32,32);
		animationStop_hurt = new SpriteAnimation(player, Duration.millis(500),1,1,32*8,32,32,32);
		animationAir_hurt = new SpriteAnimation(player, Duration.millis(500),4,4,32*4,32*4,32,32);		
		
		animationStop.setCycleCount(-1); 
		animationLeft.setCycleCount(-1);
		animationRight.setCycleCount(-1);
		animationAirLeft.setCycleCount(-1);
		animationAirRight.setCycleCount(-1);
		animationAir.setCycleCount(-1);
		
		
		animationStop_hurt.setCycleCount(-1);
		animationLeft_hurt.setCycleCount(-1);
		animationRight_hurt.setCycleCount(-1);
		animationAirLeft_hurt.setCycleCount(-1);
		animationAirRight_hurt.setCycleCount(-1);
		animationAir_hurt.setCycleCount(-1);
		
		animationAir.play();
		
		//Media
		screaming = new MediaPlayer(media_screaming);
		BGM  =new MediaPlayer(media_BGM);
		
		
				
		
		//Move smooth
		final DoubleProperty angleVelocity = new SimpleDoubleProperty();
		final LongProperty lastUpdateTime = new SimpleLongProperty();
		final AnimationTimer angleAnimation = new AnimationTimer() {
		  @Override
		  public void handle(long timestamp) { 
			  
		    if (lastUpdateTime.get() > 0) {
		      final double elapsedSeconds = (timestamp - lastUpdateTime.get()) / 1_000_000_000.0 ;
		      final double deltaX = elapsedSeconds * angleVelocity.get();
		      final double oldX = player.getTranslateX();
		      final double newX = Math.max(minX, Math.min(maxX, oldX + deltaX));
		      player.setTranslateX(newX); 
		    }   
		    lastUpdateTime.set(timestamp);			    
		  }
		};
		angleAnimation.start();

		
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
		  @Override
		  public void handle(KeyEvent event) {
			 //need to change the animation while different situation
			  
			if(!GameOver){
				
			
		    if (event.getCode()==KeyCode.RIGHT) { 
		    	
		    	stop = false;
		    	animationAir.pause();
		    	animationStop.pause();
		    	animationAir_hurt.pause();
		    	animationStop_hurt.pause();
		    	//two situations
		    	if(checkCollisions()){
		    		if(hurt)
		    			animationRight_hurt.play();
		    		else
		    			animationRight.play();
		    	}else{
		    		if(hurt)
		    			animationAirRight_hurt.play();
		    		else
		    			animationAirRight.play();
		    	}
		    	
		    	angleVelocity.set(angleSpeed);
		      
		    } else if (event.getCode() == KeyCode.LEFT) {		    
		    	
		    	stop = false;
		    	animationAir.pause();
		    	animationStop.pause();
		    	animationAir_hurt.pause();
		    	animationStop_hurt.pause();
		    	if(checkCollisions()){
		    		if(hurt)
		    			animationLeft_hurt.play();
		    		else
		    			animationLeft.play();
		    	}else{
		    		if(hurt)
		    			animationAirLeft_hurt.play();
		    		else
		    			animationAirLeft.play();
		    	}
		    	
		    	angleVelocity.set(-angleSpeed);
		      
	        }
		  }else{
			  if  (event.getCode() == KeyCode.ENTER) {
					if(GameOver)
						initializeGame();
	       	
		        }
		  }
		  }
		});

		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
		  @Override
		  public void handle(KeyEvent event) {
		    if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT) {
		      angleVelocity.set(0);
		      animationLeft.pause();
		      animationRight.pause();
		      animationAirLeft.pause();
		      animationAirRight.pause();
		      animationLeft_hurt.pause();
		      animationRight_hurt.pause();
		      animationAirLeft_hurt.pause();
		      animationAirRight_hurt.pause();
		      stop = true;
		    }
		  }
		});
							
    	Game() ;
    }
     
    void Game() {   	
    	//for updating my window
    	gameLoop = new Timeline(new KeyFrame(Duration.millis(1000/FPS),new EventHandler<ActionEvent>(){
    		
    		public void handle(ActionEvent e){
    			
    			playersolid.setTranslateX(player.getTranslateX());
    			playersolid.setTranslateY(player.getTranslateY()+31);
    			
    			//player.setTranslateY(player.getTranslateY()+1);
    			
    			if(checkCollisions()){
    				player.setTranslateY(player.getTranslateY()-1);
    				
    				if(Now_block!=Pre_block)
    				{
    					if(nail)
    					{
    						HeartPoint -= 3;
    						hurt = true;
    					}
    					else if(HeartPoint<10)
    						HeartPoint++;
    				}
    				
    				Pre_block = Now_block;
    				
    				//ceiling collisions
    				if((player.getBoundsInParent().intersects(ceiling.getBoundsInParent()))&&ceiling_check!=Now_block){
    					HeartPoint -= 3;
						hurt = true;
						ceiling_check = Now_block;
    				}
    				
    			}else
    				player.setTranslateY(player.getTranslateY()+1);
    			
    			
    			
    			for(int i=0;i<listOfBlocks.size();i++)
    			{
    				listOfBlocks.get(i).positionY-=1;
    				
    				listOfBlocks.get(i).block.setTranslateY(listOfBlocks.get(i).positionY);
    				
    				listOfBlocks.get(i).blocksolid.setTranslateX(listOfBlocks.get(i).positionX);
    				if(listOfBlocks.get(i).type==1)
    					listOfBlocks.get(i).blocksolid.setTranslateY(listOfBlocks.get(i).positionY);
    				else if(listOfBlocks.get(i).type==2)
    					listOfBlocks.get(i).blocksolid.setTranslateY(listOfBlocks.get(i).positionY+15);
    								
    				if(listOfBlocks.get(i).positionY<=0){
    					listOfBlocks.remove(i);
    					root.getChildren().remove(5);
    					root.getChildren().remove(5);
    				}
    			}
    			
    			
    			if(Timecount%(FPS)==0)
    			{
    				switch((int)(Math.random()*3+1))
    				{
    				case 1:
    					break;
    				case 2:
    					addnorblock();
    					break;
    				case 3:
    					addnailblock();
    					break;
    				}
    				
    				Score++;
    				Floor.setText("B"+Score);
    			}
    			
    			if(HeartPoint>0)
    			{
    				HP.setText("HP:"+HeartPoint);
    			}
    			else
    				HP.setText("HP:0");
    			
    			Timecount++;
    			
    		
    		
    		//////////////////////
    		//when key released => stop = true
    		if(stop)
        	{
    			//on plane
        		if(checkCollisions())
        		{
        			if(hurt)
        			{
        				animationAir_hurt.pause();
        				animationStop_hurt.play();
        			}else
        			{
        				animationAir.pause();
        				animationStop.play();
        			}
        		}
        		//in the air
        		else
        		{
        			if(hurt)
        			{
        				animationStop_hurt.pause();    				
        				animationAir_hurt.play();
        			}else
        			{
        				animationStop.pause();
        				animationAir.play();
        			}
        			
        		}
        		
        	}
        	
        	//when got hurt, animation remains for 2 seconds
        	if(HurtTime==0){
        		if(hurt)
        			HurtTime = FPS*2;
        	}else if(HurtTime==1)
        	{
        		hurt = false;
        		HurtTime--;
        	}else
        		HurtTime--;
        	
        	//GameOver
        	if((playersolid.getTranslateY()>=height+32)||HeartPoint<=0)
        	{
        		screaming.play();
        		Text gameOverLabel = new Text();
        		gameOverLabel.setFill(Color.RED);
        		gameOverLabel.setFont(Font.font(null,FontWeight.BOLD,28));
        		gameOverLabel.setTranslateX(60);
        		gameOverLabel.setTranslateY(height/2);
        		gameOverLabel.setText("Enter 重新開始");
        		root.getChildren().add(gameOverLabel);
        		
        		GameOver=true;
        		BGM.stop();
        		gameLoop.stop();
        	}
        	
    		}

    	}));
    	
    	gameLoop.setCycleCount(-1);
    	
    	initializeGame();
    }
    
    protected void addnailblock() {
    	blockID++;
		Block block = new Block(blockID,nail_block,2,(Math.random()*(width-97))+1,height);
		listOfBlocks.add(block);
		
		root.getChildren().add(block.block);
		root.getChildren().add(block.blocksolid);
		
	}

	protected void addnorblock() {
		blockID++;
		Block block = new Block(blockID,nor_block,1,(Math.random()*(width-97))+1,height);
		listOfBlocks.add(block);
		
		root.getChildren().add(block.block);
		root.getChildren().add(block.blocksolid);
	}
	
	boolean checkCollisions(){
		
		for(int i=0;i<listOfBlocks.size();i++)
		{
			if(playersolid.getBoundsInParent().intersects(listOfBlocks.get(i).blocksolid.getBoundsInParent())){
				if(listOfBlocks.get(i).type==2)
					nail = true;
				else
					nail = false;
				Now_block =listOfBlocks.get(i).ID;
				
				return true;
			}
			
			
		}
		return false;
		
	}

	void initializeGame() {
    	root.getChildren().clear(); 
    	
    	player.setTranslateX(width/2);
    	player.setTranslateY(40);
    	Floor.setFill(Color.RED);
    	Floor.setFont(Font.font(null,FontWeight.BOLD,28));
    	Floor.setTranslateX(width-60);
    	Floor.setTranslateY(40);
    	HP.setFill(Color.RED);
    	HP.setFont(Font.font(null,FontWeight.BOLD,28));
    	HP.setTranslateX(5);
    	HP.setTranslateY(40);
    	
    	root.getChildren().add(player);
    	root.getChildren().add(Floor);
    	root.getChildren().add(HP);
    	root.getChildren().add(ceiling);
    	root.getChildren().add(playersolid);
    	
    	Timecount = 0;
    	Score = 0;
    	HeartPoint = 10;
    	
    	listOfBlocks.clear();
    	blockID=0;
    	
    	Pre_block = -1;
    	Now_block=-1;
    	
    	GameOver=false;
    	
    	screaming.stop();
    	BGM.setCycleCount(MediaPlayer.INDEFINITE);
    	BGM.play();
    	
    	
    	gameLoop.play();
    	
    
    }      
    
    
	public static void main(String[] args) {
        launch(args);
    }
    
 
   
}
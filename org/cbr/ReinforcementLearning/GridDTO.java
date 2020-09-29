package org.cbr.ReinforcementLearning;

import java.util.Random;

public class GridDTO {
	
	private GridCellDTO[] grid = null;
	private int startCellIndex = -1;
	private int stopCellIndex = -1;
	private int currentCellIndex = -1;
	private RLEnums.DIRECTION lastMovementDirection =  null;
	//
    double gamma = (double)0.99;
    double alpha = (double)0.7;
	
	public GridDTO()
	{
		
	}
	//-------------------------------------------------------------------
	public void makeGrid(int width , int heigth )
	{
		grid = new GridCellDTO[ width * heigth ];
		for(int i=0;i<grid.length;i++)
		{
			grid[i] = new GridCellDTO(i);
		}
		// boundaries
		for(int i=0;i<grid.length;i++)
		{
			GridCellDTO x = grid[i];
			int c = -1;
			// up
			c = i - width; if( c < 0 ) c = -1;
			x.setNeighbourCell( RLEnums.DIRECTION.UP , c);
			// right
			c = (i % width) + 1;  if ( c >= width ) c = -1;  else c = i + 1;
			x.setNeighbourCell( RLEnums.DIRECTION.RIGHT , c);
			// down
			c = i + width; if ( c >= grid.length ) c = -1; 
			x.setNeighbourCell( RLEnums.DIRECTION.DOWN , c); 
		   	// left
			c = (i % width) - 1; if ( c < 0 ) c =-1; else c = i - 1;
			x.setNeighbourCell( RLEnums.DIRECTION.LEFT , c);
			
			x.setReward( -1 );
		}
	}
   
	//-------------------------------------------------------------------
	public GridCellDTO getCell( int idx )
	{
	    if( (idx < 0) || (idx >= grid.length) ) return null;
	    return grid[ idx ];
	}
	
	//-------------------------------------------------------------------
	public void setStartCellIndex(int idx) {
		this.startCellIndex = idx;
	}
	
	public void setStopCellIndex(int idx) {
		this.stopCellIndex = idx;
		try {
			grid[stopCellIndex].setReward( (double)10 );
		}
		catch(Exception e ) {
			System.err.println("Cannoyt set reward on stop");
			System.exit(1);
		}
	}
	public void setCurrentCellIndex(int idx) {
		this.currentCellIndex = idx;
	}
    //-------------------------------------------------------------------
	public GridCellDTO getStartCell() {
		try {
			return grid[ startCellIndex ];
		}
		catch(Exception e) {
			return null;
		}
	}
	//-------------------------------------------------------------------
	public GridCellDTO getStopCell() {
		try {
			return grid[ stopCellIndex ];
		}
		catch(Exception e) {
			return null;
		}
	}
	//-------------------------------------------------------------------
	public GridCellDTO getCurrentCell() {
		try {
			return grid[ currentCellIndex ];
		}
		catch(Exception e) {
			return null;
		}
	}
	//-------------------------------------------------------------------
	public RLEnums.DIRECTION getRandomDirection()
	{
		Random rn = new Random(System.nanoTime());
		int answer = rn.nextInt(10000);
		if( answer < 2500 ) return RLEnums.DIRECTION.UP;
		if( answer < 5000 ) return RLEnums.DIRECTION.RIGHT;
		if( answer < 7500 ) return RLEnums.DIRECTION.DOWN;
		if( answer < 10000 ) return RLEnums.DIRECTION.LEFT;
		return null;
	}
	
	private GridCellDTO selectNextAction( boolean israndom )
	{
		lastMovementDirection = null;
	    GridCellDTO current = getCurrentCell();
	    if( current == null )  {
	    	System.err.println("no current"); System.exit(1);
	    }
	    RLEnums.DIRECTION dir = null;
	    if( israndom ) {
	      dir = this.getRandomDirection();
	    }
	    else {
	      dir = current.getMaxQSAAction();
	    }
	    if( dir == null ) {
	    	System.err.println("no direction found [" + israndom + "]"); System.exit(1);
	    }
	    lastMovementDirection = dir;
		//
	    int targetIndex = current.getNeighbourIndex( dir );
	    if( targetIndex < 0 ) { // bounced off
	       return current;
	    }
	    if( targetIndex >= grid.length )  {
	    	System.err.println("target overflow"); System.exit(1);
	    }
	    GridCellDTO target = grid[ targetIndex ];
	    return target;
	}
	
	// epsilon 1 = random (explore)    closer to 0 = exploit
	//-------------------------------------------------------------------
	public int performGridRun( int maxMoves , double epsilon)
	{
		if( (epsilon < 0) || (epsilon > 1) ) {
			System.err.println( "Epsilon must be between 0 an 1");
			System.exit(1);
		}
		GridCellDTO startCell = this.getStartCell();
    	if( startCell == null ) {
			System.err.println("cannot get start cell");
			System.exit(1);
		}
    	GridCellDTO stopCell = this.getStopCell();
    	if( stopCell == null ) {
			System.err.println("cannot get stop cell");
			System.exit(1);
		}
    	this.setCurrentCellIndex( startCell.getIndex() );
    	for(int i=0;i<maxMoves;i++)
    	{
        	String scomment = "";
    		GridCellDTO currentCell = this.getCurrentCell();
    		if( currentCell ==  null ) return -1;
    		
    		
    		// Exploite or Explore
    		boolean explore = true;
    		Random rn = new Random(System.nanoTime());
    		int prob = rn.nextInt(1000);
    		int milepsilon = (int)((double)1000 * epsilon);
    		if( prob > milepsilon )  explore = false;
            //
    		GridCellDTO targetCell = null;
    		targetCell = selectNextAction( explore );
            
    		
    		// No target
    		if( targetCell == null ) {
    			System.err.println("Could not find a target cell");
    			return -1;
    		}
    	    // bounce   		
    		if( currentCell.getIndex() == targetCell.getIndex() ) {
    			scomment = "BOUNCE";
    		}
    		// calculate q values QSA
    		// q(s,a) = (1 - alpha) q(s,a) + alpha ( Reward(t+1) + gamma * Max q(s',a) )
    		double maxQSAnext = targetCell.getMaxQSA();
    		double oldqsa = currentCell.getQSA( lastMovementDirection );
    		double reward = targetCell.getReward();
    		double newqsa = (( 1 - alpha) * oldqsa) + alpha * ( reward  + (gamma *maxQSAnext) ); 
    		currentCell.setQSA( lastMovementDirection , newqsa );
    		//
    		//System.out.println( "[" + i + "] " + lastMovementDirection + " [" + currentCell.getIndex() + " -> " + targetCell.getIndex() + "] [" + oldqsa + "] [" + newqsa + "] " + scomment);
    		
    		// end
    		if( targetCell.getIndex() == this.getStopCell().getIndex() ) {
    			System.out.println("Run completed in [" + (i+1) + "] actions");
    			return i+1;
    		}
    		// move
    		this.setCurrentCellIndex( targetCell.getIndex() );
    	}
    	System.out.println("Not completed in [" + maxMoves + "]");
    	return maxMoves + 100;
	}
	
	//-------------------------------------------------------------------
	private String cf(double dd)
	{
		String ss = "";
		if( dd == 0 ) ss = " ";
		else {
		 ss += "" + String.format("%6.4f", dd) + "";
		}
		ss = String.format("%8s" , ss );
		return ss;
	}
	
	//-------------------------------------------------------------------
	public void show( int w )
	{
		String s1 = "";
		String s2 = "";
		String s3 = "";
		
		for(int i=0;i<grid.length;i++)
		{
			int x = i % w;
			int y = i / w;

	        //
			GridCellDTO cc = grid[ i ];
			double[] qsa = cc.getQSA();
		    s1 += "[" + cf(0) + cf(qsa[0]) + cf(0) + "]  ";
		    s2 += "[" + cf(qsa[3]) + cf(0) + cf(qsa[1]) + "]  ";
		    s3 += "[" + cf(0) + cf(qsa[2]) + cf(0) + "]  ";
	 
		    
			if( ((i % w) + 1) == w ) {
				System.out.println( s1 );
				System.out.println( s2 );
				System.out.println( s3 );
				System.out.println( "");		
				s1 = s2 = s3 = "";
			}
		}
		
		
	}
}

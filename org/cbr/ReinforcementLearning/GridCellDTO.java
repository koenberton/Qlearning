package org.cbr.ReinforcementLearning;

public class GridCellDTO {
	
	private int idx = -1;
	private boolean isActive = true;
    private int[] neighbourCells = null;   // UP - RIGHT - DOWN - LEFT
	private double reward = 0;
	private double[] qsa = null;
    
    //------------------------------------------------------------------- 
    public GridCellDTO(int index)
    {
    	idx=index;
    	neighbourCells = new int[4];
    	for(int i=0;i<neighbourCells.length;i++) neighbourCells[i] = -1;
    	//
    	reward = (double)0;
    	//
    	qsa = new double[4];
		for(int i=0;i<qsa.length;i++) qsa[i] = 0;
    }
   

	public double getReward() {
		return reward;
	}
	public void setReward(double reward) {
		this.reward = reward;
	}
	public double[] getQSA() {
		return qsa;
	}
	public void setQSA(double[] qvalues) {
		this.qsa = qvalues;
	}

    

    //------------------------------------------------------------------- 
    public double getMaxQSA()
    {
    	double statevalue = qsa[0];
		for(int i=1;i<qsa.length;i++) 
		{
		  if( qsa[i] > statevalue ) statevalue = qsa[i];  	
		}
    	return statevalue;
    }
    
    //------------------------------------------------------------------- 
    public RLEnums.DIRECTION getMaxQSAAction()
    {
    	double statevalue = qsa[0];
    	int statemax = 0;
		for(int i=1;i<qsa.length;i++) 
		{
		  if( qsa[i] > statevalue ) {
			  statevalue = qsa[i];
			  statemax = i;
		  }
		}
		switch( statemax )
		{
		case RLEnums.UP    : return RLEnums.DIRECTION.UP;
		case RLEnums.RIGHT : return  RLEnums.DIRECTION.RIGHT;
		case RLEnums.DOWN  : return RLEnums.DIRECTION.DOWN;
		case RLEnums.LEFT  : return  RLEnums.DIRECTION.LEFT;
		default : { System.err.println("Unsupported direction Index " + statemax ); System.exit(1);  }
		}
    	return null;
    }
    
    //------------------------------------------------------------------- 
    public int getIndex()
    {
        return idx;	
    }
    //-------------------------------------------------------------------
    public void setNeighbourCell( RLEnums.DIRECTION dir , int val)
    {
    	switch( dir )
    	{
    	case UP    :  { neighbourCells[0] = val; break; }
    	case RIGHT :  { neighbourCells[1] = val; break; }
    	case DOWN  :  { neighbourCells[2] = val; break; }
    	case LEFT  :  { neighbourCells[3] = val; break; }
    	default : { System.err.println("cannot perform setNeigbour"); System.exit(1); }
    	}
    }
    //-------------------------------------------------------------------
    public int getNeighbourIndex( RLEnums.DIRECTION dir )
    {
    	try {
    	 switch( dir )
    	 {
    	 case UP    :  return neighbourCells[0]; 
    	 case RIGHT :  return neighbourCells[1]; 
    	 case DOWN  :  return neighbourCells[2]; 
    	 case LEFT  :  return neighbourCells[3]; 
    	 default : { System.err.println("Unsupported direction"); System.exit(1); return -99; }
    	 }
    	}
    	catch ( Exception e ) {
    		System.err.println("getNeigbourIndex [" + dir + "]");
    		return -1;
    	}
    }
    //-------------------------------------------------------------------
    public int getNeighbourIndex( int idir )
    {
    		switch( idir )
    		{
    		case RLEnums.UP : return getNeighbourIndex( RLEnums.DIRECTION.UP );
    		case RLEnums.RIGHT : return getNeighbourIndex( RLEnums.DIRECTION.RIGHT );
    		case RLEnums.DOWN : return getNeighbourIndex( RLEnums.DIRECTION.DOWN );
    		case RLEnums.LEFT : return getNeighbourIndex( RLEnums.DIRECTION.LEFT );
    		default : { System.err.println("Unsupported direction Index " + idir); System.exit(1); return -99; }
    		}
    }
    //-------------------------------------------------------------------
    public double getQSA( RLEnums.DIRECTION dir )
    {
    	try {
       	 switch( dir )
       	 {
       	 case UP    :  return qsa[0]; 
       	 case RIGHT :  return qsa[1]; 
       	 case DOWN  :  return qsa[2]; 
       	 case LEFT  :  return qsa[3]; 
       	 default : { System.err.println("Unsupported direction QSA"); System.exit(1); return -99; }
       	 }
       	}
       	catch ( Exception e ) {
       		System.err.println("getQSA [" + dir + "]");
       		return -1;
       	}
    }
    //-------------------------------------------------------------------
    public void setQSA( RLEnums.DIRECTION dir , double val)
    {
    	try {
       	 switch( dir )
       	 {
       	 case UP    :  { qsa[0] = val; break; } 
       	 case RIGHT :  { qsa[1] = val; break; } 
       	 case DOWN  :  { qsa[2] = val; break; } 
       	 case LEFT  :  { qsa[3] = val; break; } 
       	 default : { System.err.println("Unsupported direction QSA - set"); System.exit(1); return; }
       	 }
       	}
       	catch ( Exception e ) {
       		System.err.println("setQSA [" + dir + "]");
       		return;
       	}
    }
    
}

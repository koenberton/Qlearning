import org.cbr.ReinforcementLearning.GridCellDTO;
import org.cbr.ReinforcementLearning.GridDTO;
import org.cbr.generalPurpose.gpUtils;

public class QLearningtest {

	gpUtils xU = null;
	GridDTO grid =  null;
	
	
	public QLearningtest(gpUtils u)
	{
		xU=u;
	}
	
	//------------------------------------------------------------------- 
	public void makeGrid(int width , int heigth)
	{
		grid = new GridDTO();
		grid.makeGrid(width, heigth);
		
		for(int i=0;i<(width*heigth);i++)
		{
			GridCellDTO cell = grid.getCell(i);
			String ss= "";
			for(int j=0;j<4;j++) ss += "[" + j + "=" + cell.getNeighbourIndex(j) + "]";
			System.out.println( "[" + i + "] [" + (i % width) + "] [" + (i / width) + "] " + ss );
		}
		
		//startpunt
		grid.setStartCellIndex( (heigth - 1) * width );
		if( grid.getStartCell() == null ) {
			System.err.println("cannot set startcell");
			System.exit(1);
		}
		//eindpunten
		grid.setStopCellIndex( width - 1 );
		if( grid.getStopCell() == null ) {
			System.err.println("cannot set stopcell");
			System.exit(1);
		}
	}
	
	//------------------------------------------------------------------- 
	public void defavourCell(int i)
	{
		try {
		  GridCellDTO x = grid.getCell(i);
		  x.setReward( -10 );
		}
		catch(Exception e) {
			System.err.println("Cannot defaour");
			System.exit(1);
		}
	}
	
	//------------------------------------------------------------------- 
	public void performActions(int niters , int maxMoves, double epsilon )
	{
	    for(int i=0;i<niters;i++)
	    {
	    	int k = grid.performGridRun( maxMoves , epsilon );
	    	if( k >= maxMoves ) { System.out.println("End not reached"); }
	    }
	}
	
	
	
	public void show(int width)
	{
		grid.show(width);
	}
}

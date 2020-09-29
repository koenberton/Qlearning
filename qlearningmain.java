import org.cbr.generalPurpose.gpUtils;

public class qlearningmain {

	gpUtils xU = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Hello world\n");
		
		gpUtils xU = new gpUtils();
	    
		QLearningtest tst = new QLearningtest(xU);
		int w = 3;
		int h = 6;
		tst.makeGrid( w , h );
		
		//tst.defavourCell(7);
		//tst.defavourCell(9);
		
		tst.performActions( 100 , 4*4*50 , 1 );
		
		tst.show( w );
		
	}

	
	
}

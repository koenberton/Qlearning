package org.cbr.tekThread;


import java.util.ArrayList;


import org.cbr.generalPurpose.gpUtils;
import org.cbr.tekThread.ConcurrencyController.SEMAPHORE_TYPE;
import org.cbr.tekThread.ThreadMonitorDTO.MONITORSTATUS;


public class ThreadDispatcher {
	
	public enum REQUESTED_CLASS { PERFORM_TASKLIST , UNKNOWN }
	
	private static int MAX_TIME_MSEC = 60 * 60 * 1000;  // een uur
	private static int MAX_NBR_THREADS_ALLOWED = 10;
	
    gpUtils xU = null;
	
    private int loglevel = 99;
    private int max_threads = MAX_NBR_THREADS_ALLOWED;
	
    
    //----------------------------------------------------------------
    public void setMaxThreads(int mx)
    {
    	max_threads = mx;
    	if( (mx < 0) || (mx > 10) ) max_threads =  MAX_NBR_THREADS_ALLOWED;
    }
    
    
	//----------------------------------------------------------------
    private void logit(int level , String sLog)
    //----------------------------------------------------------------
    {
      if( level <= 5 ) System.out.println( sLog );
    }
                                   
    //----------------------------------------------------------------
    private void errit(String sLog)
    //----------------------------------------------------------------
    {
    	System.err.println( sLog );
    }
    
    //----------------------------------------------------------------
    public ThreadDispatcher(gpUtils u)
    //----------------------------------------------------------------
    {
    	xU = u;
    }
    
    //----------------------------------------------------------------
    public boolean performDispatch(ArrayList<ThreadMonitorDTO> list , REQUESTED_CLASS  requestedClass)
    //----------------------------------------------------------------
    {
    	ConcurrencyController locker = new ConcurrencyController( SEMAPHORE_TYPE.AUTHOR , null);
		for(int fieldIndex=0;fieldIndex<list.size();fieldIndex++)
		{
			ThreadMonitorDTO moni = list.get(fieldIndex);
			moni.setStatus(MONITORSTATUS.QUEUED);
		}
		long blockstarted = System.currentTimeMillis();
		long kicker = System.currentTimeMillis();
		boolean graceful=false;
		while ( (System.currentTimeMillis() - blockstarted) < MAX_TIME_MSEC )
		{
			// Get a lock
			if( locker.getLock() == false ) break;
			//
		    boolean threadStarted=false;    	
			int activeThreads=0;
			int firstQueueIndex=-1;
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) && (firstQueueIndex<0) ) firstQueueIndex = i;
				if( (list.get(i).getStatus() == MONITORSTATUS.BUSY) || (list.get(i).getStatus() == MONITORSTATUS.STARTED) ) activeThreads++;
			}
			//logit(5,"[Active=" + activeThreads + "]" );
			// Quit ?
			if( (firstQueueIndex <  0) && (activeThreads==0) ) { // nothing to process and nothing active
				graceful=true;
				locker.unLock();
				break;
			}
			//
			if( (activeThreads < max_threads ) && (firstQueueIndex >= 0)) {  // Start a thread
				list.get(firstQueueIndex).setStatus(MONITORSTATUS.STARTED);
				list.get(firstQueueIndex).setStarttime(System.currentTimeMillis());
				//logit(5,"Starting [" + list.get(firstQueueIndex).getDatafilename() );
	            //logit(5,"Starting [" + requestedClass + "] [" + list.get(firstQueueIndex).getFieldIndex() + "]" );
			
				// Start the requested thread
				switch( requestedClass )
				{
				/*
				 case DUMPFILESORT : {
					DumpFileSortThread th = new DumpFileSortThread(xMSet, list.get(firstQueueIndex) , locker.getSemaphore() );
					th.start();				
					threadStarted=true;
					break;
				 }
				 */
				
				
				 default  : {
					//daoutils.removeSemaphoreFile( list.get(firstQueueIndex).getSemaphoreFileStarted() );
					errit ("Unsupported Class requested for thread [" + requestedClass + "]");
					locker.unLock();
					return false;
				 }
				}
				
			}
			
			// debug
			int threadsToComplete=0;
			int nQueued=0;
			int nStarted=0;
			int nBusy=0;
			int nCompleted=0;
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) )  { threadsToComplete++; nQueued++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.STARTED) ) { threadsToComplete++; nStarted++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.BUSY) )    { threadsToComplete++; nBusy++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.COMPLETED) ) {nCompleted++; }
						
			}
			if( System.currentTimeMillis() - kicker > 5000L ) {  // kicks in every 5 secons
			 kicker = System.currentTimeMillis();
			 int ol = loglevel;
		     long runti = System.currentTimeMillis() - blockstarted; 
			 logit( 5 , "[Dispatcher=" + list.get(0).getLabel() + "] [ToDo=" + threadsToComplete + "] [Queued=" + nQueued + "] [Started="+nStarted + "] [Busy=" + nBusy + "] [Completed=" + nCompleted + "] [RunTime= " + runti + " msec]");
			 loglevel = ol;
			}
			
			// see if there are very long lasting threads
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) ) continue;
				if( (list.get(i).getStatus() == MONITORSTATUS.COMPLETED) ) continue;
				if( (list.get(i).getStatus() == MONITORSTATUS.FAILED) ) continue;
				//
				long runtime = System.currentTimeMillis() - list.get(i).getStarttime();
				if( ((runtime / 1000L) % 20L) == 19 ) {  // kicks in every 20 seconds
				 logit(loglevel , "[Thread=" + i + "] -> [" + list.get(i).getStatus() + "] [RunTime=" + (runtime / 1000L) + " seconds] " + runtime );
				}
			}
			//
			locker.unLock();
			//
			if( threadStarted ) continue;  // no sleep if you just started a thread
			// sleep
			try {
			 Thread.sleep(200);
			}
			catch(Exception e ) { errit("Got exception " + xU.LogStackTrace(e)); }
		}
		// clean
		for(int i=0;i<list.size();i++)
		{
			long runtime = list.get(i).getEndtime() - list.get(i).getStarttime();
			logit(loglevel , "[File=" + xU.getFolderOrFileName(list.get(i).getFullSourceFileName()) + "] [" + list.get(i).getStatus() + "] [RunTime=" + runtime + " milliSeconds]"   );
		}
		if( !graceful ) {
			errit("Thread monitor timed out");
			return false;
		}
		//
		int errors=0;
		for(int i=0;i<list.size();i++)
		{
			if( (list.get(i).getStatus() != MONITORSTATUS.COMPLETED) ) errors++;
		}
	    return (errors ==0 ) ? true : false;
    }
    
}

package org.cbr.transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

// A stand-alone transaction manager 
public class transactionManager {

	private String dbfile = null;
	private long TXUID = System.nanoTime();
	
	/*
	//-------------------------------------------------------------------
	private void doLog(String s)
	{
	  System.out.println( s );
	}
	*/	
	//-------------------------------------------------------------------
	private void doError(String s)
	{
	  System.err.println( s );
	}
	
	public transactionManager(String LongFileName)
	{
		dbfile = LongFileName;
	}
	
	//-------------------------------------------------------------------
	private boolean checkdatabasefile()
	{
		if( fileExists( dbfile ) ) return true;
	    doError( "database file [" + dbfile + "] not found"); 
		return false;
	}
	
	// copy dbase to before image and set lock file
	//-------------------------------------------------------------------
	public boolean beginTransaction()
	{
		if( checkdatabasefile() == false ) return false;
        if( isLocked() ) { doError("database is locked"); return false; }
        if( fileExists(dbfile) == false ) {
        	doError( "database [" + dbfile + "] does not exist" );
        	return false;
        }
        if( saveCopyFile( dbfile , getBeforeImageFileName()) == false ) return false;
		return setLock();
	}
	
	//-------------------------------------------------------------------
	public boolean commitTransaction()
	{
		if( checkdatabasefile() == false ) return false;
	    if( fileExists( getBeforeImageFileName()) == false ) {
	    	doError( "Commit - Out of Band - Before image [" + getBeforeImageFileName() +"] is missing" );
	    	return false;
	    }
		if( !isLocked() ) { doError("Commit - Out of band - database not locked"); return false; }
        if( removeLock() == false ) return false;
        return removeFile( getBeforeImageFileName() , "commit" );
	}
	
	// move before image over database - unset all
	//-------------------------------------------------------------------
	public boolean rollbackTransaction()
	{
		if( checkdatabasefile() == false ) return false;
	    if( fileExists( getBeforeImageFileName()) == false ) {
	    	doError( "Out of Band - Before image [" + getBeforeImageFileName() +"] is missing" );
	    	return false;
	    }
		if( !isLocked() ) { doError("Out of band - database not locked"); return false; }
		if( removeFile( dbfile , "rollback 1") == false) {
			doError( "Afterimage [" + dbfile + "] cannot be removed" );
	    	return false;
		}
		if( saveCopyFile( getBeforeImageFileName() , dbfile) == false ) {
			doError( "Before Image [" + dbfile + "] cannot be restored" );
	    	return false;
		}
		if( removeLock() == false ) return false;
		if( removeFile( getBeforeImageFileName() , "rollback 2") == false ) {
			doError( "WARNING - Before Image [" + dbfile + "] cannot be removed" );
	    	return false;
		}
	    return true;	
	}
	
	// if there is no db nothing to do
	// if there is a lock and no before image then corrupt
	// if there is a lock and before then just rollback
	// if there is a before image just remove it
	//-------------------------------------------------------------------
	public boolean recoverTransaction()
	{
		if( checkdatabasefile() == false ) return false;
		if( (isLocked()) && (fileExists(getBeforeImageFileName())==false) ) {
		     doError("Database is possibly corrupt [" + dbfile +"]");
		     return false;
		}
		if( (isLocked()) && (fileExists(getBeforeImageFileName())) ) {
			return rollbackTransaction();
		}
		return removeIfExists( getBeforeImageFileName());
	}
	
	//-------------------------------------------------------------------	
	//-------------------------------------------------------------------
	private boolean isLocked()
	{
		return fileExists(getLockFileName());
	}
	//-------------------------------------------------------------------
	private boolean setLock()
	{
		if( removeIfExists( getLockFileName() ) == false ) return false;
		boolean succes=false;
		try {
	      File file = new File(getLockFileName());
	      succes = file.createNewFile();
	    } catch(Exception e) { e.printStackTrace(); return false; }
	    return succes;
	}
	//-------------------------------------------------------------------
	private boolean removeLock()
	{
		if( removeFile( getLockFileName() , "removelock") == false )
		{
			doError( "Lock [" + dbfile + "] cannot be removed" );
	    	return false;
		}
		return true;
	}
	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	private String getBeforeImageFileName()
	{
		return dbfile + TXUID + "-bi.txt";
	}
	//-------------------------------------------------------------------
	private String getLockFileName()
	{
		return dbfile + TXUID + "-lck.txt";
	}
	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	private boolean removeIfExists( String LongFileName )
	{
		if( fileExists( LongFileName ) == false ) return true;
	    return removeFile( LongFileName , "rout" );
	}
	//-------------------------------------------------------------------
	private boolean saveCopyFile( String SourceLongFileName , String TargetLongFileName )
	{
        if( removeIfExists( TargetLongFileName) == false ) return false;
        if( fileExists( SourceLongFileName) == false ) {
        	doError("Source [" + SourceLongFileName +"] does not exist"); return false;
        }
		if( copyFile( SourceLongFileName , TargetLongFileName) == false ) return false;
		return true;
	}
	//-------------------------------------------------------------------
	private boolean removeFile( String sIn , String src)
	{
        File FObj = new File(sIn);
        if ( FObj.isFile() != true ) {
        	doError( "ERROR " + sIn + ") -> file not found" + src);
        	return false;
        }
        if ( FObj.getAbsolutePath().length() < 10 ) {
        	doError( sIn + "-> Length too small. File will not be deleted" + src);
        	return false;  // blunt safety
        }
        FObj.delete();
        File XObj = new File(sIn);
        if ( XObj.isFile() == true ) {
        	doError("ERROR" + sIn+ " -> could not be deleted" + src);	
        }
        FObj=null;
        XObj=null;
        return true;
	}
	//-------------------------------------------------------------------
	public boolean fileExists( String sIn )
	{
	   if( sIn == null ) return false;
	   try {
			 File fObj = new File(sIn);
			 if ( fObj.exists() == true ) { return fObj.isFile();  } 
		     return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	//-------------------------------------------------------------------
	private boolean copyFile(String sIn , String sOut)
	{
	   InputStream in = null;
	   OutputStream out = null; 
		try {
			   in = null;
			   out = null; 
			   byte[] buffer = new byte[16384];
			  
			   in = new FileInputStream(sIn);
			   out = new FileOutputStream(sOut);
			   while (true) {
			         synchronized (buffer) {
			            int amountRead = in.read(buffer);
			            if (amountRead == -1) {
			               break;
			            }
			            out.write(buffer, 0, amountRead); 
			         }
			  } 
			  return true;
		}
		catch (Exception e) { return false;	}
		finally {
			try {
			  if (in != null) in.close();
			  if (out != null) {
			    	 out.flush();
			         out.close();
			  }
			}
			catch(Exception e) { return false; }
		}
	}
	
}

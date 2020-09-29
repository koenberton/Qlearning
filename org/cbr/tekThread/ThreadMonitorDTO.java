package org.cbr.tekThread;


public class ThreadMonitorDTO {

        public enum MONITORSTATUS  { QUEUED , STARTED , BUSY , COMPLETED , FAILED }

        String label="Unknown";
		int fieldIndex=-1;
		String fullSourceFileName = null;
		String dumpfileName = null;
		MONITORSTATUS status = MONITORSTATUS.QUEUED;
		long starttime=-1L;
		long endtime=-1L;
	    //
		
		public void shallowCopy(ThreadMonitorDTO x)
		{
			label                  = x.label;
			fieldIndex             = x.fieldIndex;
			fullSourceFileName     = x.fullSourceFileName;
			dumpfileName           = x.dumpfileName;
			status                 = x.status;
			starttime              = x.starttime;
			endtime                = x.endtime;
		}
		
		public ThreadMonitorDTO(String lb , int id)
		{
			label = lb;
			fieldIndex=id;
		}

		public String getLabel()
		{
			return this.label;
		}
		
		public void setLabel(String l)
		{
			this.label = l;
		}
		
		public int getFieldIndex() {
			return fieldIndex;
		}

		public void setFieldIndex(int fieldIndex) {
			this.fieldIndex = fieldIndex;
		}

		public String getFullSourceFileName() {
			return fullSourceFileName;
		}

		public void setFullSourceFileName(String fullsourcefilename) {
			this.fullSourceFileName = fullsourcefilename;
		}

		public MONITORSTATUS getStatus() {
			return status;
		}

		public void setStatus(MONITORSTATUS status) {
			this.status = status;
		}

		public long getStarttime() {
			return starttime;
		}

		public void setStarttime(long starttime) {
			this.starttime = starttime;
		}

		public long getEndtime() {
			return endtime;
		}

		public void setEndtime(long endtime) {
			this.endtime = endtime;
		}

		public String getDumpfileName() {
			return dumpfileName;
		}

		public void setDumpfileName(String dumpfileName) {
			this.dumpfileName = dumpfileName;
		}
	
}

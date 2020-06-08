package org.timo.chunkedupload.model;

public class RangeData {
	private String filename;
	private String splitedFilename;
	private long begin = 0;
	private long end = 0;
	private long totalSize = 0;
	
	public RangeData() {
		super();
	}
	
    public RangeData(String filename, String splitedFilename, int i, long fileLength, long totalSize) {
		this();
		this.filename = filename;
		this.splitedFilename = splitedFilename;
		this.begin = i * fileLength;
		this.end = ((i * fileLength ) + fileLength) - 1;
		if(end > totalSize) {
			end = totalSize - 1;
		}
		this.totalSize = totalSize;
		
	}

	public RangeData(long begin, long end, long totalSize) {
        this.totalSize = 0;
		this.begin = begin;
        this.end = end;
        this.totalSize = totalSize;
    }

	public String getSplitedFilename() {
		return splitedFilename;
	}
	
	public void setSplitedFilename(String splitedFilename) {
		this.splitedFilename = splitedFilename;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getBegin() {
		return begin;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}


	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public String toString() {
		return "RangeData [splitedFilename=" + splitedFilename + ", begin=" + begin + ", end=" + end + "]";
	}
}

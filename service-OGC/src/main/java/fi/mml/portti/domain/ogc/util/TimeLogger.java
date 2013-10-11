package fi.mml.portti.domain.ogc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TimeLogger {
	
	private long startTime = 0;
	private HashMap<String, Long> startTimeMoments = new HashMap<String, Long>();
	private HashMap<String, Long> stopTimeMoments = new HashMap<String, Long>();
	private List<String> stack = new ArrayList<String>();
	
	public TimeLogger() {
		startTime = System.currentTimeMillis();
	}
	
	public void addStackTime(String info) {
		stack.add(info+":"+ convertMillis2readable(count(startTime,System.currentTimeMillis())));
	}
	
	public String getStackTimes() {
		StringBuilder sb = new StringBuilder();
		
		for (String moment: stack) {
			sb.append(moment +"\n");
		}
		
		return sb.toString();
		
		
	}
	
	public void setStartTimeMoment(String key) {
		startTimeMoments.put(key, new Long(System.currentTimeMillis()));
		stopTimeMoments.put(key, new Long(System.currentTimeMillis()));
	}
	
	public void setStopTimeMoment(String key) {
		
		if (!startTimeMoments.containsKey(key)) {
			startTimeMoments.put(key, new Long(System.currentTimeMillis()));
		}
		stopTimeMoments.put(key, new Long(System.currentTimeMillis()));
	}
	
	public List<String> getAllMoments() {
		List<String> all = new ArrayList<String>();
		
		Iterator<String> iter = startTimeMoments.keySet().iterator();
		
		while (iter.hasNext()) {
			String key = iter.next();
			
			long mills = count(startTimeMoments.get(key), stopTimeMoments.get(key));
			all.add(key+":"+convertMillis2readable(mills));
		}
		
		return all;
	}
	
	private long count(Long startTime, Long stopTime) {
		
		long value = stopTime.longValue() - startTime.longValue();
		
		return value;
	}
	
	private String convertMillis2readable(long mills) {
		
		String readable = "N/A";
		
		/*Date date = new Date(mills);
		readable = format.format(date);
		*/
		
		long mins = mills/(1000*60);
		long secs = 0;
		
		
		if (mins > 0) {
			if (mills%(1000*60) > 0) {
				secs = mills/(1000*60);
				if (secs%1000>0) {
					mills = secs%1000;
					return mins +"min "+secs+"s "+ (mills%1000) +"ms";
				}
				return mins +"min "+secs+"s ";
			}
			
		}else {
			secs = mills/1000;
			if (mills%1000>0) {
				mills = mills%1000;
				return secs+"s "+ mills +"ms";
			}
			return secs+"s ";
		}
		
		
		return readable;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		List<String> all = getAllMoments();
		
		for (String moment: all) {
			sb.append(moment +"\n");
		}
		
		return sb.toString();
	}
	

}

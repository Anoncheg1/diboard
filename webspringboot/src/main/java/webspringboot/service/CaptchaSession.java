package webspringboot.service;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
//TODO: add timer for every session.
public class CaptchaSession {//very simple session FIFO.  
	private static final int MAX = 5000;//sessions
	private static final int CHARS = 10;//chars in session identifier
	
	//private static LinkedList<String> listPref = new LinkedList<String>(); //max 500
	private static Queue<String> listPref = new ArrayDeque<String>(MAX); //ordered sessions
	private static ConcurrentHashMap<String, String> prefSol = new ConcurrentHashMap <String, String>(MAX); //solutions
	
	
	public synchronized static void put(String prefix, String solution)
	{
		if (listPref.size()>=MAX)
			prefSol.remove(listPref.poll());
		
		listPref.add(prefix);
		prefSol.put(prefix, solution);
	}
	
	/**
	 * if  solution is null then captcha is expired
	 * 
	 * @param prefix
	 * @return solution for captcha
	 */
	public static String get(String prefix) //already concurrent hashmap
	{
		return prefSol.get(prefix); 
	}
	
	public static String generate()
	{
		char[] buf = new char[CHARS];
		for(int i = 0; i< CHARS; i++){
			int pos = (int) (Math.random()*CaptchaGen.ALL_ENGLISH_CHARS_AND_NUMBERS.length());
			buf[i] = CaptchaGen.ALL_ENGLISH_CHARS_AND_NUMBERS.charAt(pos);
		}
		return String.valueOf(buf); 
	}
}

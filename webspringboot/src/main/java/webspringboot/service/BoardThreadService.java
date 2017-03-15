package webspringboot.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import dibd.config.Config;
import dibd.daemon.NNTPConnection;
import dibd.feed.PushDaemon;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageBackendException;
import dibd.storage.StorageManager;
import dibd.storage.article.Article;
import dibd.storage.impl.JDBCDatabase;
import dibd.storage.web.ShortRefParser;
import dibd.storage.web.StorageWeb;
import dibd.storage.web.ThRLeft;
import dibd.storage.web.WebRef;
import dibd.util.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import webspringboot.entity.ArticleWeb;

/**
 * Represents a boards - groups.
 *
 * @author Vitalij Chepelev
 * @since webspringboot/1.0.1
 * TODO: создать пул объектов базы и выдавать его объекты тредам на использование
 */
@Component
public class BoardThreadService {

	//private final StorageWeb db;
	// TODO Make configurable
	public static final int QUEUE_SIZE = 5;
	
	//must be thread-safe
	private final ArrayBlockingQueue<StorageWeb> storageWeb = new ArrayBlockingQueue<StorageWeb>(QUEUE_SIZE);
	
	private BoardThreadService() throws SQLException {
		super();
		for(int i = 0; i<QUEUE_SIZE; i++){
			
			JDBCDatabase db = new JDBCDatabase();
			db.arise();
			storageWeb.add(db);
		}
		
	}
	
	
	/**
	 * @param name String
	 * @return BoardId
	 * @throws NoSuchFieldException if not exist
	 */
	public int getBoardIdByName(String boardName) throws NoSuchFieldException {
		Group g = StorageManager.groups.get(boardName);
		if(g != null)
			return g.getInternalID();
		else
			throw new NoSuchFieldException();
	}
/*
	public String getBoardNameById(Integer id){ 
		return boards.; 
	}
	*/ 
	/*public List<String> getBoardsNames() {
		Group.getAll()getClass().
		return new ArrayList<String>(boards.keySet());
	}*/

	
	/**
	 * Get threads with rLeft
	 * 
	 * @param boardName
	 * @param boardPage should be correct.
	 * @return
	 * @throws Exception if boardName is wrong
	 */
	public Map<ThRLeft<ArticleWeb>,List<ArticleWeb>> getThreads(String boardName, int boardPage) throws NoSuchFieldException {
		Map<ThRLeft<ArticleWeb>,List<ArticleWeb>> ret;
		
		Integer boardId = StorageManager.groups.get(boardName).getInternalID();
		if(boardId == 0)
			throw new NoSuchFieldException("Wrong boardName.");
		
		StorageWeb db = null;
		try {
			db = storageWeb.take();
			Map<ThRLeft<Article>,List<Article>> threads = db.getThreads(boardId, boardPage, boardName);

			ret = new LinkedHashMap<ThRLeft<ArticleWeb>,List<ArticleWeb>>();
			for (Entry<ThRLeft<Article>, List<Article>> entry : threads.entrySet()) {
				//value
				List<ArticleWeb> replays = new ArrayList<ArticleWeb>();
				for(Article a: entry.getValue()){
					Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, a.getMessage());

					replays.add(new ArticleWeb(a, refs));
				}
				//key
				ThRLeft<Article> key = entry.getKey();
				Article thread = key.getThread();
				Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, thread.getMessage());
				ThRLeft<ArticleWeb> threadW = new ThRLeft<ArticleWeb>(new ArticleWeb(thread, refs), key.getRLeft()); 
				ret.put(threadW, replays);
			}

		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "BoardService.getThreads() failed: {0}", e);
			return null;
		} catch (InterruptedException e) {
			return null;
		} finally{
			try {
				if(db != null)
					storageWeb.put(db);
			} catch (InterruptedException ex) {}
		}
			
		return ret;
	}
	
	
	/**
	 * Get threads count total for boardName.
	 * 
	 * @param boardName
	 * @return threads count
	 * @throws NoSuchFieldException if boardName not exist 
	 */
	public int getThreadsCount(String boardName) throws NoSuchFieldException {
		Integer boardId = StorageManager.groups.get(boardName).getInternalID();
		if(boardId == 0)
			throw new NoSuchFieldException();
		int count=0;
		StorageWeb db = null;
		try {
			db = storageWeb.take();
			count = db.getThreadsCountGroup(boardId);
		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "BoardService.getThreadsCount() failed: {0}", e);
			return Integer.MAX_VALUE;
		} catch (InterruptedException e) {
		} finally{
			try {
				if(db != null)
					storageWeb.put(db);
			} catch (InterruptedException ex) {}
		}
		return count;
	}
	

	// public void validateBoard(String boardId);
	// public Board getBoardById(String boardId);
	// public List<Board> getAllBoards();
	// public List<List<Message>> getThread(String boardId);
	// public List<String> getAllBoards();
	
	

	
	/**
	 *  Create new thread or replay.
	 * 
	 * @param threadId if null - thread. if not null - replay
	 * @param boardName
	 * @param boardId
	 * @param name
	 * @param subject
	 * @param message
	 * @param file
	 * @return 0 if already exist
	 */
	public int createArticle(final Integer threadId, final String boardName, final int boardId, 
			String name, final String subject, String message, final MultipartFile file) {

		//Map <String, String> short_ref_messageId = getShortRefs(message);
		int ret_id = 0; //with id
		StorageWeb db = null;
		try {
			db = storageWeb.take();
			Article art = new Article(threadId, name, subject, ShortRefParser.getShortRefs(db, message),
					boardId, boardName);//, short_ref_messageId);

			byte[] b = null;
			String ct = null;
			String fname = null;
			if(file != null){
				b = file.getBytes();
				ct = file.getContentType();
				fname = file.getOriginalFilename();
			}
			
			Article article;
			if(threadId == null)
				article = db.createThreadWeb(art, b, ct, fname);
			else
				article = db.createReplayWeb(art, b, ct, fname);

			if (article == null) //exist
				return 0;
			else
				ret_id = article.getId();
				
			//peering
			PushDaemon.queueForPush(article);
		} catch (InterruptedException| IOException e) {
			e.printStackTrace();
		} catch (StorageBackendException e1) {
			Log.get().log(Level.SEVERE, "ThreadService.createArticle() failed: {0}", e1);
		}finally{try {
			if (db !=null)
				storageWeb.put(db);
		} catch (InterruptedException e) {}}

		return ret_id;
	}
	

	public List<ArticleWeb> getOneThread(int threadId, String boardName) throws StorageBackendException{
		List<ArticleWeb> thread = new ArrayList<ArticleWeb>();
		
		StorageWeb db = null;
		try {
			db = storageWeb.take();
			List<Article> al = db.getOneThread(threadId, boardName);

			
			for(Article a: al){
				Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, a.getMessage());
				thread.add(new ArticleWeb(a, refs));
			}
		} catch (InterruptedException e) {
			return null;
		}finally{try {
			if (db !=null)
				storageWeb.put(db);
		} catch (InterruptedException e) {}}

		return thread;
	}
	
	/**
	 * Required to make limit of rLeft.
	 * 
	 * @param threadId
	 * @return
	 */
	public int getReplaysCount(int threadId){
		StorageWeb db = null;
		int count = -1;
		try {
			db = storageWeb.take();
			count = db.getReplaysCount(threadId);
		} catch (InterruptedException e) {
		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "ThreadService.getReplaysCount() failed: {0}", e);
		}finally{try {
			if (db !=null)
				storageWeb.put(db);
		} catch (InterruptedException e) {}}
		
		return count;
	}
	
}

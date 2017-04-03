package webspringboot.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

import dibd.feed.FeedManager;
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
 * Fucking magic because of dynamically generated threads.
 *
 * @author Vitalij Chepelev
 * @since webspringboot/1.0.1
 * 
 */
@Component
public class BoardThreadService {

	//private final StorageWeb db;
	// TODO Make configurable
	public static final int QUEUE_SIZE = 9;
	
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
	 * @return Group may be null
	 */
	public Group getGroup(String boardName){
		return StorageManager.groups.get(boardName);
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

	Object lock1 = new Object();
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
		Map<ThRLeft<Article>,List<Article>> threads;
		synchronized(lock1){
			try {
				db = storageWeb.take();
				threads = db.getThreads(boardId, boardPage, boardName);
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
		}
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
		
		return ret;
	}
	
	
	Object lock2 = new Object();
	/**
	 * Get threads count total for boardName.
	 * 
	 * @param boardName
	 * @return threads count
	 * @throws NoSuchFieldException if boardName not exist 
	 */
	public int getThreadsCount(String boardName) throws NoSuchFieldException {
		synchronized(lock2){
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
	}
	

	Object lock3 = new Object();
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
	public int createArticle(final Integer threadId, Group group, 
			String name, final String subject, String message, final MultipartFile file) {
		
		synchronized(lock3){
			//Map <String, String> short_ref_messageId = getShortRefs(message);
			int ret_id = 0; //with id
			StorageWeb db = null;
			File tmpf = null;
			try {
				db = storageWeb.take();
				Article art = new Article(threadId, name, subject, ShortRefParser.shortRefParser(db, message), group);

				String ct = null;
				String fname = null;
				if(file != null){
					ct = file.getContentType();
					fname = file.getOriginalFilename();

					tmpf = File.createTempFile(fname, "");
					file.transferTo(tmpf);
				}

				Article article;
				if(threadId == null)
					article = db.createThreadWeb(art, tmpf, ct, fname);
				else
					article = db.createReplayWeb(art, tmpf, ct, fname);

				if (article == null) //exist
					return 0;
				else
					ret_id = article.getId();

				//peering
				FeedManager.queueForPush(article);
			} catch (InterruptedException| IOException e) {
				e.printStackTrace();
			} catch (StorageBackendException e1) {
				Log.get().log(Level.SEVERE, "ThreadService.createArticle() failed: {0}", e1);
			}finally{try {
				if (db !=null)
					storageWeb.put(db);
				if (tmpf != null && tmpf.exists())//not happen if file moved.
					tmpf.delete();


			} catch (InterruptedException e) {}}

			return ret_id;
		}
	}
	
	Object lock4 = new Object();
	
	public List<ArticleWeb> getOneThread(int threadId, Group group){
		synchronized(lock4){
			List<ArticleWeb> thread = new ArrayList<ArticleWeb>();

			StorageWeb db = null;
			try {
				db = storageWeb.take();
				assert(group != null);
				List<Article> al = db.getOneThread(threadId, group.getName(), 1); //get 1 and 0 status
				if (al.isEmpty())
					return new ArrayList<ArticleWeb>(0);

				for(Article a: al){
					Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, a.getMessage());
					thread.add(new ArticleWeb(a, refs));
				}
			} catch (InterruptedException | StorageBackendException e) {
				return thread;
			}finally{try {
				if (db !=null)
					storageWeb.put(db);
			} catch (InterruptedException e) {}}

			return thread;
		}
	}
	
	Object lock5 = new Object();
	/**
	 * Required to make limit of rLeft.
	 * 
	 * @param threadId
	 * @return
	 */
	public int getReplaysCount(int threadId){
		synchronized(lock5){
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
	
}

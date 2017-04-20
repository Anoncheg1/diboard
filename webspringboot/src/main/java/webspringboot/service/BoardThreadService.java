package webspringboot.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import dibd.feed.FeedManager;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageBackendException;
import dibd.storage.StorageManager;
import dibd.storage.article.Article;
import dibd.storage.web.ShortRefParser;
import dibd.storage.web.StorageObjectPool;
import dibd.storage.web.StorageWeb;
import dibd.storage.web.ThRLeft;
import dibd.storage.web.WebRef;
import dibd.util.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import webspringboot.entity.ArticleWeb;
import webspringboot.entity.ArticleWeb.oneMany;

/**
 * Represents a boards - groups.
 *
 * For dynamically spawn threads by Spring Boot.
 *
 * @author user
 * @since webspringboot/1.0.1
 * 
 */
@Component
public class BoardThreadService {

	//used instead of StorageManager.current()
	//work much slower with limit of 7 threads at one time.
	private StorageWeb db = new StorageObjectPool(); 
	
	private BoardThreadService() throws SQLException {
		super();
	}
	
	/**
	 * @param name String
	 * @return Group may be null
	 */
	public Group getGroup(String boardName){
		return StorageManager.groups.get(boardName);
	}

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
		
		Map<ThRLeft<Article>,List<Article>> threads;

		try {
			threads = db.getThreads(boardId, boardPage, boardName);
		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "BoardService.getThreads() failed: {0}", e);
			return null;
		}

		ret = new LinkedHashMap<ThRLeft<ArticleWeb>,List<ArticleWeb>>();
		for (Entry<ThRLeft<Article>, List<Article>> entry : threads.entrySet()) {
			//value
			List<ArticleWeb> replays = new ArrayList<ArticleWeb>();
			for(Article a: entry.getValue()){
				Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, a.getMessage());

				replays.add(new ArticleWeb(a, refs, oneMany.Board));
			}
			//key
			ThRLeft<Article> key = entry.getKey();
			Article thread = key.getThread();
			Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, thread.getMessage());
			ThRLeft<ArticleWeb> threadW = new ThRLeft<ArticleWeb>(new ArticleWeb(thread, refs, oneMany.Board), key.getRLeft()); 
			ret.put(threadW, replays);
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
		try {

			count = db.getThreadsCountGroup(boardId);
		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "BoardService.getThreadsCount() failed: {0}", e);
			return Integer.MAX_VALUE;
		}
		
		return count;
	}
	
	
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

		int ret_id = 0; //with id
		File tmpf = null;
		try {

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
		} catch (IOException e) {
			Log.get().log(Level.SEVERE, "createArticle() failed: {0}", e);
		} catch (StorageBackendException e1) {
			Log.get().log(Level.SEVERE, "createArticle() failed: {0}", e1);
		}finally{
			if (tmpf != null && tmpf.exists())//not happen if file moved.
				tmpf.delete();
		}

		return ret_id;
	}
	
	
	/**
	 * @param threadId
	 * @param group
	 * @return
	 */
	public List<ArticleWeb> getOneThread(int threadId, Group group){
	
		List<ArticleWeb> thread = new ArrayList<ArticleWeb>();

		try {

			assert(group != null);
			List<Article> al = db.getOneThread(threadId, group.getName(), 1); //get 1 and 0 status
			if (al.isEmpty())
				return new ArrayList<ArticleWeb>(0);

			for(Article a: al){
				Map<String, WebRef> refs = ShortRefParser.getGlobalRefs(db, a.getMessage());
				thread.add(new ArticleWeb(a, refs, oneMany.OneThread));
			}
		} catch (StorageBackendException e) {
			return thread;
		}

		return thread;
	}
	
	/**
	 * Required to make limit of rLeft.
	 * 
	 * @param threadId
	 * @return
	 */
	public int getReplaysCount(int threadId){
		int count = -1;
		try {
			count = db.getReplaysCount(threadId);
		} catch (StorageBackendException e) {
			Log.get().log(Level.SEVERE, "getReplaysCount() failed: {0}", e);
		}

		return count;
	}
	
}

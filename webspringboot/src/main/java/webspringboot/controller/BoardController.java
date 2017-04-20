package webspringboot.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import dibd.config.Config;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageManager;
import dibd.storage.web.ThRLeft;
import dibd.util.Log;
import webspringboot.entity.ArticleWeb;
import webspringboot.service.BoardThreadService;
import webspringboot.service.CaptchaSession;



/**
 * 
 * 1)index with board links
 * 2) boards
 * 
 * @author user
 *
 */
@Controller
public class BoardController {

	@Autowired
	BoardThreadService service;
	
	int size = Config.inst().get(Config.MAX_ARTICLE_SIZE, 1);

	
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public final class NotFoundException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		//  class definition
	}
	
	
	@RequestMapping("/")
	public void index(HttpServletResponse resp) throws IOException {
		File ofile = new File("index.html");
		if(ofile.exists()){
			try {
				FileInputStream fos = new FileInputStream (ofile);
				OutputStream or = null;
			    try {
			    	resp.setContentType("text/html");
			    	or = resp.getOutputStream();
			    	//BufferedOutputStream bo = new BufferedOutputStream(resp.getOutputStream());
			    	byte[] bs = new byte[1024];
			    	int n = 0;
			    	while((n = fos.read(bs)) != -1){
			    		or.write(bs, 0, n);
			    	}
			    	or.flush();
			    	
				} catch (IOException e) {
					Log.get().log(Level.INFO, "exception {0}", e.getLocalizedMessage());
				}finally{
					if (or != null)
						try {
							or.close();
						} catch (IOException e1) {}
					if (fos != null)
						try {
							fos.close();
						} catch (IOException e) {	}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			//TODO:error or do redirect somehow
			
			try {
				resp.sendError(404);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//show board
	@RequestMapping(value = "/{boardName}/{boardPage:[0-9]?}")
	public String getBoard(@PathVariable String boardName, @PathVariable int boardPage, Map<String, Object> model) {
		//System.out.println("c"+Thread.currentThread().getId());
		Set<Entry<ThRLeft<ArticleWeb>, List<ArticleWeb>>> the = null;
		// Page and boardName check
		int count = 0; //количество thread
		int pagesCount = 0;
		
		//check that group is not deleted. Do we need it?
		Group g = StorageManager.groups.get(boardName);
		if(g == null || g.isDeleted())
			//throw new NotFoundException(); 
			return "redirect:pages/errorPage404";
		
		try {
			count = service.getThreadsCount(boardName);
		} catch (NoSuchFieldException e) {
			Log.get().log(Level.INFO, "BoardController.getBoardIndex() at getThreads() failed: {0}", e);
			return "redirect:pages/errorPage404";
		}
		if (count != 0) {
			int tpp= Config.inst().get(Config.THREADS_PER_PAGE, 5);
			//count=1-... , pagesCount=0....
			pagesCount = count / tpp - (count % tpp == 0 ? 1 : 0);
			
			if (boardPage > pagesCount)
				return "redirect:pages/errorPage404";

			// Get threads
				try {
					//System.out.println(boardName+ boardPage);
					Map<ThRLeft<ArticleWeb>, List<ArticleWeb>> th = service.getThreads(boardName, boardPage);
					if(th != null)
							the = th.entrySet();
				} catch (NoSuchFieldException e) {
					Log.get().log(Level.INFO, "BoardController.getBoardIndex() at getThreads() failed: {0}", e);
					return "redirect:pages/errorPage404";
				}
		}
		// Prepare pages navigation
		String pages = "";
		for (int i = 0; i <= pagesCount; i++)
			if (i == boardPage) {
				pages = pages.concat("[" + i + "]&ensp;");
			} else
				pages = pages.concat("<a href=" + i + ">[" + i + "]</a>&ensp;");

		model.put("boardName", boardName);
		model.put("hostName", Config.inst().get(Config.HOSTNAME, null));
		model.put("pages", pages); //[0][1] - one line
		// form
		model.put("post_url", "/post/" + boardName + "/");
		model.put("reference", "");
		model.put("button", "New Thread");
		model.put("files", true);
		model.put("prefix", CaptchaSession.generate());
		model.put("size", this.size);

		model.put("threads", the);
		return "board";
	}
	
	
		//show board form
		@RequestMapping(value = "/{boardName}/captcha")
		public String getBoardCaptcha(@PathVariable String boardName, Map<String, Object> model) {
			//check that group is not deleted. Do we need it?
			Group g = StorageManager.groups.get(boardName);
			if(g == null || g.isDeleted())
				return "redirect:pages/errorPage404";
			
			model.put("boardName", boardName);
			model.put("hostName", Config.inst().get(Config.HOSTNAME, null));
			
			// form
			model.put("post_url", "/post/" + boardName + "/");
			model.put("reference", "");
			model.put("button", "New Thread");
			model.put("files", true);
			model.put("prefix", CaptchaSession.generate());
			model.put("size", this.size);
			
			return "boardsepcaptcha";
		}
	

	@RequestMapping(value = "/{boardName}/")
	public String getBoard2(@PathVariable String boardName) {
		return "redirect:/{boardName}/0";
	}
	
}
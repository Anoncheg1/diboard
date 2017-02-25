package webspringboot.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import dibd.config.Config;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageManager;
import dibd.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

	@RequestMapping("/")
	public String index(Map<String, Object> model) {
		model.put("boards_names", StorageManager.groups.getAllNames());
		model.put("message", "Groups network:");
		
		List<String> groups= new ArrayList<>();
		for(Group g : StorageManager.groups.getAll()){
			Set<String> hosts = g.getHosts();
			if (!hosts.isEmpty() && !g.isDeleted()){
				StringBuilder sb = new StringBuilder(g.getName()).append(": ");
				Iterator<String> it = g.getHosts().iterator();
				sb.append(it.next());
				while(it.hasNext()){
					sb.append(", ").append(it.next());
				}
				groups.add(sb.toString());
			}
			
		}
		model.put("groups_network", groups); //IMPORTANT TO BE PUBLIC
		
		return "index";
	}

	@RequestMapping(value = "/{boardName}/{boardPage:[0-9]?}")
	public String getBoard(@PathVariable String boardName, @PathVariable int boardPage, Map<String, Object> model) {
		//System.out.println("c"+Thread.currentThread().getId());
		Set<Map.Entry<ArticleWeb, List<ArticleWeb>>> te = null;
		// Page and boardName check
		int count = 0; //количество thread
		int pagesCount = 0;
		
		//check that group is not deleted. Do we need it?
		Group g = StorageManager.groups.get(boardName);
		if(g == null || g.isDeleted())
			return "redirect:pages/errorPage404";
		
		try {
			count = service.getThreadsCount(boardName);
		} catch (NoSuchFieldException e) {
			Log.get().log(Level.INFO, "BoardController.getBoardIndex() at getThreads() failed: {0}", e);
			System.out.println("weha are here");
			return "redirect:pages/errorPage404";
		}
		if (count != 0) {
			int tpp= Config.inst().get(Config.THREADS_PER_PAGE, 5);
			pagesCount = count / tpp - (count % tpp == 0 ? 1 : 0);
			
			if (boardPage > pagesCount)
				return "redirect:pages/errorPage404";

			// Get threads
				try {
					//System.out.println(boardName+ boardPage);
					Map<ArticleWeb, List<ArticleWeb>> t = service.getThreads(boardName, boardPage);
					if(t != null)
							te = t.entrySet();
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
		model.put("hostName", Config.inst().get(Config.HOSTNAME, "localhost"));
		model.put("pages", pages); //[0][1] - one line
		// form
		model.put("post_url", "/post/" + boardName + "/");
		model.put("reference", "");
		model.put("button", "New Thread");
		model.put("files", true);
		model.put("prefix", CaptchaSession.generate());

		model.put("threads", te);
		return "board";
	}

	@RequestMapping(value = "/{boardName}/")
	public String getBoard2(@PathVariable String boardName, Map<String, Object> model) {
		return "redirect:/{boardName}/0";
	}

}
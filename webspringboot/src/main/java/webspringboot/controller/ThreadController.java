package webspringboot.controller;

import webspringboot.entity.ArticleWeb;
import webspringboot.service.*;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import dibd.config.Config;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageBackendException;
import dibd.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ThreadController {

	@Autowired
	BoardThreadService service;

	class Capsule {
		public String message;

		Capsule(String m) {
			this.message = m;
		}
	}
	
	// create thread
	//TODO:check limites for name, subject, message
	@RequestMapping(value = "/post/{boardName}/", method = RequestMethod.POST)
	public String postThread(@PathVariable final String boardName, @RequestParam("name") String name,
			@RequestParam("subject") String subject, @RequestParam("message") String message,
			@RequestParam(value = "attachment", required = false) MultipartFile file,
			@RequestParam("session") String prefix,
			@RequestParam("captcha") String captcha, RedirectAttributes redirectAttrs) {
		// check boardname
		Group group = service.getGroup(boardName);
		if(group == null)
			return "redirect:pages/errorPage404";

		String errorM = null;

		String solution = CaptchaSession.get(prefix); //prefix = session
		captcha = captcha.toUpperCase().replaceAll("O", "0");
		
		if (solution == null){
			errorM = "Captcha is expired. Try again.";
		}else if (!solution.equals(captcha)){
			errorM = "Wrong captcha answer. Try again.";
		//1message.length() == 0 or !=
		//2subject.length() == 0 or !=
		//3file.getSize() == 0 or !=
		//( 1 AND (2 OR 3) ) OR (2 AND 3)
		}else if ((message.length() == 0 && ( subject.length() == 0 || file.getSize() == 0)) 
			|| (subject.length() == 0 && file.getSize() == 0)) {
			errorM = "Don't flood please.";

		//} else if (file.getSize() != 0 && !file.getContentType().substring(0, 6).equals("image/")) {
			//errorM = "Wrong file format!";
		} else if(file != null && file.getSize() > Config.inst().get(Config.ARTICLE_MAXSIZE, 1)*1024*1024){
			errorM = "Your file is arger than limit "+Config.inst().get(Config.ARTICLE_MAXSIZE, 1)*1024*1024+" MB.";
		} else {
			if (file.getSize() == 0)
				file = null;
			
			if (service.createArticle(null, group, name,
					subject, message, file) == 0) { //we do not escape html at input
				errorM = "Thread already exist or another error.";
			}
		}
		
		if (errorM != null){
			redirectAttrs.addFlashAttribute("error", new Capsule(errorM))
			.addFlashAttribute("name", new Capsule(name))
			.addFlashAttribute("subject", new Capsule(subject))
			.addFlashAttribute("message", new Capsule(message));
		}
		return "redirect:/{boardName}/0";
	}

	// create replay
	@RequestMapping(value = "/post/{boardName}/thread-{threadId_hex:[0-9a-fA-F]*}", method = RequestMethod.POST)
	public String postReplay(@PathVariable final String boardName, @PathVariable final String threadId_hex,
			@RequestParam("name") String name, @RequestParam("subject") String subject,
			@RequestParam("message") String message,
			@RequestParam(value = "attachment",	required = false) MultipartFile file,
			@RequestParam("session") String prefix,	@RequestParam("captcha") String captcha,
			RedirectAttributes redirectAttrs) {
		int threadId = Integer.parseInt(threadId_hex, 16);
		// проверка существования такой группы
		// check boardname
		Group group = service.getGroup(boardName);
		if(group == null)
			return "redirect:pages/errorPage404";

		String errorM = null;
		int id = 0;
		
		//Check rLeft count
		int count = service.getReplaysCount(threadId);
		if (count == -1)
			return "pages/errorPage404";
		else if (service.getReplaysCount(threadId) >
		Config.inst().get(Config.MAX_REPLAYS, 500))
			errorM = "Thread has reached replays limit.";
		else{

			String solution = CaptchaSession.get(prefix); //prefix = session
			captcha = captcha.toUpperCase().replaceAll("O", "0");

			if (solution == null){
				errorM = "Captcha is expired. Try again.";
			}else if (!solution.equals(captcha)){
				errorM = "Wrong captcha answer. Try again.";

				/*if(name.isEmpty())
					name = null;
				if(subject.isEmpty())
					subject = null;
				if(message.isEmpty())
					message = null;
				if(file.getSize() == 0)
					file = null;*/
				//1message.length() == 0
				//2subject.length() == 0
				//3file.getSize() == 0
				//( 1 or (2 and 3) ) or (2 AND 3)
				//( 1 or (2 and 3) )
				//- ( 1 and (2 or 3) )
			}else if (message.length() == 0 && ( subject.length() == 0 || file.getSize() == 0  )) {
				errorM = "Don't flood please.";

			//} else if (file.getSize() != 0 && !file.getContentType().substring(0, 6).equals("image/")) {
				//errorM = "Wrong file format!";

			} else if(file != null && file.getSize() > Config.inst().get(Config.ARTICLE_MAXSIZE, 1)*1024*1024){
				errorM = "Your file is arger than limit "+Config.inst().get(Config.ARTICLE_MAXSIZE, 1)*1024*1024+" MB.";
			} else{
				if (file != null && file.getSize() == 0)
					file = null;
				id = service.createArticle(threadId, group, name,
						subject, message, file); //we do not escape html at input
				if (id == 0) {
					errorM = "Replay already exist or another error.";
				}
			}
		}
		if (errorM != null){
			redirectAttrs.addFlashAttribute("error", new Capsule(errorM))
			.addFlashAttribute("name", new Capsule(name))
			.addFlashAttribute("subject", new Capsule(subject))
			.addFlashAttribute("message", new Capsule(message));
			return "redirect:/{boardName}/thread-{threadId_hex}";
		}else
			return "redirect:/{boardName}/thread-{threadId_hex}#"+String.format("%X", id);
	}

	// Show thread
	// TODO: convert threadId to hex for links
	@RequestMapping(value = "/{boardName}/thread-{threadId_hex:[0-9a-fA-F]*}")
	public String getThread(@PathVariable final String boardName, @PathVariable final String threadId_hex,
			Map<String, Object> model) {
		int threadId = Integer.parseInt(threadId_hex, 16);
		// check boardname
		Group group = service.getGroup(boardName);
		if(group == null)
			return "redirect:pages/errorPage404";
		
		// check threadId
		List<ArticleWeb> t  = service.getOneThread(threadId, group);
		if (t.isEmpty()){
			Log.get().log(Level.INFO, "No such thread: {0} of group {1}", new Object[]{threadId, group});
			return "redirect:pages/errorPage404";
		}
		ArticleWeb tmain = t.remove(0);

		// header
		model.put("boardName", boardName);
		model.put("hostName", Config.inst().get(Config.HOSTNAME, null));
		// form
		model.put("post_url", "/post/" + boardName + "/thread-" + threadId_hex);
		model.put("reference", "");
		model.put("button", "Replay");
		model.put("files", true);
		
		//prefix for captcha
		model.put("prefix", CaptchaSession.generate()); //prefix = session
		//model.put("prefix", "");

		model.put("thread", tmain);
		model.put("replays", t);
		
		if (t.size() > Config.inst().get(Config.MAX_REPLAYS, 500))
			model.put("error", new Capsule("Thread has reached rLeft limit."));

		return "thread";
	}
	
}

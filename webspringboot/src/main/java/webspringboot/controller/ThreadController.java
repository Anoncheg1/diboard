package webspringboot.controller;

import webspringboot.entity.ArticleWeb;
import webspringboot.service.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import dibd.config.Config;
import dibd.storage.GroupsProvider.Group;
import dibd.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	
	int size = Config.inst().get(Config.MAX_ARTICLE_SIZE, 1);
	
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
			return "404";

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

		}else if (message.length() > Config.inst().get(Config.MAX_MESSAGE_SIZE, 8192) ) {
			errorM = "Message body is too big.";
			
		} else if(file != null && file.getSize() > Config.inst().get(Config.MAX_ARTICLE_SIZE, 1)*1024*1024){
			errorM = "Your file is arger than limit "+Config.inst().get(Config.MAX_ARTICLE_SIZE, 1)+" MB.";
		} else {
			if (file.getSize() == 0)
				file = null;
			
			if (service.createArticle(null, group, name,
					subject, message, file) == -1) { //we do not escape html at input
				errorM = "Thread already exist or another error.";
			}
		}
		
		if (errorM != null){
			redirectAttrs.addFlashAttribute("error", new Capsule(errorM))
			.addFlashAttribute("name", new Capsule(name))
			.addFlashAttribute("subject", new Capsule(subject))
			.addFlashAttribute("message", new Capsule(message));
			return "redirect:/{boardName}/captcha";
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
			return "404";

		String errorM = null;
		int id = 0;
		
		//Check rLeft count
		int count = service.getReplaysCount(threadId);
		if (count == -1)
			return "404";
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
				
			}else if (message.length() > Config.inst().get(Config.MAX_MESSAGE_SIZE, 8192) ) {
				errorM = "Message body is too big.";

			} else if(file != null && file.getSize() > Config.inst().get(Config.MAX_ARTICLE_SIZE, 1)*1024*1024){
				errorM = "Your file is arger than limit "+Config.inst().get(Config.MAX_ARTICLE_SIZE, 1)*1024*1024+" MB.";
			} else{
				if (file != null && file.getSize() == 0)
					file = null;
				id = service.createArticle(threadId, group, name,
						subject, message, file); //we do not escape html at input
				if (id == -1) {
					errorM = "Replay already exist or another error.";
				}
			}
		}
		if (errorM != null){
			redirectAttrs.addFlashAttribute("error", new Capsule(errorM))
			.addFlashAttribute("name", new Capsule(name))
			.addFlashAttribute("subject", new Capsule(subject))
			.addFlashAttribute("message", new Capsule(message));
			return "redirect:/{boardName}/captcha-thread-{threadId_hex}";
		}else
			return "redirect:/{boardName}/thread-{threadId_hex}#"+String.format("%X", id);
	}

	// Show thread
	// # symbol is not recognized, that is why we repeat #HEX as !HEX#HEX
	//?{replayId_hex:[0-9a-fA-F]*} params = {"id"},
	@RequestMapping(value = "/{boardName}/thread-{threadId_hex:[0-9a-fA-F]*}", method = RequestMethod.GET)
	public String getThread(HttpServletRequest request, @PathVariable final String boardName, 
			@PathVariable final String threadId_hex, //@PathVariable final String replayId_hex, //// 
			Map<String, Object> model) {
		
		int threadId = Integer.parseInt(threadId_hex, 16);
		// check boardname
		Group group = service.getGroup(boardName);
		if(group == null)
			return "404";
		
		// check threadId
		List<ArticleWeb> t  = service.getOneThread(threadId, group);
		if (t.isEmpty()){
			Log.get().log(Level.INFO, "No such thread: {0} of group {1}", new Object[]{threadId, group.getName()});
			return "404";
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
		model.put("size", this.size);

		model.put("thread", tmain);
		model.put("replays", t);
		
		if (t.size() > Config.inst().get(Config.MAX_REPLAYS, 500))
			model.put("error", new Capsule("Thread has reached rLeft limit."));

		return "thread";
	}
	
		// Show thread separate form
		// # symbol is not recognized, that is why we repeat #HEX as !HEX#HEX
		//?{replayId_hex:[0-9a-fA-F]*} params = {"id"},
		@RequestMapping(value = "/{boardName}/captcha-thread-{threadId_hex:[0-9a-fA-F]*}", params = {"id"}, method = RequestMethod.GET)
		public String getThreadCaptcha(HttpServletRequest request, @PathVariable final String boardName, 
				@PathVariable final String threadId_hex, @RequestParam("id") String id,//@PathVariable final String replayId_hex, //// 
				Map<String, Object> model) {
			String replayId_hex = id;
			
			//int threadId = Integer.parseInt(threadId_hex, 16);
			// check boardname
			Group group = service.getGroup(boardName);
			if(group == null)
				return "404";
			
			// TODO: check threadId !!!!!!!!!!
			

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
			model.put("size", this.size);

			model.put("message", new Capsule(">>"+replayId_hex));
			
			return "threadsepcaptcha";
		}
		
		
		//Redirect is not working we must create method without message injection! for redirect to here from "error in creating replay".
		@RequestMapping(value = "/{boardName}/captcha-thread-{threadId_hex:[0-9a-fA-F]*}", method = RequestMethod.GET)
		public String getThreadCaptcha(HttpServletRequest request, @PathVariable final String boardName, 
				@PathVariable final String threadId_hex,// @RequestParam("id") String id,//@PathVariable final String replayId_hex, //// 
				Map<String, Object> model) {
			//String replayId_hex = id;
			
			//int threadId = Integer.parseInt(threadId_hex, 16);
			// check boardname
			Group group = service.getGroup(boardName);
			if(group == null)
				return "404";
			
			// TODO: check threadId !!!!!!!!!!
			

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
			model.put("size", this.size);

			//model.put("message", new Capsule(">>"+replayId_hex));
			
			return "threadsepcaptcha";
		}
		
	
}

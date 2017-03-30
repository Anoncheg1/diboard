/**
 * 
 */
package webspringboot.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import dibd.storage.StorageManager;
import dibd.storage.article.Article;
import dibd.storage.web.WebRef;
import org.springframework.web.util.HtmlUtils;

/**
 * Article with HTML getters.
 * 
 * @author user
 *
 */
public class ArticleWeb extends Article{
	
	private static final SimpleDateFormat webdate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy"); 
	/**
	 * When we get threads(for board) and one thread from database.
	 * 
	 * 
	 * @param article
	 * @param refs
	 */
	public ArticleWeb(Article article, Map <String, WebRef> refs) {
		super(article);
		
		if(super.a.a_name == null || super.a.a_name.equals(""))
			super.a.a_name = "Anonymous"; //we add it only for output
		
		if (super.a.subject == null)
			super.a.subject = "";

		if (super.a.message == null)
			super.a.message = "";
		else{
			super.a.message = HtmlUtils.htmlEscape(super.a.message); // IMPORTANT for security
			//System.out.println(HtmlUtils.htmlEscape("<as.ggd@ho-st.com>"));
			
			for(Map.Entry<String, WebRef> ref : refs.entrySet()){
				WebRef wr = (WebRef) ref.getValue();

				String refW = "<a href=\"thread-"+wr.getThread_id_hex()+"#"+wr.getReplay_id_hex()+"\">&gt;&gt;"+wr.getReplay_id_hex()+"</a>";

				super.a.message = super.a.message.replace(HtmlUtils.htmlEscape(ref.getKey()), refW);
			}
			
			//new lines to HTML
			//super.a.message = super.a.message.replaceAll("\n", "<br />"); //replaced with <pre> tag
		}
	}

	public String getId_hex() {
		return String.format("%X", super.a.id);
	}

	/**
	 * "dd.MM.yyyy HH:mm:ss"
	 * 
	 * @return String
	 */
	public String getPost_time_web() {
		Date d = new Date(super.a.post_time * 1000);
		return webdate.format(d);
	}

	public String getAttachment() {
		String fileName = super.a.fileName;
		//System.out.println(fileName + super.getGroupName());
		assert(super.a.groupName != null);
		if(fileName == null)
			return "<td></td>";
		else{
			return "<td class=\"attachments\" ><a href=\"/img/"+super.a.groupName+"/"+fileName
					+"\" title=\""+ fileName +"\" target=\"_blank\"><img src=\"/thm/"
					+super.a.groupName+"/"+StorageManager.attachments.checkSupported(fileName)
					+"\" alt=\""+StorageManager.attachments.checkSupported(fileName)+"\" class=\"thumbnail\"/></a></td>";
		}

	}

	public String getHref() {
		//TODO:String.format("%X", super.getId()); format hex
		return "thread-"+String.format("%X", super.getThread_id());
	}
	
}
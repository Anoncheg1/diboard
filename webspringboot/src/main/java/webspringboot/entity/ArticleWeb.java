/**
 * 
 */
package webspringboot.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import dibd.storage.AttachmentProvider;
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
			super.a.a_name = "Anonymous";
		
		if (super.a.subject == null)
			super.a.subject = "";

		if (super.a.message == null)
			super.a.message = "";
		else{
			super.a.message = HtmlUtils.htmlEscape(super.a.message); // IMPORTANT
			//System.out.println(HtmlUtils.htmlEscape("<as.ggd@ho-st.com>"));
			
			for(Map.Entry<String, WebRef> ref : refs.entrySet()){
				WebRef wr = (WebRef) ref.getValue();

				String refW = "<a href=\"thread-"+wr.getThread_id()+"#"+wr.getReplay_id_hex()+"\">&gt;&gt;"+wr.getReplay_id_hex()+"</a>";

				super.a.message = super.a.message.replace(HtmlUtils.htmlEscape(ref.getKey()), refW);
			}
		}
	}

	public String getId_hex() {
		return String.format("%X", super.getId());
	}

	/**
	 * "dd.MM.yyyy HH:mm:ss"
	 * 
	 * @return String
	 */
	public String getPost_time_web() {
		Date d = new Date(super.getPost_time()*1000);
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(d);
	}

	public String getAttachment() {
		String fileName = super.getFileName();
		//System.out.println(fileName + super.getGroupName());
		if(fileName == null || super.getGroupName() == null)
			return "<td></td>";
		else{
			return "<td class=\"attachments\" ><a href=\"/img/"+super.getGroupName()+"/"+fileName
					+"\" title=\""+ fileName +"\" target=\"_blank\"><img src=\"/thm/"
					+super.getGroupName()+"/"+StorageManager.attachments.checkSupported(fileName)
					+"\" alt=\""+StorageManager.attachments.checkSupported(fileName)+"\" class=\"thumbnail\"/></a></td>";
		}

	}

	public String getHref() {
		//TODO:String.format("%X", super.getId()); format hex
		return "thread-"+String.format("%X", super.getThread_id());
	}
	
	//public String getMessage() - must be escaped!!!
}
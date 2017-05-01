/**
 * 
 */
package webspringboot.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import dibd.storage.StorageManager;
import dibd.storage.article.Article;
import dibd.storage.article.ArticleOutput;
import dibd.storage.web.WebRef;
import org.springframework.web.util.HtmlUtils;

/**
 * Article with HTML getters.
 * 
 * @author user
 *
 */
public class ArticleWeb extends Article{
	
	// web-fronted data format
	private static final SimpleDateFormat webdate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
	
	
	/**
	 * In thread when we click n. FFF we will add link to it.
	 * In board we just go the the thread and id of article.
	 *
	 */
	public enum oneMany{OneThread, Board};
	
	private oneMany om;
	/**
	 * When we get threads(for board) and one thread from database.
	 * 
	 * 
	 * @param article
	 * @param refs
	 */
	public ArticleWeb(ArticleOutput article, Map <String, WebRef> refs, oneMany om) {
		super(article);

		this.om = om;
		
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

				String rhex = wr.getReplay_id_hex();
				String refW = "<a href=\"thread-"+wr.getThread_id_hex()+"#"+rhex+"\">&gt;&gt;"+rhex+"</a>";

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
			String fixedfn = StorageManager.attachments.checkSupported(fileName);
			return "<td class=\"attachments\" ><a href=\"/img/"+super.a.groupName+"/"+fileName
					+"\" title=\""+ fileName +"\" target=\"_blank\"><img src=\"/thm/"
					+super.a.groupName+"/"+fixedfn
					+"\" alt=\""+fixedfn+"\" class=\"thumbnail\"/></a></td>";
		}

	}

	public String getHref() {
		if (om.equals(oneMany.Board)){
			return "thread-"+String.format("%X", super.getThread_id())+"#"+getId_hex();
		}else
			return "captcha-thread-"+String.format("%X", super.getThread_id())+"?id="+getId_hex();
	}
	
	public String getA_name() {
		return a.a_name.replaceFirst("<.*", "").trim();
	}
	
}
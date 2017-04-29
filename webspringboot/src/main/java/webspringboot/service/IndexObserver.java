package webspringboot.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.springframework.web.util.HtmlUtils;

import dibd.config.Config;
import dibd.storage.GroupsProvider.Group;
import dibd.storage.StorageManager;
import dibd.storage.AttachmentProvider.Atype;
import dibd.storage.article.ArticleForOverview;
import dibd.util.io.Resource;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;



/**
 * generate index cache file
 * 
 * one Observable thread(may user storage)
 * 
 * @author user
 *
 */

public class IndexObserver implements Observer {

	String must;
	Map<String, String> grs = new LinkedHashMap<>();
	
	List<String> networking_groups = new ArrayList<>();
	
	File ifile = new File("index.html");
	
	public IndexObserver(){
		//boards at top
		for(String gn :StorageManager.groups.getAllNames())
			grs.put(gn, gn.replaceFirst("overchan.", ".."));
		
		must = Resource.getAsString("src/main/resources/templates/index.mustache", true);
		
		Map<String, String> net_groups = new HashMap<>();
		
		for(Group g : StorageManager.groups.getAll()){  //we group together groups with equal peers for show 
			Iterator<String> it = g.getHosts().iterator();
			if (it.hasNext() && !g.isDeleted()){
				//group name
				StringBuilder sb = new StringBuilder(": ");
				
				//build string of hosts
				sb.append(it.next());
				while(it.hasNext()){
					sb.append(", ").append(it.next());
				}
				String st = sb.toString();//net of group
				
				if (net_groups.containsKey(st)){ //net exist
					String groups = net_groups.get(st);
					net_groups.replace(st, groups +", "+g.getName().replaceFirst("overchan.", "o.")); //new groups for net
				}else//new net and group
					net_groups.put(st, g.getName().replaceFirst("overchan.", "o."));
			}
		}
		for(Entry<String, String> ng : net_groups.entrySet())
			networking_groups.add(ng.getValue()+ng.getKey());
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		@SuppressWarnings("unchecked")
		List<ArticleForOverview> arts = (List<ArticleForOverview>) arg;
		Map<String, Object> model = new HashMap<>();
		//compile
		Template tmpl = Mustache.compiler().compile(must);
		
		model.put("boards_names", grs.entrySet());
		model.put("message", "Welcome to <h1>"+Config.inst().get(Config.HOSTNAME, null)+"</h1> diboard "+dibd.App.VERSION+".<br />");
		
		//statistic
		Set<String> lastgr = new LinkedHashSet<>(); //unique groups
		for( ArticleForOverview a: arts)
			lastgr.add(a.getGroupName());
		
		class groupNew{ //group with images or messages
			@SuppressWarnings("unused")
			public String gname; //group name
			@SuppressWarnings("unused")
			public List<String> articles; //cells of group
			groupNew(String gname, List<String> articles){
				this.gname = gname;
				this.articles = articles;
			}
		}
		
		List<groupNew> groups = new ArrayList<>();  
		
		for (String gr: lastgr){
			List<String> articles = new ArrayList<>(); //cell 
			for( ArticleForOverview a: arts){
				String gname = a.getGroupName();
				if ( articles.size() < 4 && gname.equals(gr)){
					
					String img = null;
					String id = String.format("%X", a.getId());
					String thid = String.format("%X", a.getThread_id());
					if(a.getStatus() == 0 && a.getFileName() != null && StorageManager.attachments.getPath(gname, a.getFileName(), Atype.thm).exists()){
						String fixedfn = StorageManager.attachments.checkSupported(a.getFileName());
						img = "<a href=\"/"+gname+"/thread-"+thid+"#"+id+"\"><img src=\"/thm/"
								+gname+"/"+fixedfn
								+"\" alt=\""+fixedfn+"\" class=\"thumbnail\"/></a>";
						articles.add(img);
					}else{
						String rstr = new String();
						String sub = a.getSubject();
						String mes = a.getMessage();
						if (mes != null && ! mes.isEmpty())
							mes = mes.replaceAll("<[^>]+>", "").replaceAll("\n", "").trim();
						
						//subject
						if (sub != null && ! sub.equalsIgnoreCase("none")){
							int len = sub.length();
							if (mes == null || mes.isEmpty())
								sub = sub.substring(0, len<60 ? len : 60);
							else
								sub = sub.substring(0, len<30 ? len : 30);
							
							sub = HtmlUtils.htmlEscape(sub); //important for security
							rstr += "<a href=/" + gname + "/thread-"+ thid +"#"+id+">S:"+sub+"</a>";
						}
						
						//message
						if (mes != null && ! mes.isEmpty()){
							mes = mes.replaceAll("<[^>]@[^>]>", "");
							int len = mes.length();
							if (rstr == null || rstr.isEmpty()){
								mes = mes.substring(0, len<60 ? len : 60);
								mes = HtmlUtils.htmlEscape(mes); //important for security
								rstr += "<a href=/" + gname + "/thread-"+ thid +"#"+id+">M:"+mes+"</a>";
							}else{
								mes = mes.substring(0, len<30 ? len : 30);
								mes = HtmlUtils.htmlEscape(mes); //important for security
								rstr += "<br />M:"+mes;
							}
						}
						if (rstr != null && !rstr.isEmpty())
							articles.add(rstr);
					}
				}
			}
					
			
			groups.add(new groupNew(gr.replaceFirst("overchan.", "o."), articles));
		}
		
		model.put("groups", groups);
		
		
		
		
		//net
		model.put("groups_network1","<p>Networking groups here:</p>");
		model.put("groups_network2", networking_groups); //IMPORTANT TO BE PUBLIC
		
		//execute
		String res = tmpl.execute(model);

		//save
		FileOutputStream fos = null;
		try {
			File tmp = File.createTempFile("index", "html");

			fos = new FileOutputStream(tmp);
			fos.write(res.getBytes(StandardCharsets.UTF_8));
			fos.close();
			Files.move(tmp.toPath(), ifile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {}
		}
		
	}
	
	
	
}
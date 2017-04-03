package webspringboot.controller;

import java.awt.image.BufferedImage;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dibd.storage.AttachmentProvider.Atype;
import dibd.storage.StorageManager;
import webspringboot.service.CaptchaGen;
import webspringboot.service.CaptchaSession;

@RestController
public class ImageController {
	
	@RequestMapping("/{what:[a-z]{3}}/{boardName}/{fileName:.+}")
	public void file(HttpServletRequest req, HttpServletResponse resp, 
			@PathVariable final String what, @PathVariable final String boardName, @PathVariable final String fileName) {
		//check existence of what?
		Atype type = Atype.valueOf(what);
		
		//File ofile = new File("attachments/" + boardName + "/"+what+"/" + fileName);
		File ofile = StorageManager.attachments.getPath(boardName, fileName, type);
		if(ofile.exists()){
			try {
				FileInputStream fos = new FileInputStream (ofile);
				OutputStream or = null;
			    try {
			    	if (type.equals(Atype.img) && 
			    			! StorageManager.attachments.getPath(boardName, fileName, Atype.thm).exists())
			    		resp.setContentType("dont/open");
			    	or = resp.getOutputStream();
			    	//BufferedOutputStream bo = new BufferedOutputStream(resp.getOutputStream());
			    	byte[] bs = new byte[100];
			    	int n = 0;
			    	while((n = fos.read(bs)) != -1){
			    		or.write(bs, 0, n);
			    	}
			    	or.flush();
			    	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			//TODO:error
			try {
				resp.sendError(404);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	
	
	/**
	 * Captcha for every thread, board in formPartial
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping("/{boardName}/captcha-{prefix:[0-9|A-Z|a-z]*}")
	public void captcha(@PathVariable final String prefix, HttpServletRequest req, HttpServletResponse resp) {
		CaptchaGen cgen = new CaptchaGen(CaptchaGen.ALL_ENGLISH_CHARS_AND_NUMBERS, 120, 25, 23, 5, 1, 2, 40, 20, 60, 20);
		StringBuilder solraw = new StringBuilder();
		BufferedImage img = cgen.generate(solraw);
		
		String solution = solraw.toString().toUpperCase().replaceAll("O", "0");
		CaptchaSession.put(prefix, solution);
		
		
		try {
			OutputStream or = resp.getOutputStream();
			ImageIO.write(img, "png", or);
			
		} catch (ClientAbortException e) {
			//shit happens
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	
		
	@RequestMapping("foo")
	public String foo() {
		throw new RuntimeException("Expected exception in controller");
	}
}

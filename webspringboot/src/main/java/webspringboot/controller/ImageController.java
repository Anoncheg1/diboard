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

import webspringboot.service.CaptchaGen;
import webspringboot.service.CaptchaSession;

@RestController
public class ImageController {
	
	@RequestMapping("/{what:[a-z]{3}}/{boardName}/{fileName:.+}")
	public void file(HttpServletRequest req, HttpServletResponse resp, 
			@PathVariable final String what, @PathVariable final String boardName, @PathVariable final String fileName) {
		//File ofile = new File(Config.inst().get(Config.ATTACHMENTSPATH, "attachments/") + boardName + "/"+what+"/" + fileName+"."+ext);
		File ofile = new File("attachments/" + boardName + "/"+what+"/" + fileName);
		if(ofile.exists()){
			try {
				FileInputStream fos = new FileInputStream (ofile);
				
			    try {
			    	if (what.equals("img") && 
			    			! new File("attachments/" + boardName + "/"+"thm"+"/" + fileName).exists())
			    		resp.setContentType("dont/open");
			    	OutputStream or = resp.getOutputStream();
			    	//BufferedOutputStream bo = new BufferedOutputStream(resp.getOutputStream());
			    	byte[] bs = new byte[100];
			    	int n = 0;
			    	while((n = fos.read(bs)) != -1){
			    		or.write(bs, 0, n);
			    	}
			    	fos.close();
			    	or.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		CaptchaGen cgen = new CaptchaGen(CaptchaGen.ALL_ENGLISH_CHARS_AND_NUMBERS, 150, 30, 23, 5, 1, 2, 40, 20, 60, 20);
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

package webspringboot.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

	/*public CustomErrorController() {
		// TODO Auto-generated constructor stub
	}*/

	private static final String PATH = "/error";
	
	@RequestMapping(value=PATH)
    public String error() {
        return "Error heaven";
    }

	
	@Override
	public String getErrorPath() {
		// TODO Auto-generated method stub
		return PATH;
	}

}

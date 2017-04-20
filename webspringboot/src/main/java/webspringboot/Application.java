package webspringboot;


import dibd.App;
import dibd.storage.web.ObservableDatabase;
import webspringboot.service.IndexObserver;

import java.util.Observable;
import java.util.Observer;

/*import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
*/
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


//@ImportResource("classpath:/WEB-INF/NewFile.xml")
//@RestController
//@Controller

@Configuration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan
public class Application implements Observer{
	
	//@Bean
	//private webspringboot.mvc.controllers.MyController a; 
		/*
	@RequestMapping("/")
	String home() {
        return "Hello World!";
    }
	*/
	/*
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
	    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
	    tomcat.addAdditionalTomcatConnectors(createSslConnector());
	    return tomcat;
	}

	private Connector createSslConnector() {
	    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
	    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
	    try {
	        File keystore = new ClassPathResource("keystore").getFile();
	        File truststore = new ClassPathResource("keystore").getFile();
	        connector.setScheme("https");
	        connector.setSecure(true);
	        connector.setPort(8443);
	        protocol.setSSLEnabled(true);
	        protocol.setKeystoreFile(keystore.getAbsolutePath());
	        protocol.setKeystorePass("changeit");
	        protocol.setTruststoreFile(truststore.getAbsolutePath());
	        protocol.setTruststorePass("changeit");
	        protocol.setKeyAlias("apitester");
	        return connector;
	    }
	    catch (IOException ex) {
	        throw new IllegalStateException("can't access keystore: [" + "keystore"
	                + "] or truststore: [" + "keystore" + "]", ex);
	    }
	}
	
	*/
	
	
		
    public static void main(String[] args) {
       //ApplicationContext ctx = 
    	SpringApplication.run(Application.class, args);
    	
    	
    	
    	
       /*
        System.out.println("Let's inspect the beans provided by Spring Boot:");
        
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
        
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
        	System.out.println(url.getFile());
        }*/
    	
    	App.setObserver(new Application());
    	try {
    		App.main(args);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
       
       
       
    }

	@Override
	public void update(Observable o, Object arg) {
		ObservableDatabase.inst().addObserver(new IndexObserver());
	}

}
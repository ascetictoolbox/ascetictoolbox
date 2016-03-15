package es.bsc.demiurge.ws.gui.controllers;

import es.bsc.demiurge.core.configuration.Config;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Controller
public class IndexController {
	@RequestMapping({"/", "/index"})
	public String showIndex(HttpServletRequest request) {
		return "index";
	}

	@RequestMapping("/test")
	public String showTest(Model model) {
		return "test";
	}
}

package es.bsc.demiurge.ws.gui.controllers;

import es.bsc.demiurge.core.configuration.Config;
import org.apache.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Controller
public class UserInfoController {

	@RequestMapping(method = {RequestMethod.GET}, path = {"/users"})
	public String getPasswordForm(Model model) {
		return "users";
	}

	@RequestMapping(method = {RequestMethod.POST}, path = {"/users"})
	public String getPasswordForm(@RequestParam("user") String user,
								  @RequestParam("oldpwd") String oldpwd,
								  @RequestParam("newpwd1") String newpwd1,
								  @RequestParam("newpwd2") String newpwd2,
								  Model model) {
		if(!newpwd1.equals(newpwd2)) {
			model.addAttribute("error","Both Passwords must coincide");
			return "users";
		} else if(newpwd1.equalsIgnoreCase("changeme")) {
			model.addAttribute("error","Unallowed password");
			return "users";
		} else if(!Config.INSTANCE.getVmManager().getDB().getUserDao().checkUser(user,oldpwd)) {
			model.addAttribute("error", "Invalid user/password");
			return "users";
		}
		try {
			Config.INSTANCE.getVmManager().getDB().getUserDao().updateUserPassword(user,newpwd1);
		} catch(Exception e) {
			LogManager.getLogger(UserInfoController.class).error(e.getMessage(),e);
			model.addAttribute("error", e.getMessage());
			return "users";
		}
		return "redirect:/gui/index";
	}

}

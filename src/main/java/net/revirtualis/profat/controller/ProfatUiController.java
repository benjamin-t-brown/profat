package net.revirtualis.profat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Serves the bundled Profat API tester ({@code classpath:/static/index.html}) at {@code /profat/...}
 * so it can be exposed behind nginx without colliding with {@code /proxy/{route}/...} static sites.
 */
@Controller
public class ProfatUiController {

	@RequestMapping(
			value = {"/profat", "/profat/", "/profat/**"},
			method = {RequestMethod.GET, RequestMethod.HEAD})
	public String profatUi() {
		return "forward:/index.html";
	}
}

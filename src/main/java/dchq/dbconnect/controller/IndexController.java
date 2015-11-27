package dchq.dbconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @since 11/22/2015
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @RequestMapping
    public String getIndexPage() {
        return "index";
    }
}

package com.will.compairator.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Browser entry point; AI operations remain owned by the existing REST API. */
@Controller
public class WebController {

    @GetMapping({"/admin/stats", "/admin/stats/"})
    public String statistics() {
        return "forward:/compairator-ui/admin.html";
    }

    @GetMapping({"/", "/web", "/web/"})
    public String index() {
        return "forward:/compairator-ui/index.html";
    }
}

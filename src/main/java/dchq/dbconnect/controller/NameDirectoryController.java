package dchq.dbconnect.controller;

import dchq.dbconnect.model.NameDirectory;
import dchq.dbconnect.service.NameDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @since 11/22/2015
 */
@Controller
@RequestMapping("/names")
public class NameDirectoryController {
    @Autowired
    private NameDirectoryService nameDirectoryService;

    @RequestMapping("/getList.json")
    @ResponseBody
    public List<NameDirectory> getCarList() {
        return nameDirectoryService.getAllRows();
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public void add(@RequestBody NameDirectory nd) {
        nameDirectoryService.addNameDirectory(nd);
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void remove(@PathVariable("id") Long id) {
        nameDirectoryService.deleteNameDirectoryById(id);
    }

    @RequestMapping(value = "/removeAll", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeAll() {
        nameDirectoryService.deleteAll();
    }

    @RequestMapping("/layout")
    public String getCarPartialPage() {
        return "namedirectory/layout";
    }
}

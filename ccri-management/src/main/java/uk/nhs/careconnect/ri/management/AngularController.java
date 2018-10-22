package uk.nhs.careconnect.ri.management;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AngularController {

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Object displayCustomers() {
        System.out.println("search DETECTED");
        return new ModelAndView
                ("redirect:/index.html" );
    }



    @RequestMapping(value = "/ed", method = RequestMethod.GET)
    public Object handleAmb() {
        System.out.println("Amb base");
        return new ModelAndView
                ("forward:/" );
    }

    @RequestMapping(value = "/ed/{path}", method = RequestMethod.GET)
    public Object handleSubPath() {
        System.out.println("handleSubPath");
        return new ModelAndView
                ("forward:/" );
    }

    @RequestMapping(value = "/ed/{path}/{patient}", method = RequestMethod.GET)
    public Object handlePatient() {
        System.out.println("handlePatient");
        return new ModelAndView
                ("forward:/" );
    }

    @RequestMapping(value = "/exp", method = RequestMethod.GET)
    public Object displayED() {
        System.out.println("exp Base");
        return new ModelAndView
                ("forward:/" );
    }

    @RequestMapping(value = "/exp/{subpath}", method = RequestMethod.GET)
    public Object edSubPath() {
        System.out.println("exp SubPath");
        return new ModelAndView
                ("forward:/" );
    }

    @RequestMapping(value = "/exp/resource/{resource}", method = RequestMethod.GET)
    public Object displayResource() {
        System.out.println("exp resource");
        return new ModelAndView
                ("forward:/" );
    }

}

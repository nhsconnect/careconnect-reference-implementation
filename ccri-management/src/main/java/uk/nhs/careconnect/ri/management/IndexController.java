package uk.nhs.careconnect.ri.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class IndexController implements ErrorController {

    private static final String PATH = "/error";

    private ErrorAttributes errorAttributes;

    @Autowired
    public void SimpleErrorController(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equals(parameter.toLowerCase());
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    @RequestMapping(value = PATH)
    public Object error(HttpServletRequest aRequest){
        System.out.println(aRequest.getRequestURI());

        Map<String, Object> body = getErrorAttributes(aRequest,getTraceParameter(aRequest));
        String trace = (String) body.get("trace");
        if(trace != null){
            System.out.println("trace not null");
            String[] lines = trace.split("\n\t");
            body.put("trace", lines);
            for (String line : lines) {
                System.out.println(line);
            }
        }
/*
        if (aRequest.getRequestURI().contains("/error")) {
            return new ModelAndView
                    ("redirect:/");
        }
*/
        return "An error has occured.";
    }



    @Override
    public String getErrorPath() {
        return PATH;
    }
}

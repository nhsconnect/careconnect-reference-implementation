package uk.nhs.careconnect.ri.fhirserver.hooks;

import cucumber.api.java.Before;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import uk.nhs.careconnect.ri.fhirserver.test.apiSteps;

import java.io.File;

public class serverHookTest {


    private static boolean dunit = false;

    private static Server ourServer;

    public final static int ourPort = 8887;

   // public final static org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(apiSteps.class);;




    @Before
    public void setupServer() throws Exception {
		/*
		 * This runs under maven, and I'm not sure how else to figure out the target directory from code..
		 */

        if (!dunit) {

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        ourServer.stop();
                    } catch (Exception ex) {
                    }

                    System.out.println("Inside Add Shutdown Hook");
                }
            });

            System.out.println("Shut Down Hook Attached.");
        //    ourLog.info("Lets Do this!");

            String path = apiSteps.class.getClassLoader().getResource("application.properties").getPath();
     //       ourLog.info("Properties Path = " + path);
            path = new File(path).getParent();
            path = new File(path).getParent();
            path = new File(path).getParent();

     //       ourLog.info("Project base path is: {}", path);

            //ourPort = RandomServerPortProvider.findFreePort();
            ourServer = new Server(ourPort);

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/");
            webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setResourceBase(path + "/target/careconnect-ris");
            webAppContext.setParentLoaderPriority(true);

            ourServer.setHandler(webAppContext);
            ourServer.start();

            dunit = true;
        }

    }


}

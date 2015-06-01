package uk.ac.susx.tag.dialoguer;

/**
 * Created by juliewe on 01/06/2015.
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Service is hosted on port passed in as first argument
 * File created by Simon Wibberley and Jeremy Reffin on 30/04/2014.
 */
public class RootServer {

    // -------------------- MAIN() --------------------

    public static void main(String[] args) throws Exception {

        Server server = new Server(Integer.parseInt(args[0])); //port number e.g. 8080

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.addServlet(new ServletHolder(new ServletContainer(new ResourceConfig()
                .packages("uk.ac.susx.tag.dialoguer")
                .property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true))), "/*");
        bb.setContextPath("/");
        bb.setWar("src/main/webapp");


        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase("static");
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});

        ContextHandler resourceContextHandler = new ContextHandler();
        resourceContextHandler.setContextPath("/static");
        resourceContextHandler.setHandler(resourceHandler);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceContextHandler);
        handlers.addHandler(bb);

//        server.setHandler(bb);
        server.setHandler(handlers);
        server.start();
        server.join();
    }

// --------------- GLOBAL VARIABLES ---------------
// -------------------- FIELDS --------------------
// ----------------- CONSTRUCTORS -----------------
// -------------- API: PUBLIC METHODS -------------
// ---------- API: STD OVERRIDE METHODS -----------
// ---------------- PRIVATE METHODS ---------------
// -------------- PROTECTED METHODS ---------------
// ---------------- STATIC METHODS ----------------
// ----------------- STATIC CLASS -----------------
// -------------------- MAIN() --------------------
}

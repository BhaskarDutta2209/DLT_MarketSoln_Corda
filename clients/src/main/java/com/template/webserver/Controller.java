package com.template.webserver;

import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {

//    private final CordaRPCOps proxy;
//    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
//
//    public Controller(NodeRPCConnection rpc) {
//        this.proxy = rpc.proxy;
//    }
//
//    @GetMapping(value = "/templateendpoint", produces = "text/plain")
//    private String templateendpoint() {
//        return "Define an endpoint here.";
//    }

    @Autowired
    private CordaRPCOps buyerProxy;

    @Autowired
    private CordaRPCOps deliveryProxy;

//    @Autowired
//    private CordaRPCOps bankProxy;

    @Autowired
    private CordaRPCOps shopProxy;

    @Autowired
    @Qualifier("buyerProxy")
    private CordaRPCOps proxy;

    @GetMapping(value = "/whoami")
    private String whoAmI() {
        return proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
    }

}
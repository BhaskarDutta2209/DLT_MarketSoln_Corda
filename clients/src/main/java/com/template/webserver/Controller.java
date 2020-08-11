package com.template.webserver;

import com.template.flows.AddItem;
import com.template.flows.CreateNewAccount;
import com.template.webserver.models.ItemModel;
import com.template.webserver.models.ShopModel;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

    @PostMapping(value = "/switch-party/{party}")
    private String switchParty(@PathVariable String party) {
        if(party.equalsIgnoreCase("shop"))
            proxy = shopProxy;
        else if(party.equalsIgnoreCase("delivery"))
            proxy = deliveryProxy;
        else if(party.equalsIgnoreCase("buyer"))
            proxy = buyerProxy;
        return proxy.nodeInfo().getLegalIdentities().get(0).getName().toString();
    }

    @PostMapping(value = "/createshopaccount")
    private String createShopAccount(@RequestBody ShopModel body) throws ExecutionException, InterruptedException {
        Party delivery = proxy.partiesFromName("Delivery",false).iterator().next();
        Party buyer = proxy.partiesFromName("Buyer",false).iterator().next();

        CordaFuture<String> st = proxy.startFlowDynamic(CreateNewAccount.class,
                body.getShopName(),
                Arrays.asList(delivery,buyer)).getReturnValue();

        if(st.get().equalsIgnoreCase("Creation Failed"))
            return "Creation Failed";
        else
            return "Success";
    }

    @PostMapping(value = "/additem")
    private ResponseEntity addItem(@RequestBody ItemModel body) {
        if(proxy.nodeInfo().getLegalIdentities().get(0).getName().getOrganisation().equalsIgnoreCase("Shop")) {
            UUID key = UUID.randomUUID();
            proxy.startFlowDynamic(AddItem.class,key,
                    body.getProductName(),
                    body.getProductDetails(),
                    body.getPrice(),
                    body.getExpiryDate(),
                    body.getQuantity(),
                    body.getBarCode(),
                    body.getShopAccountName());
            return new ResponseEntity(key.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity(null,HttpStatus.NOT_ACCEPTABLE);
        }
    }

}
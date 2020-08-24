package com.template.webserver.security;

import com.template.webserver.Controller;
import com.template.webserver.NodeRPCConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private NodeRPCConnection rpc;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Controller ctl = new Controller(rpc);
        if (ctl.checkExistance(username))
            return new User(username,"test",new ArrayList<>());
        else
            throw new UsernameNotFoundException("Username Not Found");
    }
}

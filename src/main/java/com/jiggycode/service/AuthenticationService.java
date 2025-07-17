package com.jiggycode.service;

import com.jiggycode.request.AuthenticationRequest;
import com.jiggycode.request.RegisterRequest;
import com.jiggycode.response.AuthenticationResponse;

public interface AuthenticationService {
    void register(RegisterRequest input) throws Exception;
    AuthenticationResponse login(AuthenticationRequest request);

}

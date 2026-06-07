package com.example.persona.client;

import com.example.persona.dto.response.AccountDetail;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(contentType = "application/json")
public interface CdpHttpClient {

    @GetExchange("/v3/account/detail")
    AccountDetail getAccountDetail(@RequestParam("accountNo") String accountNo);
}

package com.example.demo.rest;


import com.example.demo.dto.feign.QuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "quoteClient", url = "${quote.api.url}")
public interface QuoteClient {
    @GetMapping("/quotes/random")
    QuoteResponse getQuote();
}

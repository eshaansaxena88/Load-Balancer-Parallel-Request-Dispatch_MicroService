package com.ThreadPoolCall.LBAPIcaller.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@RestController
public class ParallelRequestController {

    private final ThreadPoolTaskExecutor taskExecutor;

    @Value("${file.url}")
    private String url;
    @Value("${file.numberOfRequests}")
    private int numRequests;
    
    @Autowired
    public ParallelRequestController(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @GetMapping("/parallel-request")
    public ResponseEntity<List<String>> executeParallelRequests() {
     //   String url = "http://example.com/api/endpoint"; 
       // int numRequests = 500;

        List<Future<String>> futures = new ArrayList<>();

        // Submit tasks to the executor for each request
        for (int i = 0; i < numRequests; i++) {
            RequestTask task = new RequestTask(url);
            Future<String> future = taskExecutor.submit(task);
            futures.add(future);
        }

        List<String> responses = new ArrayList<>();

        // Wait for all tasks to complete and retrieve the results
        for (Future<String> future : futures) {
            try {
                String response = future.get(); // This blocks until the task is complete and returns the result
                responses.add(response);
            } catch (InterruptedException | ExecutionException e) {
                // Handle any exceptions that occurred during task execution
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(responses);
    }

    private static class RequestTask implements Callable<String> {
        private final String Url;

        public RequestTask(String url) {
            this.Url = url;
        }

        @Override
        public String call() throws Exception {


       	 try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
       	        HttpGet httpGet = new HttpGet(Url);
       	        HttpResponse response = httpClient.execute(httpGet);
       	        HttpEntity entity = response.getEntity();
       	        return EntityUtils.toString(entity);
       	    } catch (IOException e) {
       	        e.printStackTrace();
       	        return "Error occurred while sending request to the server.";
       	    }
        
        }
    }
}

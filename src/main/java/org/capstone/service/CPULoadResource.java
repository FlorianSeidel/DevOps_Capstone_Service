package org.capstone.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Path("/create-load")
public class CPULoadResource {

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String createLoad() {
        List<Future<?>> futures = new LinkedList<Future<?>>();
        for(int k=0;k<4;k++) {
            futures.add(executor.submit(() -> {
                double j = 0;
                for (int i = 0; i < 100000; i++) {
                    j += Math.sqrt(i);
                }
            }));
        }
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        return "Done";
    }

}
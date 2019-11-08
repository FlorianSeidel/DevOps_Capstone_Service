package org.capstone.service;

import com.martensigwart.fakeload.*;

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
        FakeLoad fakeload = FakeLoads.create()
                .lasting(5, TimeUnit.SECONDS)
                .withCpu(80)
                .withMemory(50, MemoryUnit.MB);

        FakeLoadExecutor executor = FakeLoadExecutors.newDefaultExecutor();
        executor.execute(fakeload);
        return "Done";
    }

}
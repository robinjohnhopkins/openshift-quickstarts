package org.openshift.quickstarts.todolist.rest;


import org.openshift.quickstarts.todolist.service.Find;

import javax.json.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.logging.Logger;

@Path("rest")
@Produces(MediaType.APPLICATION_JSON)
public class NonDbRest {

    private final static Logger LOGGER = Logger.getLogger(NonDbRest.class.getName());

    @GET
    public String getAString() {
        return "boom1";
    }

    @Path("/numbers")
    @GET
    public JsonArray numbers() {
        LOGGER.fine("numbers");
        LOGGER.fine("numbers");

        JsonArrayBuilder array = Json.createArrayBuilder();
        Stream<String> numberStream = Stream.generate(System::currentTimeMillis).
                map(String::valueOf).
                limit(10);
        numberStream.forEach(array::add);
        return array.build();
    }

    @Path("/env")
    @GET
    public JsonArray env() {
        LOGGER.info("env");

        JsonArrayBuilder array = Json.createArrayBuilder();
        Map<String, String> env = System.getenv();
        // Java 8
        env.forEach((k, v) -> array.add(k + ":" + v));
        return array.build();
    }

    @Path("/files")
    @GET
    public JsonObject files(@QueryParam("dir") String dir,
                           @QueryParam("pattern") String pattern) throws IOException {

        JsonArrayBuilder array = Json.createArrayBuilder();
        if (dir == null || dir.length() == 0){
            dir = "/";
        }
        if (pattern == null || pattern.length() == 0){
            pattern = "*";
        }
        List<String> listFileStrings = new ArrayList<>();
        java.nio.file.Path startingDir = Paths.get(dir);
        Find.Finder finder = new Find.Finder(pattern, listFileStrings);
        Files.walkFileTree(startingDir, finder);
        finder.done();

        // Java 8
        listFileStrings.forEach((k) -> array.add(k));
        JsonObject object = Json.createObjectBuilder()
            .add("files",array.build())
            .build();
        return object;
    }

}

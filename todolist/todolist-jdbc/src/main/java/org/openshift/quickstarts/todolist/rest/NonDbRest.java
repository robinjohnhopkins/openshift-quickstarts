package org.openshift.quickstarts.todolist.rest;



import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Stream;

@Path("rest")
@Produces(MediaType.APPLICATION_JSON)
public class NonDbRest {

//    @Path("/task")
//    @GET
//    public Task getATask() {
//       Task task = new Task();
//       task.setId(11L);
//       task.setTitle("my tasky wask");
//       return task;
//    }

    @GET
    public String getAString() {
        return "boom1";
    }

    @Path("/numbers")
    @GET
    public JsonArray numbers() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        Stream<String> numberStream = Stream.generate(System::currentTimeMillis).
                map(String::valueOf).
                limit(10);
        numberStream.forEach(array::add);
        return array.build();
    }

}

package com.sestevez.todo;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.sestevez.todo.api.Todo;
import io.smallrye.common.annotation.Blocking;

@Path("/api")
public class AstraTODO {
    @Inject
    QuarkusCqlSession cqlSession;

    private String keyspaceName = "free";
    private String tableName = "todolist";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/todo/{list_id}")
    @Blocking
    public List<Todo> getTodos(@PathParam("list_id") String list_id) {
        PreparedStatement statement = this.cqlSession
                .prepare("SELECT * FROM " + this.keyspaceName + "." + this.tableName + " where list_id =?");

        BoundStatement bound = statement.bind(list_id);

        ResultSet rs = this.cqlSession.execute(bound);

        List<Row> rows = rs.all();
        return rows.stream()
          .map(x -> new Todo(x.getUuid("id"), x.getString("title"), x.getBoolean("completed")))
          .collect(Collectors.toList());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/todo/{list_id}")
    @Blocking
    public Response setTodo(@PathParam("list_id") String list_id, Todo todo) {
        PreparedStatement statement = this.cqlSession
                .prepare("INSERT INTO " + this.keyspaceName + "." + this.tableName + "(list_id, id, title, completed)VALUES(?,?,?,?)");

        BoundStatement bound = statement.bind(list_id, todo.getId(), todo.getTitle(), todo.isCompleted());

        ResultSet rs = this.cqlSession.execute(bound);

        boolean successful = rs.wasApplied();
        if (successful) {
            return Response.ok().build();
        }else{
            return Response.serverError().build();
        }

    }


}

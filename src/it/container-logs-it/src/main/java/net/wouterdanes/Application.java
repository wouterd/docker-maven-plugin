package net.wouterdanes;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.Guice;
import ratpack.handling.ChainAction;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;

import java.util.Date;
import java.util.stream.StreamSupport;

/**
 * Main entrypoint for this application
 */
public class Application implements HandlerFactory {

  private static final Logger log = LoggerFactory.getLogger(Application.class);
  private static final String ENV_APP_MESSAGE = "APP_MESSAGE";

  @Override
  public Handler create(final LaunchConfig launchConfig) throws Exception {
    return Guice.handler(launchConfig, bindingsSpec -> {
      String mongoHost = System.getProperty("mongo.host", "localhost");
      log.info("Mongo host = {}", mongoHost);
      DBCollection collection = new MongoClient(mongoHost)
          .getDB("discuss")
          .getCollection("posts");
      bindingsSpec.bind(DBCollection.class, collection);
    }, new Routes());
  }

  private class Routes extends ChainAction {

    @Override
    protected void execute() throws Exception {

      get("message", ctx -> ctx.render(System.getenv(ENV_APP_MESSAGE)));

      prefix("posts", (Handler) ctx -> ctx.byMethod(posts -> {
            posts.post(context -> {

              String body = context.getRequest().getBody().getText();

              DBCollection collection = context.get(DBCollection.class);

              DBObject object = new BasicDBObjectBuilder()
                  .add("dateTime", new Date())
                  .add("message", JSON.parse(body))
                  .get();

              collection.insert(object);

              context.getResponse().status(201);
              context.getResponse().send();

            });

            posts.get(context -> {

              DBObject fieldSpec = new BasicDBObjectBuilder()
                  .add("_id", 0)
                  .add("dateTime", 1)
                  .add("message", 1)
                  .get();

              DBCollection collection = context.get(DBCollection.class);
              DBCursor cursor = collection.find(new BasicDBObject(), fieldSpec).sort(new BasicDBObject("dateTime", -1));

              if (!cursor.hasNext()) {
                context.clientError(404);
                return;
              }

              String result = "[" +
                  StreamSupport.stream(cursor.spliterator(), true)
                      .map(JSON::serialize)
                      .reduce((s, s2) -> s + "," + s2)
                      .get()
                  + "]";

              context.getResponse().send("application/json", result);

            });
          }
      ));

    }
  }
}

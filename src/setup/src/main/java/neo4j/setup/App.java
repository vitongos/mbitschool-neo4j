package neo4j.setup;

import static spark.Spark.get;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	get("/hello", (req, res) -> "Setup complete!");
    }
}

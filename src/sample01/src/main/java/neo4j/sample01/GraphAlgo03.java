package neo4j.sample01;

import java.io.File;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Paths;

public class GraphAlgo03 {
	private static final String DB_PATH = "neo4j-shortest-path";
    private static final String NAME_KEY = "name";
    private static RelationshipType KNOWS = DynamicRelationshipType.withName( "KNOWS" );

    private static GraphDatabaseService graphDb;
    private static Index<Node> indexService;

    public static void main( final String[] args )
    {
        deleteFileOrDirectory( new File( DB_PATH ) );
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        registerShutdownHook();
        try ( Transaction tx = graphDb.beginTx() )
        {
        	indexService = graphDb.index().forNodes( "nodes" );
            /*
             *  (Neo) --> (Trinity)
             *     \       ^
             *      v     /
             *    (Morpheus) --> (Cypher)
             *            \         |
             *             v        v
             *            (Agent Smith)
             */
            createChain( "Neo", "Trinity" );
            createChain( "Neo", "Morpheus", "Trinity" );
            createChain( "Morpheus", "Cypher", "Agent Smith" );
            createChain( "Morpheus", "Agent Smith" );
            tx.success();
            // So let's find the shortest path between Neo and Agent Smith
            Node neo = getOrCreateNode( "Neo" );
            Node agentSmith = getOrCreateNode( "Agent Smith" );
            // START SNIPPET: shortestPathUsage
            PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
                    PathExpanders.forTypeAndDirection( KNOWS, Direction.BOTH ), 4 );
            Path foundPath = finder.findSinglePath( neo, agentSmith );
            System.out.println( "Path from Neo to Agent Smith: "
                    + Paths.simplePathToString( foundPath, NAME_KEY ) );
        	// END SNIPPET: shortestPathUsage
        }

        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
        System.out.println( "Database off." );
    }

    private static void createChain( String... names )
    {
        for ( int i = 0; i < names.length - 1; i++ )
        {
            Node firstNode = getOrCreateNode( names[i] );
            Node secondNode = getOrCreateNode( names[i + 1] );
            firstNode.createRelationshipTo( secondNode, KNOWS );
        }
    }

    private static Node getOrCreateNode( String name )
    {
        Node node = indexService.get( NAME_KEY, name ).getSingle();
        if ( node == null )
        {
            node = graphDb.createNode();
            node.setProperty( NAME_KEY, name );
            indexService.add( node, NAME_KEY, name );
        }
        return node;
    }

    private static void registerShutdownHook()
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    private static void deleteFileOrDirectory( File file )
    {
        if ( file.exists() )
        {
            if ( file.isDirectory() )
            {
                for ( File child : file.listFiles() )
                {
                    deleteFileOrDirectory( child );
                }
            }
            file.delete();
        }
    }
}

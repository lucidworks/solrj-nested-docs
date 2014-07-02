import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class NestedDocs2 {

static final String SOLR_URL = "http://localhost:8983/solr/collection1";

// IMPORTANT: This demo requires Solr 4.9.0 or above

public static void main ( String[] args ) throws Exception {

    HttpSolrServer solr = new HttpSolrServer( SOLR_URL );

  /*
  Goals of this demo:
  - Index the nested documents
  - Be able to do normal queries
  - Use parent / child Block Joins in queries
  - Be able to query for either parent or child records
  - Get nested query results with both record types using the [child] Transformer
    (this is new in Solr 4.9.0)
  - Use eDismax to alter the relevancy
  - Show how to use custom parameters
  - Show the full Solr URL to allow for easy edits and testing


  Consider an XML stream of the form:
  <insurance_accounts>
    <person>
        <firstname>John</firstname>
        <lastname>Jones</lastname>
        ...other details...
        <cars>
            <car>
                <make>honda</make>
                <model>accord</model>
            </car>
            <car>
                <make>Nissan</make>
                <model>Maxima</model>
            </car>
        </cars>
        <bikes>
            <bike>
                <make>yamaha</make>
                <model>passion</model>
            </bike>
            <bike>
                <make>Peugeot</make>
                <model>Vivacity</model>
            </bike>
        </bikes>
    </person>
    <person>
        <firstname>Satish</firstname>
        <lastname>Smith</lastname>
        <cars>
            <car>
                <make>Peugeot</make>
                <model>iOn</model>
            </car>
        </cars>
        <bikes>
            <bike>
                <make>Honda</make>
                <model>Spree</model>
            </bike>
        </bikes>
    </person>
    <person>
        ...
    </person>
  </insurance_accounts>

  Summary of the Parent/Child objects:
    John Jones
        car: Honda Accord
        car: Nissan Maxima
        bike: Yamaha Passion
        bike: Peugeot Vivacity
    Satish Smith
        car: Peugeot iOn
        bike: Honda Spree

  We then submit this data via SolrJ, using nested SolrInputDocument objects.

  Then run queries and show results.

  TODO: maybe add code that shows converting from XML to SolrJ

  */


    // Docs to submit
    Collection<SolrInputDocument> batch = new ArrayList<SolrInputDocument>();

    // Parent Doc 1, a person mamed John Jones
    SolrInputDocument person1 = new SolrInputDocument();
    person1.addField( "id",            "john_jones" );
    person1.addField( "content_type",  "person"     );
    // "_t" suffix tells Solr that it's text
    person1.addField( "first_name_t",  "John"       );
    person1.addField( "last_name_t",   "Jones"      );
    // states and history used in edismax examples
    person1.addField( "states_t",      "California Nevada Idaho Maine" );
    person1.addField( "history_t",     "safe accident accident accident accident accident" );

    // child docs, the vehicles he owns
    SolrInputDocument p1_car1 = new SolrInputDocument();
    p1_car1.addField( "id",            "jj_car1"    );
    p1_car1.addField( "content_type",  "car"        );
    // For cars "make" is an alias for "manufacturer"
    p1_car1.addField( "make_t",        "Honda"      );
    p1_car1.addField( "model_t",       "Accord"     );

    SolrInputDocument p1_car2 = new SolrInputDocument();
    p1_car2.addField( "id",            "jj_car2"    );
    p1_car2.addField( "content_type",  "car"        );
    p1_car2.addField( "make_t",        "Nissan"     );
    p1_car2.addField( "model_t",       "Maxima"     );

    SolrInputDocument p1_bike1 = new SolrInputDocument();
    p1_bike1.addField( "id",           "jj_bike1"   );
    p1_bike1.addField( "content_type", "bike"       );
    p1_bike1.addField( "make_t",       "Yamaha"     );
    p1_bike1.addField( "model_t",      "Passion"    );

    SolrInputDocument p1_bike2 = new SolrInputDocument();
    p1_bike2.addField( "id",           "jj_bike2"   );
    p1_bike2.addField( "content_type", "bike"       );
    p1_bike2.addField( "make_t",       "Peugeot"    );
    p1_bike2.addField( "model_t",      "Vivacity"   );

    // Add children to parent
    person1.addChildDocument( p1_car1  );
    person1.addChildDocument( p1_car2  );
    person1.addChildDocument( p1_bike1 );
    person1.addChildDocument( p1_bike2 );

    // Add parent to batch
    batch.add( person1 );


    // Parent Doc 2, person mamed Satish Smith
    SolrInputDocument person2 = new SolrInputDocument();
    person2.addField( "id",           "satish_smith" );
    person2.addField( "content_type", "person"       );
    person2.addField( "first_name_t", "Satish"       );
    person2.addField( "last_name_t",  "Smith"        );
    person2.addField( "states_t",     "California Texas California Maine Vermont Connecticut" );
    person2.addField( "history_t",    "safe safe safe safe safe safe safe safe accident" );

    // Vehicles (child docs)
    SolrInputDocument p2_car1 = new SolrInputDocument();
    p2_car1.addField( "id",            "ss_car1"     );
    p2_car1.addField( "content_type",  "car"         );
    p2_car1.addField( "make_t",        "Peugeot"     );
    p2_car1.addField( "model_t",       "iOn"         );
    SolrInputDocument p2_bike1 = new SolrInputDocument();
    p2_bike1.addField( "id",           "ss_bike1"    );
    p2_bike1.addField( "content_type", "bike"        );
    p2_bike1.addField( "make_t",       "Honda"       );
    p2_bike1.addField( "model_t",      "Spree"       );
    // link objects and add to batch
    person2.addChildDocument( p2_car1  );
    person2.addChildDocument( p2_bike1 );
    batch.add( person2 );

    System.out.println( "Adding batch of " + batch.size() + " parent docs" );

    // Submit as a group
    solr.add( batch );
    solr.commit();

    // Run some Test Queries

    // doQuery( solrServer, qryDescription, qryTerms, optFilterQuery )
    doQuery( solr, "Null Query, All Docs",
                "*:*", null );

    // People and their Vehicles
    doQuery( solr, "All People",
                "*:*", "content_type:person" );
    doQuery( solr, "All Cars",
                "*:*", "content_type:car" );
    doQuery( solr, "All Bikes",
                "*:*", "content_type:bike" );

    /***
    Repeating sample data outline here:
    John Jones
        car: Honda Accord
        car: Nissan Maxima
        bike: Yamaha Passion
        bike: Peugeot Vivacity
    Satish Smith
        car: Peugeot iOn
        bike: Honda Spree
    ***/

    // Any person who owns a Honda car AND a Peugeot bike
    //   but NOT Honda bike nor Peugeot car, so not Satish Smith
    // We're retrieving parent docs (people)
    //   but most of the search logic is against child docs (vehicles)
    doQuery(solr,
            "People who own both a Honda *car* and a Peugeot *bike*",
            "*:*",
            "{!parent which=\"content_type:person\"}"
                + "(content_type:car AND make_t:Honda)"
                + " OR (content_type:bike AND make_t:Peugeot)");

    // All Cars owned by Mr. Jones
    // We're retrieving child docs (cars)
    //   but the fulltext search is against parent docs (people)
    doQuery(solr,
            "All cars owned by Mr. Jones",
            "content_type:car",
            "{!child of=\"content_type:person\"}last_name_t:jones");

    // Show how to get merged Parent and Child records
    // -----------------------------------------------

    // Any person who owns a Honda car AND a Peugeot bike
    //   and include the matching vehicles as children,
    //   but *only* the *matching* children.
    // This uses the [child ...] doc Transformer that appeared in Solr 4.9.0
    Map<String,String> params = new LinkedHashMap<>();
    params.put( "parent_filter", "content_type:person" );
    params.put( "child_filter",
                  "(content_type:car AND make_t:Honda)"
                + " OR (content_type:bike AND make_t:Peugeot)" );
    // Use second form of doQuery to pass more arguments:
    // doQuery( solr, description, queryStr, optFilter, optFields, extraParamsMap )
    doQuery(solr,
            "People who own a Honda car and Peugeot bike, and include the *matching* vehicles in results",
            "*:*",                                                             // query
            "{!parent which=$parent_filter v=$child_filter}",                  // filter
            "*,[child parentFilter=$parent_filter childFilter=$child_filter]", // fields
            params );                                                          // extra params

    // Also, try some fancy queries with eDismax
    // -----------------------------------------

    // eDismax1: Drivers matching accidents and/or California, no joins
    // dismax and edismax are similar, "e" is for "Enhanced"
    // John Jones will be listed first, "accident" appears many times in his history
    /*Map<String,String>*/ params = new LinkedHashMap<>();
    params.put( "defType", "edismax" );
    params.put( "qf", "history_t states_t" );  // query fields
    doQuery(solr,
            "eDismax1: Drivers matching accidents and/or California, no joins",
            "California accident report",
            null, null, params);

    // eDismax2: Drivers matching accidents in California, stressing the State
    // Satish Smith will be listed first, since he's lived in California twice
    /*Map<String,String>*/ params = new LinkedHashMap<>();
    params.put( "defType", "edismax" );
    params.put( "qf", "history_t states_t^100" );  // query fields
    doQuery(solr,
            "eDismax2: Drivers matching accidents and/or California, no joins, boost on State",
            "California accident report",
            null, null, params);

    // eDismax + Parent / Child Join
    // -----------------------------

    // eDismax3: Drivers matching accidents and/or California, favoring state
    //   and include *all* of their vehicles (no childFilter this time)
    // John Jones will be listed first, accident appears many times in history
    /*Map<String,String>*/ params = new LinkedHashMap<>();
    params.put( "parent_filter", "content_type:person" );
    params.put( "defType", "edismax" );
    params.put( "qf", "history_t states_t^100" );
    doQuery(solr,
            "eDismax3: Drivers matching accidents and/or California, and all of their vehicles, boost on State",
            "California accident report",
            "{!parent which=$parent_filter}",
            "*,[child parentFilter=$parent_filter]",
            params );

}

static void doQuery( HttpSolrServer solr, String description, String queryStr, String optFilter ) throws Exception {
    doQuery( solr, description, queryStr, optFilter, null, null );
}
static void doQuery( HttpSolrServer solr, String description, String queryStr, String optFilter,
                     String optFields, Map<String,String>extraParams ) throws Exception {
    // Setup Query
    SolrQuery q = new SolrQuery( queryStr );
    System.out.println();
    System.out.println( "Test: " + description );
    System.out.println( "\tSearch: " + queryStr );
    if ( null!=optFilter ) {
        q.addFilterQuery( optFilter );
        System.out.println( "\tFilter: " + optFilter );
    }
    if ( null!=optFields ) {
        // Use setParam instead of addField
        q.setParam( "fl", optFields );  // childFilter=doc_type:chapter limit=100
        System.out.println( "\tFields: " + optFields );
    }
    else {
        q.addField( "*" );  // childFilter=doc_type:chapter limit=100
    }
    if ( null!=extraParams ) {
        for ( Entry<String,String> param : extraParams.entrySet() ) {
            // Could use q.setParam which allows you to pass in multiple strings
            q.set( param.getKey(), param.getValue() );
            System.out.println( "\tParam: " + param.getKey() + "=" + param.getValue() );
        }
    }

    // Run and show results
    QueryResponse rsp = solr.query( q );
    SolrDocumentList docs = rsp.getResults();
    long numFound = docs.getNumFound();
    System.out.println( "Matched: " + numFound );
    int docCounter = 0;
    for (SolrDocument doc : docs) {
        docCounter++;
        System.out.println( "Doc # " + docCounter );
        for ( Entry<String, Object> field : doc.entrySet() ) {
            String name = field.getKey();
            Object value = field.getValue();
            System.out.println( "\t" + name + "=" + value );
        }
        List<SolrDocument> childDocs = doc.getChildDocuments();
        // TODO: make this recursive, for grandchildren, etc.
        if ( null!=childDocs ) {
            for ( SolrDocument child : childDocs ) {
                System.out.println( "\tChild doc:" );
                for ( Entry<String, Object> field : child.entrySet() ) {
                    String name = field.getKey();
                    Object value = field.getValue();
                    System.out.println( "\t\t" + name + "=" + value );
                }
            }
        }
    }
    System.out.println( "Query URL:" );
    // TODO: should check URL for existing trailing /, and allow for different query handler
    System.out.println( SOLR_URL + "/select?" + q );
}

}

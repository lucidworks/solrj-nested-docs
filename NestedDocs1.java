import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;


public class NestedDocs1 {

static final String SOLR_URL = "http://localhost:8983/solr/collection1";

public static void main ( String[] args ) throws Exception {

    HttpSolrServer solr = new HttpSolrServer( SOLR_URL );

    /***
    Equivalent Solr XML doc:
    <add>
        <doc>
            <field name="id">product01</field>
            <field name="name">car</field>
            <field name="content_type">product</field>
            <doc>
                <field name="id">part01</field>
                <field name="name">wheels</field>
                <field name="content_type">part</field>
            </doc>
            <doc>
                <field name="id">part02</field>
                <field name="name">engine</field>
                <field name="doctype">part</field>
            </doc>
            <doc>
                <field name="id">part03</field>
                <field name="name">brakes</field>
                <field name="content_type">part</field>
            </doc>
        </doc>
        <doc>
            <field name="id">product02</field>
            <field name="name">truck</field>
            <field name="content_type">product</field>
            <doc>
                <field name="id">part04</field>
                <field name="name">wheels</field>
                <field name="content_type">part</field>
            </doc>
            <doc>
                <field name="id">part05</field>
                <field name="name">flaps</field>
                <field name="doctype">part</field>
            </doc>
        <doc>
    </add>
    ***/

    // Docs to submit
    Collection<SolrInputDocument> batch = new ArrayList<SolrInputDocument>();

    // Parent Doc 1
    SolrInputDocument product01 = new SolrInputDocument();
    product01.addField( "id", "product01" );
    product01.addField( "name", "car" );
    product01.addField( "content_type", "product" );

    // 3 Children
    SolrInputDocument part01 = new SolrInputDocument();
    part01.addField( "id", "part01" );
    part01.addField( "name", "wheels" );
    part01.addField( "content_type", "part" );
    SolrInputDocument part02 = new SolrInputDocument();
    part02.addField( "id", "part02" );
    part02.addField( "name", "engine" );
    part02.addField( "content_type", "part" );
    SolrInputDocument part03 = new SolrInputDocument();
    part03.addField( "id", "part03" );
    part03.addField( "name", "brakes" );
    part03.addField( "content_type", "part" );

    // Add children to parent
    product01.addChildDocument( part01 ); 
    product01.addChildDocument( part02 ); 
    product01.addChildDocument( part03 ); 

    //System.out.println( "product01 = " + product01 );

    // Add parent to batch
    batch.add( product01 );

    // Parent Doc 2 with 2 children
    SolrInputDocument product02 = new SolrInputDocument();
    product02.addField( "id", "product02" );
    product02.addField( "name", "truck" );
    product02.addField( "content_type", "product" );
    SolrInputDocument part04 = new SolrInputDocument();
    part04.addField( "id", "part04" );
    part04.addField( "name", "wheels" );
    part04.addField( "content_type", "part" );
    SolrInputDocument part05 = new SolrInputDocument();
    part05.addField( "id", "part05" );
    part05.addField( "name", "flaps" );
    part05.addField( "content_type", "part" );
    product02.addChildDocument( part04 ); 
    product02.addChildDocument( part05 ); 
    //System.out.println( "product02 = " + product02 );
    batch.add( product02 );

    System.out.println( "Adding batch of " + batch.size() + " parent docs" );

    // Submit as a group
    solr.add( batch );
    solr.commit();

    // Run some Test Queries
    // doQuery( solrServer, qryDescription, qryTerms, optFilterQuery )

    doQuery( solr, "Null Query, All Docs",
                "*:*", null );

    // Products and Parts
    doQuery( solr, "All Products",
                "*:*", "content_type:product" );
    doQuery( solr, "All Parts",
                "*:*", "content_type:part" );

    // Wheels for Cars, returning Parts
    //      want part01 "car / wheels"
    //      but not part04 "truck / wheels"
    // Return Children:
    //      ?q=childTerm&fq={!child of="doctype:parent"}parentTerm
    doQuery( solr, "Wheels for Cars",
                "name:wheels", "{!child of=\"content_type:product\"}name:car" );

    // Products that have Mud Flaps, AKA "flaps"
    //      want product02 from "track / flaps"
    // Return Parents:
    //      ?q=parentTerm&fq={!parent which="doctype:parent"}childTerm
    doQuery( solr, "Products with Flaps",
                "*:*", "{!parent which=\"content_type:product\"}name:flaps" );

}

static void doQuery( HttpSolrServer solr, String description, String queryStr, String optFilter ) throws Exception {
    // Setup Query
    SolrQuery q = new SolrQuery( queryStr );
    System.out.println();
    System.out.println( "Running query: " + description );
    System.out.print  ( "         with: " + queryStr );
    if ( null!=optFilter ) {
        q.addFilterQuery( optFilter );
        System.out.print( ", Filter: " + optFilter );
    }
    System.out.println();

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
    }
}

}

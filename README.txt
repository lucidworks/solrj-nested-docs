Solr Nested Documents and Block Joins
v0.2, 7/1/2014, mark dot bennett at lucidworks dot com

Simple programs to demonstrate Parent / Child joins in Solr
    using the SolrJ API

Requires:
    Java 1.7 or above
    Demo 1: Solr 4.5 or above; 4.9.0 used here
    Demo 2: Requires Solr 4.9.0
    Script assumes Unix Bash Shell
        could run on Windows if proper classpath is set

Demo 1 vs. Demo 2
    Demo 1 shows basic joins
    Demo 2 goes further:
    - Index the nested documents
    - Be able to do normal queries
    - Use parent / child Block Joins in queries
    - Be able to query for either parent or child records
    - Get nested query results with both record types using the [child] Transformer
      (this is new in Solr 4.9.0)
    - Use eDismax to alter the relevancy
    - Show how to use custom parameters
    - Show the full Solr URL to allow for easy edits and testing

Just want to look?
    Just view the files NestedDocs1 & 2.java and sample-output1 & 2.txt

To actually run...

Setup Solr:
    Download Solr (for example 4.9.0)
    Unzip / untar
    cd solr-4.9.0/example
    java -jar start.jar
    # Java code uses fields defined in the default schema.xml

Run this example:
    Be in this main directory
    ./jr1.sh   # jr = "java run"
    ./jr2.sh   # second, fancier demo
    # Ignore warnings starting with "SLF4J: ...", this is just SolrJ logging nonsense.

Recompile:
    This ships with compiled .class files
    But you can recompile with:
    ./jc.sh   # jc = "java compile", compiles both demos
    Perhaps useful if you want to change the code
        or use older Solr or Java versions

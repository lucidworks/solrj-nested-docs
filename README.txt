Solr Nested Documents and Block Joins
v0.1, 6/17/2014, mark dot bennett at lucidworks dot com

Simple program to demonstrate Parent / Child joins in Solr
    using the SolrJ API

Requires:
    Java 1.7 or above
    Solr 4.5 or above; 4.8.1 used here
    Script assumes Unix Bash Shell
        could run on Windows if proper classpath is set

Just want to look?
    Just view the files NestedDocs1.java and sample-output1.txt

To actually run...

Setup Solr:
    Download Solr (for example 4.8.1)
    Unzip / untar
    cd solr-4.8.1/example
    java -jar start.jar
    # Java code uses fields defined in the default schema.xml

Run this example:
    Be in this main directory
    ./jr1.sh   # jr = "java run"
    # Ignore warnings starting with "SLF4J: ...", this is just SolrJ logging nonsense.

Recompile:
    This ships with a compiled .class file
    But you can recompile with:
    ./jc.sh   # jc = "java compile"
    Perhaps useful if you want to change the code
        or use older Solr or Java versions

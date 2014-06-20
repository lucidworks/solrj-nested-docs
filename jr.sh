#!/bin/bash

JARS=`echo lib/*.jar | sed -e 's/ /:/g'`
CLASS=NestedDocs

java -cp ".:$JARS" $CLASS

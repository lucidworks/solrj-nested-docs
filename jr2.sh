#!/bin/bash

JARS=`echo lib/*.jar | sed -e 's/ /:/g'`
CLASS=NestedDocs2

java -cp ".:$JARS" $CLASS

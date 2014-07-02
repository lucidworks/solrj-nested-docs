#!/bin/bash

JARS=`echo lib/*.jar | sed -e 's/ /:/g'`
CLASS=NestedDocs1

java -cp ".:$JARS" $CLASS

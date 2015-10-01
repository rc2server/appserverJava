#!/usr/bin/env bash

DBNAME=rc2itest
dropdb $DBNAME
createdb -O rc2 $DBNAME
psql -U rc2 $DBNAME < rc2.sql >/dev/null
psql -U rc2 $DBNAME < testData.sql > /dev/null


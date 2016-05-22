#!/bin/bash
 
# Set this hostname
HOSTNAME='hi'
 
# Set Graphite host
GRAPHITE=192.168.99.100
GRAPHITE_PORT=8125
 
# Loop forever
while :
do
    # Get epoch
    DATE=`date +%s`
 	echo "DATE"
 	echo "${DATE}"

    # Collect some random data for
    # this example
    #MY_DATA=`ls /tmp | wc -l`
 
    # Send data to Graphite
    echo "com.hiitsme.test.dropwizard.api.RecipeEndpoint.postRecipe.p98 0.42 $Date\n" | nc 192.168.99.100 8125
 
    sleep 1
done
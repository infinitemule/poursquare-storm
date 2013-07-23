package com.infinitemule.hopperhack.storm

import backtype.storm.topology.TopologyBuilder
import backtype.storm.Config
import backtype.storm.LocalCluster

import com.infinitemule.hopperhack.foursq.storm._
import com.infinitemule.hopperhack.twitter.storm._


/**
 * This runs the cluster in local mode.  For submitting to the cluster,
 * I created a Maven assembly (one jar file with all dependencies) and
 * submitted using the tools that they gave us found here:
 * http://hackreduce.github.io/storm-hackathon/start/
 */
object PoursquareTopology extends App {

  val fourSqUrl   = "4sq.com"
  val bearerToken = System.getenv("TWITTER_BEARER_TOKEN")
  
    
  // API endpoint to submit checkins to the client which get stored in MongoDB
    
  val repoHost = "localhost"
  val repoPort = 8080
  val repoPath = "/poursquare/api/checkin/create"
  
    
  // Number of seconds for which to run the local topology
    
  val runFor = 2 * 60
    
  
  // I am not crazy about the Topology Builder, I find it hard to follow.  I wouldn't
  // be surprised if there is a Scala wrapper that helps you out with this.  Maybe
  // something like the Scala Camel DSL. 
  
  val builder = new TopologyBuilder();
  
  
  // I just picked three for the parallel hint because ... that's what I used.
  // I am not sure how to use or tweak those, my guess is that you can look at
  // the StormUI, which gives you a bunch of metrics about your topology, and
  // you can make adjustments about how many bolts to use.
  
  builder.setSpout("twitterApi", new TwitterApiSpout(bearerToken), 1);
  
  
  builder.setBolt("twitterApiParser", new TwitterApiParserBolt(), 3)
         .shuffleGrouping("twitterApi")

           
  builder.setBolt("twitterApiFoursquareLinkParser", new TwitterApiLinkBolt(fourSqUrl), 3)
         .shuffleGrouping("twitterApiParser")

  
  builder.setBolt("foursquareCheckinLinkResolver", new FoursquareCheckinUrlResolverBolt(fourSqUrl), 3)
         .shuffleGrouping("twitterApiFoursquareLinkParser")
  
           
  builder.setBolt("foursquareCheckinFetcherBolt", new FoursquareCheckinFetcherBolt(), 3)
         .shuffleGrouping("foursquareCheckinLinkResolver")
  
  builder.setBolt("foursquareCheckinParserBolt", new FoursquareCheckinParserBolt(), 3)
   		 .shuffleGrouping("foursquareCheckinFetcherBolt")
         
  builder.setBolt("foursquareCheckinRepositoryBolt", 
                  new FoursquareCheckinRepositoryBolt(repoHost, repoPort, repoPath), 3)
   		 .shuffleGrouping("foursquareCheckinParserBolt")
   		 
   		 
  val conf = new Config()
  
  conf.setDebug(true)
  conf.setNumWorkers(1)

  deployLocal(builder, conf) 
    
  
  def deployLocal(builder: TopologyBuilder, conf: Config) = {
    
    val cluster = new LocalCluster()
  
    cluster.submitTopology("poursquare", conf, builder.createTopology())
  
    Thread.sleep(runFor * 1000)
  
    cluster.killTopology("poursquare")
    println("Topo Killed")    
    Thread.sleep(2000)
    
    cluster.shutdown()
    println("Cluster shutdown")
    Thread.sleep(2000)
    
    println("Done")
    
  }       
  
}
package com.infinitemule.hopperhack.storm

import backtype.storm.topology.base.BaseRichSpout
import backtype.storm.topology.OutputFieldsDeclarer

import backtype.storm.task.TopologyContext
import backtype.storm.tuple.Fields
import backtype.storm.spout.SpoutOutputCollector

import java.util.{Map => JavaMap}


/**
 * Base class for Storm Spouts.  There's not much common
 * functionality that I needed, I just wanted to simplify
 * creating simple spouts.
 */
abstract class StormSpout extends BaseRichSpout {

  val fields: List[String]
  
  var collector: SpoutOutputCollector = _

  
  override def open(conf: JavaMap[_,_], context: TopologyContext, collector: SpoutOutputCollector) = {
    this.collector = collector
  }

  
  override def declareOutputFields(declarer: OutputFieldsDeclarer) = {
    declarer.declare(new Fields(fields : _*))
  }

}
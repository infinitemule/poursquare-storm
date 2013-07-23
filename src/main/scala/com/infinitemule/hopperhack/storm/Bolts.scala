package com.infinitemule.hopperhack.storm

import backtype.storm.topology.base.BaseRichBolt
import backtype.storm.topology.OutputFieldsDeclarer
import backtype.storm.task.{TopologyContext, OutputCollector}
import backtype.storm.tuple.{Fields, Tuple, Values}

import java.util.{Map => JavaMap}


/**
 * Base class for Storm Bolts.  There's not much common
 * functionality that I needed, I just wanted to simplify
 * creating simple bolts.
 */
abstract class StormBolt extends BaseRichBolt {
  
  val fields: List[String]
      
  var collector: OutputCollector = _
  
  override def prepare(stormConf: JavaMap[_,_], context: TopologyContext, collector: OutputCollector) = {
    this.collector = collector
  }
     
  override def declareOutputFields(declarer: OutputFieldsDeclarer) {
    declarer.declare(new Fields(fields : _*))
  }

  protected def emitAndAck(tuple: Tuple, value: Object*) = {
    collector.emit(tuple, new Values(value:_*))
    collector.ack(tuple)
  }
    
}
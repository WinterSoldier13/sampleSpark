package com.wintersoldier.jms

import org.apache.spark.sql.execution.streaming.Offset

case class JmsSourceOffset(val id:Long) extends Offset {
    
    override def json(): String = id.toString
    
}


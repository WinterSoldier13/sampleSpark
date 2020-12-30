package org.apache.spark.sql.jms

import com.wintersoldier.jms.{DefaultSource, JmsMessage, JmsSourceOffset}
import org.apache.spark.sql.catalyst.{InternalRow, ScalaReflection}
import org.apache.spark.sql.execution.streaming.{Offset, Source}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.util.LongAccumulator

import javax.jms._
import scala.collection.mutable.ListBuffer


class JmsStreamingSource(sqlContext: SQLContext,
                         parameters: Map[String, String],
                         metadataPath: String,
                         failOnDataLoss: Boolean
                        ) extends Source {
    
    lazy val RECEIVER_TIMEOUT: Long = parameters.getOrElse("receiver.timeout", "3000").toLong
    lazy val name_ = "ayush"
    val connection: Connection = DefaultSource.connectionFactory(parameters).createConnection
    connection.setClientID(name_)
    
    val session: Session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)
    var counter: LongAccumulator = sqlContext.sparkContext.longAccumulator("counter")
    
            val topic_ = session.createTopic("sample_topic2")
            val subscriber_ = session.createDurableSubscriber(topic_, name_)
            connection.start()
            val numMssg = 10
//            for( i <- 1 to numMssg)
//                {
//                    val textMessage: TextMessage = subscriber_.receive(5000).asInstanceOf[TextMessage]
//                    println(s"<><><><><><><><><><><><><><> $textMessage")
//                }
    
//        connection.start()
    
    override def getOffset: Option[Offset] = {
        counter.add(1)
        Some(JmsSourceOffset(counter.value))
    }
    
    override def getBatch(start: Option[Offset], end: Offset): DataFrame = {
        
//        val topic_ = session.createTopic("sample_topic2")
//        val subscriber_ = session.createDurableSubscriber(topic_, "mySub")
//        connection.start()
        
        
        //        val queue = session.createQueue(parameters("queue"))
        //        val consumer = session.createConsumer(queue)
        var break = true
        val messageList: ListBuffer[JmsMessage] = ListBuffer()
        while (break) {
            val textMsg = subscriber_.receive(RECEIVER_TIMEOUT).asInstanceOf[TextMessage]
            if (parameters.getOrElse("acknowledge", "false").toBoolean && textMsg != null) {
                textMsg.acknowledge()
            }
            textMsg match {
                case null => break = false
                case _ => messageList += JmsMessage(textMsg)
            }
        }
        import org.apache.spark.unsafe.types.UTF8String._
        val internalRDD = messageList.map(message => InternalRow(
            fromString(message.content),
            fromString(message.correlationId),
            fromString(message.jmsType),
            fromString(message.messageId),
            fromString(message.queue)
        ))
        val rdd = sqlContext.sparkContext.parallelize(internalRDD)
        //        sqlContext.internalCreateDataFrame(rdd,schema,isStreaming = true)
        sqlContext.internalCreateDataFrame(rdd, schema = schema, isStreaming = true)
        
    }
    
    override def schema: StructType = {
        ScalaReflection.schemaFor[JmsMessage].dataType.asInstanceOf[StructType]
    }
    
    override def stop(): Unit = {
        session.close()
        connection.close()
    }
    
}

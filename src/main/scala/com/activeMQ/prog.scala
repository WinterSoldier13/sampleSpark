package com.activeMQ



import com.xmlReader.xmlReader.spark
import org.apache.avro.generic.GenericData.StringType
import org.apache.spark.sql.functions.{col, from_json, schema_of_json, udf}
import org.apache.spark.sql.types.{DataType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import java.io.StringReader

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.xml.sax
import org.xml.sax.InputSource

import scala.collection.mutable.ListBuffer
import com.xmlHelper.getAllPath.getAllPaths

import scala.xml.XML
//noinspection ScalaStyle

object prog {
    
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    
    //    spark.sparkContext.setLogLevel("OFF")
    import spark.implicits._
    
//    val df2 = spark.read
////        .option("delimiter", "|")
////        .textFile("src/main/static/dataset/sampleXML.xml")
//        .format("com.databricks.spark.xml")
//        .option("rowTag", "CofiResults")
//        .xml("src/main/static/dataset/sampleXML.xml")
    
    
    
    def main(args: Array[String]): Unit = {
        
        val brokerUrl_ : String = "tcp://localhost:1883"
        val topicName_ : String = "sample_topic"
        val username_ : String  = "user"
        val password_ : String  = "password"
        
        //    This part is to read messages from a topic
        //    Format of dataframe:
        //        id : integer, topic: String, payload : binary, timestamp : timestamp
        val df = spark
            .readStream
            .format("org.apache.bahir.sql.streaming.mqtt.MQTTStreamSourceProvider")
            .option("brokerUrl", brokerUrl_)
            .option("topic", topicName_)
            .option("persistence", "memory")
            .option("cleanSession", "true")
            .option("username", username_)
            .option("password", password_)
            .load()
            val payload_ = df.select("payload")
                              .as[Array[Byte]]
                              .map(payload => { new String(payload) })
                              .toDF("payload")
                        
        
        
//        TODO LOAD THE SAMPLE XML FILE
        var xmlFile = XML.loadFile("src/main/static/dataset/sampleXML.xml")
//        todo THEN GET ALL THE POSSIBLE PATHS
        var allPossiblePaths: List[String] = getAllPaths.getAllPathAsList(xmlFile)
        // Print ALL the paths
//        for( x <- allPossiblePaths)
//            {
//                println(x)
//            }
        var queryString = ""
        for( x <- allPossiblePaths)
            {
                queryString+= s", xpath(payload, '$x/text()') "
            }
        queryString = queryString.substring(2)
        queryString = s"select $queryString from tempTable"
        
        println(queryString)
        
        payload_.createOrReplaceTempView("tempTable")
        var alpha =  spark.sql(queryString)

        alpha
            .writeStream
            .outputMode("append")
            .format("console")
            .option("truncate", value = false)
            .start
            .awaitTermination()
        
    

//        import com.databricks.spark.xml.functions.from_xml
//        import com.databricks.spark.xml.schema_of_xml
////
////
//        val payloadSchema = schema_of_xml(df2.select("value").as[String])
////
//        var parsed = payload_.withColumn("parsed", from_xml($"payload", payloadSchema))
//
    
//        val XMLSchema = df2.schema
//        XMLSchema.printTreeString()
//        df2.show()
//        df2.printSchema()
        
//        parsed = parsed.select('parsed cast "string")
//        parsed
//            .writeStream
//            .outputMode("append")
//            .format("console")
//            .option("truncate", value = false)
//            .start
//            .awaitTermination()
//
        
//
//        import com.databricks.spark.xml.functions.from_xml
//        import com.databricks.spark.xml.schema_of_xml
//
//
//        val payloadSchema = schema_of_xml(df.select("payload").as[String])
//        val parsed = df.withColumn("parsed", from_xml($"payload", payloadSchema))
//
        //    df.printSchema()
//        val Schema = df.schema
//        Schema.printTreeString()
        
        
        
        //    Print the messages from the topic on the console
        //    df.select("payload")
        //      .as[Array[Byte]]
        //      .map(payload => { new String(payload) })
        //      .toDF("payload")
    

//

//
    
//        val parse = udf((value: String) => XML.toJSONObject(value).toString) // Defined UDF to parse xml to json
//
//
//        val extractValuesFromXML = udf { (xml: String) => scala.xml.XML.loadString(xml) }
//
//
//        val payload_ = df
//            .select('payload cast "string")
//                    .select(from_json(parse($"payload"),XMLSchema).as("tempData"))
//
//        payload_.printSchema()
//
//
//        parsed
//            .writeStream
//            .outputMode("append")
//            .format("console")
//            .option("truncate", value = false)
//            .start
//            .awaitTermination()
        
        val checkpointLocation: String = "/home/wintersoldier/Desktop/dump/checkpoint"
        
        //    This is a sample example to write message to a topic of ActiveMQ
        //    Comment the print message part before using this
        
        //    val df2 = df
        //        .writeStream
        //        .format("org.apache.bahir.sql.streaming.mqtt.MQTTStreamSinkProvider")
        //        .option("brokerUrl","tcp://localhost:1883")
        //        .option("topic","sample_topic4")
        //        .option("checkpointLocation", checkpointLocation)
        //        .start
        //    df2.awaitTermination()
        
        spark.stop()
        spark.close()
    }
    
    
    def execute(dfs: DataFrame*): Seq[DataFrame] = {
        val Seq(nsDf) = dfs
        nsDf.show(truncate = false)
        nsDf.printSchema()
        val outputDf = nsDf.select(nsDf.columns.take(2).map(col): _*)
        Seq(outputDf)
    }
}

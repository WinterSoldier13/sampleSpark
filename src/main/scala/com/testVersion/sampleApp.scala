package com.testVersion

import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.collection.mutable.ListBuffer
import com.xmlHelper.getAllPath.getAllPaths

import scala.xml.XML



/*
todo HOW THIS WORKS?

-> Load a XML file from the Disk... Unfortunately this is required step :(
-> Now I will infer it's path type... I mean the path from the rootNode to the leafNode ... by node I mean XMLtag here
-> To generate all possible paths from the rootNode to a leafNode CONTAINING SOME VALUE I used Depth-First-Search... considering the XML to be Directed-Acyclic Graph
-> the DFS Algo that I wrote can be found at  "com.xmlHelper.getAllPath"
-> Now after getting all the paths to all the leafNodes, I generated a SQL query (below described as queryString)
-> Then I executed that query String on the table

-> PROS:
-> All the data is available in columnnar form

-> CONS:
-> The name of the column... instead of being "pathToTheLeafNode" is appearing as the QueryName

 */

object sampleApp
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    
    //    spark.sparkContext.setLogLevel("OFF")
    import spark.implicits._
    
    def main(args: Array[String]): Unit = {
        
        val brokerUrl_ : String = "tcp://localhost:1883"
        val topicName_ : String = "sample_topic"
        val username_ : String  = "username"
        val password_ : String  = "password"
        val path2XML_ : String  = "src/main/static/dataset/smallXML.xml"
        val tempTable_ : String = "tempTable"
        
        //    This part is to read messages from a topic
        //    Format of dataframe:
        //        id : integer, topic: String, payload : binary, timestamp : timestamp
        
        val df = spark
            .readStream
            .format("org.apache.bahir.sql.streaming.mqtt.MQTTStreamSourceProvider")
            .option("brokerUrl", brokerUrl_)
            .option("topic", topicName_)
            .option("persistence", "memory")
            .option("cleanSession", "false")
            .option("username", username_)
            .option("password", password_)
            .option("localStorage", "/home/wintersoldier/Desktop/tempS")
            .option("clientId", "ayush")
            .load()
    
        df.printSchema()
        val payload_ = df.select('payload cast "string", 'id cast "string")
        
        
//        val payload_ = df.select("payload")
//            .as[Array[Byte]]
//            .map(payload => { new String(payload) })
//            .toDF("payload")
//
//        val temp__ = payload_.select("payload").collectAsList()
//
//
//        payload_.createOrReplaceTempView(tempTable_)
        
        if(false) {
            //        TODO LOAD THE SAMPLE XML FILE from the MEMORY
            var xmlFile = XML.loadFile(path2XML_)
    
            //        todo THEN GET ALL THE POSSIBLE PATHS
            var allPossiblePaths: List[String] = getAllPaths.getAllPathAsList(xmlFile)
    
            //        todo Generate the queryString... I guess just appending will be enough
    
            val query_ = allPossiblePaths.map(x => s"xpath(payload, '$x/text()')").mkString(", ")
            val queryString_ = s"select $query_ from $tempTable_"
            println(queryString_)
        }
        else {
    
//            val path_ = "$.PmtMethod.Mop"
//            val queryString_ = s"select  get_json_object(payload, '$path_') from $tempTable_"
//
//
//            // todo EXECUTE THE SQL QUERY
//
//            val alpha = spark.sql(queryString_)
//            //
//            //        // todo PRINT THE VALUE TO THE CONSOLE
            payload_
                .writeStream
                .outputMode("append")
                .format("console")
                .option("truncate", value = false)
                .start
                .awaitTermination()
        }
        
        spark.close()
        spark.stop()
        }
}

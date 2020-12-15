package com.testVersion

import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.collection.mutable.ListBuffer
import com.xmlHelper.getAllPath.getAllPaths

import scala.xml.XML



/*
todo HOW THIS WORKS?

-> Load a XML file from the Disc... Unfortunately this is required
-> Now I will infer it's path type... I mean the path from the rootNode to the leafNode
-> To generate all possible paths from the rootNode to a leafNode CONTAINING SOME VALUE I used Depth-First-Search
-> the DFS Algo can be found at  "com.xmlHelper.getAllPath"
-> Now after getting all the paths to all the leafNodes, I generated a SQL query (below described as queryString
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
        val username_ : String  = "user"
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
            .option("cleanSession", "true")
            .option("username", username_)
            .option("password", password_)
            .load()
        
        val payload_ = df.select("payload")
            .as[Array[Byte]]
            .map(payload => { new String(payload) })
            .toDF("payload")
    
        payload_.createOrReplaceTempView(tempTable_)
    
        //        TODO LOAD THE SAMPLE XML FILE from the MEMORY
        var xmlFile = XML.loadFile(path2XML_)
        
        //        todo THEN GET ALL THE POSSIBLE PATHS
        var allPossiblePaths: ListBuffer[String] = getAllPaths.getAllPathAsList(xmlFile)
        
//        todo Generate the queryString... I guess just appending will be enough
        var queryString = ""
        // Appending to the string
        for( x <- allPossiblePaths)
        {
            queryString+= s", xpath(payload, '$x/text()') "
        }
        queryString = queryString.substring(2)
        queryString = s"select $queryString from $tempTable_"
    
        println(queryString)
    
        // todo EXECUTE THE SQL QUERY
        var alpha =  spark.sql(queryString)
    
        // todo PRINT THE VALUE TO THE CONSOLE
        alpha
            .writeStream
            .outputMode("append")
            .format("console")
            .option("truncate", value = false)
            .start
            .awaitTermination()
        
        spark.close()
        spark.stop()
        }
}

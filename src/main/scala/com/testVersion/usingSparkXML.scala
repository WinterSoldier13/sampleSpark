package com.testVersion

import com.databricks.spark.xml.functions.from_xml
import com.databricks.spark.xml.schema_of_xml
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col

object usingSparkXML {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("ERROR")
    
    import spark.implicits._
    
    def main(args: Array[String]): Unit = {
        
        val brokerUrl_ : String = "tcp://localhost:1883"
        val topicName_ : String = "sample_topic"
        val username_ : String = "username"
        val password_ : String = "password"
        val path2XML_ : String = "src/main/static/dataset/smallXML.xml"
        val tempTable_ : String = "tempTable"
        val parqOutPath: String = "/home/wintersoldier/Documents/tookitaki/sparkTest/src/main/static/ouput/fromSparkXML"
        
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
            //            .option("localStorage", "/home/wintersoldier/Desktop/tempS")
            .option("clientId", "ayush")
            .load()
        
        df.printSchema()
        //        val payload_ = df.select('payload cast "string", 'id cast "string")
        
        
        val payload_ = df.select("payload")
            .as[Array[Byte]]
            .map(payload => {
                new String(payload)
            })
            .toDF("payload")
        
        payload_.writeStream
            .foreachBatch((batchDF: DataFrame, batchID: Long) => {
                println(s"Processing batch number: $batchID")
                val payloadSchemaXML = schema_of_xml(batchDF.select("payload").as[String])
                payloadSchemaXML.printTreeString()
                val parsed = batchDF.withColumn("parsed", from_xml(col("payload"), payloadSchemaXML))
                //                parsed.show(true)
                
                if (!parsed.isEmpty) {
                    parsed.select("parsed")
                        .write
                        .format("json")
                        .mode("overwrite")
                        .save(parqOutPath)
                    parsed.select("parsed").show(false)
                    parsed.select("parsed").printSchema()
                    
                    
                    
                }
            })
            .start
            .awaitTermination()
        
        spark.close()
        spark.stop()
    }
    
}

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
            //            .option("localStorage", "/home/wintersoldier/Desktop/tempS")
            .option("clientId", "ayush")
            .load()
        
        df.printSchema()
        //        val payload_ = df.select('payload cast "string", 'id cast "string")
        
        
        val payload_ = df.select("payload")
            .as[Array[Byte]]
            .map(payload => { new String(payload) })
            .toDF("payload")
        
        payload_.writeStream
            //            .format("console")
            //            .outputMode("append")
            .foreachBatch((batchDF : DataFrame, batchID : Long) => {
                val payloadSchemaXML = schema_of_xml(batchDF.select("payload").as[String])
                payloadSchemaXML.printTreeString()
                val parsed = batchDF.withColumn("parsed", from_xml(col("Payload"), payloadSchemaXML))
                //                parsed.show(true)
                if(!parsed.isEmpty)
                {
                    parsed.select("parsed")
                        .write
                        .format("parquet")
                        .mode("overwrite")
                        .save("/home/wintersoldier/Desktop/tempS")
                    parsed.select("parsed").show(true)
                    
                    //                        val json = parsed.select("payload").toJSON
                    //                        json.show()
                    //
                    //                        json.write
                    //                            .format("json")
                    //                            .mode("overwrite")
                    //                            .save("/home/wintersoldier/Desktop/tempS")
                    
                    
                }
            })
            .start
            .awaitTermination()
        
        //        val payloadSchemaXML = schema_of_xml(payload_.select("payload").as[String])
        //        val parsed = df.withColumn("parsed", from_xml($"payload", payloadSchemaXML))
        //
        //        parsed.writeStream
        //            .outputMode("append")
        //            .format("console")
        //            .option("truncate", value = false)
        //            .start
        //            .awaitTermination()
        
        //        payload_.createOrReplaceTempView(tempTable_)
        //
        //        if(true) {
        //            //        TODO LOAD THE SAMPLE XML FILE from the MEMORY
        //            var xmlFile = XML.loadFile(path2XML_)
        //
        //            //        todo THEN GET ALL THE POSSIBLE PATHS
        //            var allPossiblePaths: List[String] = getAllPaths.getAllPathAsList(xmlFile)
        //
        //            //        todo Generate the queryString... I guess just appending will be enough
        //
        //            val query_ = allPossiblePaths.map(x => s"xpath(payload, '$x/text()')").mkString(", ")
        ////            val queryString_ = s"select $query_ from $tempTable_"
        //            val queryString_ = s"select xpath(payload, 'Entity/Field[@id=4][@type=33783808]/text()') from $tempTable_"
        //            val alpha = spark.sql(queryString_)
        //
        //            alpha
        //                .writeStream
        //                .outputMode("append")
        //                .format("console")
        //                .option("truncate", value = false)
        //                .start
        //                .awaitTermination()
        //        }
        //        else {
        //
        ////            val path_ = "$.PmtMethod.Mop"
        ////            val queryString_ = s"select  get_json_object(payload, '$path_') from $tempTable_"
        ////
        ////
        ////            // todo EXECUTE THE SQL QUERY
        ////
        ////            val alpha = spark.sql(queryString_)
        ////            //
        ////            //        // todo PRINT THE VALUE TO THE CONSOLE
        //            payload_
        //                .writeStream
        //                .outputMode("append")
        //                .format("console")
        //                .option("truncate", value = false)
        //                .start
        //                .awaitTermination()
        //        }
        
        spark.close()
        spark.stop()
    }
    
    def createQueryALPHA(s : String)
    {
        // s = "Entity.Field.@id=[4]
        var query : String = s.replace('.','/')
    }
}

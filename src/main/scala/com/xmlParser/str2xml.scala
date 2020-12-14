package com.xmlParser

import java.io.{IOException, OutputStream, StringReader}

import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory}
import org.apache.hadoop.mapred.OutputFormat
import org.w3c.dom.{Document, Element, Node}
import org.xml.sax.InputSource


object str2xml {
    def main(args : Array[String]): Unit =
    {
        
        var str = scala.xml.XML.loadFile("src/main/static/dataset/sampleXML.xml").toString()
        
        val doc = convert2XML(str)
        
//        var ele1 = doc.getElementsByTagName("employees")
    
        var docEle : Element = doc.getDocumentElement
        var nl = docEle.getChildNodes
        var length = nl.getLength
        
        var alpha = nl.getLength
        println(alpha)
        
//        for (i <- 0 to length)
//        {

//            if (nl.item(i).getNodeType == Node.ELEMENT_NODE) {
//                var el = nl.item(i).asInstanceOf[Element]
//                if (el.getNodeName.contains("staff")) {
//                    var name = el.getElementsByTagName("name").item(0).getTextContent;
//                    var phone = el.getElementsByTagName("phone").item(0).getTextContent;
//                    var email = el.getElementsByTagName("email").item(0).getTextContent;
//                    var area = el.getElementsByTagName("area").item(0).getTextContent;
//                    var city = el.getElementsByTagName("city").item(0).getTextContent;
//                }
//        }
//    }
        
//        printDocument(doc, System.out)
        
        
    }
    
    def convert2XML(xmlString: String): Document = {
    
        val factory : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        
        var builder : DocumentBuilder = null
        
        builder = factory.newDocumentBuilder()
        val doc : Document = builder.parse(new InputSource(new StringReader(xmlString)))
        doc
    }
    
    import javax.xml.transform.OutputKeys
    import javax.xml.transform.TransformerException
    import javax.xml.transform.TransformerFactory
    import javax.xml.transform.dom.DOMSource
    import javax.xml.transform.stream.StreamResult
    import java.io.OutputStreamWriter
    
    @throws[IOException]
    @throws[TransformerException]
    def printDocument(doc: Document, out: OutputStream): Unit = {
        val tf = TransformerFactory.newInstance
        val transformer = tf.newTransformer
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")))
    }
    
}

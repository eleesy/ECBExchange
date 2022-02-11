/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 * Class for Euro reference exchange rates model
 * Osztály a napi euro referencia árfolyamok modelljének tárolásához
 * Adatforrás: 
 *     Európai Központi Bank
 *     https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
 */

package ecbexchange;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Eleesy
 */
public class ExchangeRates {
    
    HashMap<String, Double> mapRefRates;
    
    /** Constructor */
    ExchangeRates() {
        mapRefRates = new HashMap<>();
        updateByFile(ECBExchange.DATA_FILENAME);
    }

    /** Get Currencies / Visszatér a pénznemek listájával */
    public java.util.List<String> getCurrencies() {
        java.util.List<String> arfolyamok = new ArrayList<>();
        mapRefRates.forEach((k,v) -> arfolyamok.add(k));
        return arfolyamok;
    }
    
    /** Get Currencies / Visszatér a pénznemek listájával */
    public Double getRate(Object o) {
      return mapRefRates.get(o);
    }
    
    /** Parser for ECB eurofxref XML file / Értelmező az ECB eurofxref XML fájhoz */ 
    private Boolean parseXML(InputStream inputStream) {
        Boolean r = false;
        mapRefRates.clear();
        mapRefRates.put("EUR", 1.0);            
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("Cube");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element el = (Element) nodeList.item(i);
                String key = el.getAttribute("currency");
                if (key != null && !key.isEmpty()) {
                    mapRefRates.put(key, Double.valueOf(el.getAttribute("rate")));
                }
            } 
            r = true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
        } 
        return r;
    }
    
    /** Betölti a megadott fájlt, feldolgozza XML-két */
    public Boolean updateByFile(String fileName) {
        Boolean r = false;
        try {
            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            r = parseXML(inputStream);
        } catch (FileNotFoundException e) {
            Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
        }
        return r;
    }
    
    /** Letölti az URL-ben megadott fájlt, értelmezi XML-két, elmenti lokálisan */
    public Boolean updateByURL(String url) {
        Boolean r = false;
        Integer fileSize;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;
        try { 
            inputStream = new URL(url).openStream();
            fileSize = inputStream.available();            
            if (fileSize > 0) {
                byte[] dataBuffer = new byte[fileSize]; 
                inputStream.read(dataBuffer);
                ByteArrayInputStream inStream = new ByteArrayInputStream(dataBuffer); /* read from the inputStream */ 
                r = parseXML(inStream);
                if (r) {
                    try {
                        fileOutputStream = new FileOutputStream(ECBExchange.DATA_FILENAME);
                        fileOutputStream.write(dataBuffer, 0, fileSize);
                        fileOutputStream.close();
                    } catch (IOException e) {
                      Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
                    }
                }            
                dataBuffer = null;
            }
        } catch (MalformedURLException e) {
            Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (IOException e) {
                Logger.getLogger(ECBExchange.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return r;
    }
 
}

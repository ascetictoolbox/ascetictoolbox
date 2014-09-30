/*
 *  Copyright 2002-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package integratedtoolkit.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The serializer class is an utility to Serialize and deserialize objects 
 * passed as a parameter of a remote task
 */
public class Serializer {
	
	public static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(integratedtoolkit.log.Loggers.IT);
    /**
     * Serializes an objects and leaves it in a file
     * @param o object to be serailized
     * @param file file where the serialized object will be stored
     * @throws IOException  Error writting the file
     */
    public static void serialize(Object o, String file) throws IOException {
        try{
        	serializeBinary(o, file);
        }catch(NotSerializableException e){	
        	serializeXML(o, file);
        }
    }

    /**
     * Reads an object from a file
     * @param file containing the serialized object
     * @return the object read from the file
     * @throws IOException  Error reading the file
     */
    public static Object deserialize(String file) throws IOException, ClassNotFoundException {
        try{
        	return deserializeBinary(file);
        }catch(Exception e){
        	logger.debug("Binary deserializer not working for file "+ file);
        	return deserializeXML(file);
        }
    }
    
    
    /**
     * Serializes an objects usign the default java serializer and leaves it in
     * a file
     * @param o object to be serialized
     * @param file file where to store the serialized object
     * @throws IOException  Error writting the file
     */
    private static void serializeBinary(Object o, String file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(o);
        oos.close();
    }

    /**
     * Reads a binary-serialized object from a file
     * @param file containing the serialized object
     * @return the object read from the file
     * @throws IOException  Error reading the file
     */
    private static Object deserializeBinary(String file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Serializes an objects usign the XML Encoder and leaves it in a file
     * @param o object to be serialized
     * @param file file where to store the serialized object
     * @throws IOException  Error writting the file
     */
    private static void serializeXML(Object o, String file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fout));
        e.writeObject(o);
        e.close();
    }

    /**
     * Reads an XML-serialized object from a file
     * @param file containing the serialized object
     * @return the object read from the file
     * @throws IOException  Error reading the file
     */
    private static Object deserializeXML(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        XMLDecoder d = new XMLDecoder(new BufferedInputStream(fis));
        Object o = d.readObject();
        d.close();
        return o;
    }
}

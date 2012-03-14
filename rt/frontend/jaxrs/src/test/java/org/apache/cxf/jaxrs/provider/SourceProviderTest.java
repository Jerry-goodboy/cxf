/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.jaxrs.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.staxutils.StaxSource;

import org.junit.Assert;
import org.junit.Test;

public class SourceProviderTest extends Assert {
    
       
    @Test
    public void testIsWriteable() {
        SourceProvider p = new SourceProvider();
        assertTrue(p.isWriteable(StreamSource.class, null, null, null)
                   && p.isWriteable(DOMSource.class, null, null, null)
                   && p.isWriteable(Source.class, null, null, null));
    }
    
    @Test
    public void testIsReadable() {
        SourceProvider p = new SourceProvider();
        assertTrue(p.isReadable(StreamSource.class, null, null, null)
                   && p.isReadable(DOMSource.class, null, null, null)
                   && p.isReadable(Source.class, null, null, null));
    }

    @Test
    public void testReadFrom() throws Exception {
        SourceProvider p = new TestSourceProvider();
        assertSame(StreamSource.class, verifyRead(p, StreamSource.class).getClass());
        assertSame(StaxSource.class, verifyRead(p, Source.class).getClass());
        assertSame(StaxSource.class, verifyRead(p, SAXSource.class).getClass());
        assertSame(StaxSource.class, verifyRead(p, StaxSource.class).getClass());
        assertSame(DOMSource.class, verifyRead(p, DOMSource.class).getClass());
        assertTrue(Document.class.isAssignableFrom(verifyRead(p, Document.class).getClass()));
    }
    
    @Test
    public void testReadFromWithPreferredFormat() throws Exception {
        SourceProvider p = new TestSourceProvider("sax");
        assertSame(StaxSource.class, verifyRead(p, Source.class).getClass());
    }
    
    @Test
    public void testWriteTo() throws Exception {
        SourceProvider p = new TestSourceProvider();
        StreamSource s = new StreamSource(new ByteArrayInputStream("<test/>".getBytes()));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.writeTo(s, null, null, null, MediaType.APPLICATION_XML_TYPE, 
                  new MetadataMap<String, Object>(), os);
        assertTrue(os.toString().contains("<test/>"));
        os = new ByteArrayOutputStream();
        p.writeTo(createDomSource(), null, null, null, MediaType.APPLICATION_XML_TYPE, 
                  new MetadataMap<String, Object>(), os);
        assertTrue(os.toString().contains("<test/>"));
    }
    
    @SuppressWarnings("unchecked")
    private <T> Object verifyRead(MessageBodyReader p, Class<T> type) throws Exception {
        return p.readFrom(type,
                   null, null, null, null,
                   new ByteArrayInputStream("<test/>".getBytes()));
    }
    
    private DOMSource createDomSource() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return new DOMSource(builder.parse(new ByteArrayInputStream("<test/>".getBytes())));
    }
    
    private static class TestSourceProvider extends SourceProvider {
        
        private String format;
        
        public TestSourceProvider() {
            
        }
        
        public TestSourceProvider(String format) {
            this.format = format;    
        }

        protected Message getCurrentMessage() {
            Message m = new MessageImpl();
            if (format != null) {
                m.put("source-preferred-format", format);
            }
            return m;
        };
    }
}

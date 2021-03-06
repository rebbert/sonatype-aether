package org.sonatype.aether.test.util.connector;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import static org.junit.Assert.*;
import static org.sonatype.aether.TransferEvent.EventType.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.DefaultMetadata;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.NoRepositoryConnectorException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferEvent.EventType;
import org.sonatype.aether.TransferListener;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.test.impl.RecordingTransferListener;
import org.sonatype.aether.test.impl.TestRepositorySystemSession;
import org.sonatype.aether.test.util.FileUtil;

public class TransferEventTester
{
    // TODO: test failed/interrupted transfers

    public static void testTransferEvents( RepositoryConnectorFactory factory )
        throws IOException, NoRepositoryConnectorException
    {

        RecordingTransferListener listener = new RecordingTransferListener();

        ConnectorTestContext ctx = setupTestContext( listener );
        testTransferEvents( factory, ctx );
    }

    public static void testTransferEvents( RepositoryConnectorFactory factory, ConnectorTestContext ctx )
        throws IOException, NoRepositoryConnectorException
    {
        RepositorySystemSession session = ctx.getSession();
        RemoteRepository repository = ctx.getRepository();
        RecordingTransferListener listener = ctx.getRecordingTransferListener();

        RepositoryConnector connector = factory.newInstance( session, repository );

        byte[] pattern = "tmpFile".getBytes();
        File tmpFile = FileUtil.createTempFile( pattern, 10000 );
        long expectedBytes = tmpFile.length();

        Collection<ArtifactUpload> artUps = createTransfers( ArtifactUpload.class, 1, tmpFile );
        Collection<ArtifactDownload> artDowns = createTransfers( ArtifactDownload.class, 1, tmpFile );
        Collection<MetadataUpload> metaUps = createTransfers( MetadataUpload.class, 1, tmpFile );
        Collection<MetadataDownload> metaDowns = createTransfers( MetadataDownload.class, 1, tmpFile );

        connector.put( artUps, null );
        LinkedList<TransferEvent> events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.get( artDowns, null );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.put( null, metaUps );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
        listener.clear();

        connector.get( null, metaDowns );
        events = new LinkedList<TransferEvent>( listener.getEvents() );
        checkEvents( events, expectedBytes );
    }

    private static void checkEvents( Queue<TransferEvent> events, long expectedBytes )
    {
        TransferEvent currentEvent = events.poll();
        assertNotNull( "initiate event is missing", currentEvent );
        assertEquals( INITIATED, currentEvent.getType() );
        checkProperties( currentEvent );

        currentEvent = events.poll();
        assertNotNull( "start event is missing", currentEvent );
        assertEquals( TransferEvent.EventType.STARTED, currentEvent.getType() );
        checkProperties( currentEvent );

        EventType progressed = TransferEvent.EventType.PROGRESSED;
        EventType succeeded = TransferEvent.EventType.SUCCEEDED;

        TransferEvent succeedEvent = null;

        int dataLength = 0;
        long transferredBytes = 0;
        while ( ( currentEvent = events.poll() ) != null )
        {
            EventType currentType = currentEvent.getType();

            if ( succeeded.equals( currentType ) )
            {
                succeedEvent = currentEvent;
                checkProperties( currentEvent );
                break;
            }
            else
            {
                assertTrue( progressed.equals( currentType ) );
                assertTrue( currentEvent.getTransferredBytes() > transferredBytes );
                transferredBytes = currentEvent.getTransferredBytes();
                dataLength += currentEvent.getDataLength();
                checkProperties( currentEvent );
            }
        }

        // all events consumed
        assertEquals( 0, events.size() );

        // test transferred size
        assertEquals( "progress events transferred bytes don't match", expectedBytes, dataLength );
        assertEquals( "succeed event transferred bytes don't match", expectedBytes, succeedEvent.getTransferredBytes() );
    }

    private static void checkProperties( TransferEvent event )
    {
        assertNotNull( event.getResource() );
        assertNotNull( event.getRequestType() );
        assertNotNull( event.getType() );

        if ( PROGRESSED.equals( event.getType() ) )
        {
            assertNotNull( event.getDataBuffer() );
            assertTrue( event.getDataLength() > -1 );
            assertTrue( event.getDataOffset() > -1 );
            assertTrue( event.getTransferredBytes() > -1 );
        }
        else if ( SUCCEEDED.equals( event.getType() ) )
        {
            assertTrue( event.getTransferredBytes() > -1 );
        }
    }

    public static ConnectorTestContext setupTestContext()
    {
        return setupTestContext( null );
    }

    public static ConnectorTestContext setupTestContext( TransferListener listener )
    {

        File testRepo = new File( "target/test-repo" );
        testRepo.mkdirs();

        RemoteRepository repository;
        try
        {
            repository = new RemoteRepository( "test-repo", "default", testRepo.toURI().toURL().toString() );
        }
        catch ( MalformedURLException e )
        {
            // conversion File->URL... should not happen
            throw new UnsupportedOperationException(
                                                     "Malformed URL with File->URI->URL: " + testRepo.getAbsolutePath(),
                                                     e );
        }

        TestRepositorySystemSession session = new TestRepositorySystemSession();

        return new ConnectorTestContext( repository, session );

    }

    public static <T extends Transfer> Collection<T> createTransfers( Class<T> cls, int count, File file )
    {
        ArrayList<T> ret = new ArrayList<T>();

        for ( int i = 0; i < count; i++ )
        {
            DefaultArtifact artifact =
                new DefaultArtifact( "testGroup", "testArtifact", "sources", "jar", ( i + 1 ) + "-test" );
            DefaultMetadata metadata =
                new DefaultMetadata( "testGroup", "testArtifact", ( i + 1 ) + "test", "jar",
                                     Metadata.Nature.RELEASE_OR_SNAPSHOT, file );
            String context = null;
            String checksumPolicy = null;

            Object obj = null;
            if ( cls.isAssignableFrom( ArtifactUpload.class ) )
            {
                obj = new ArtifactUpload( artifact, file );
            }
            else if ( cls.isAssignableFrom( ArtifactDownload.class ) )
            {
                obj = new ArtifactDownload( artifact, context, file, checksumPolicy );
            }
            else if ( cls.isAssignableFrom( MetadataUpload.class ) )
            {
                obj = new MetadataUpload( metadata, file );
            }
            else if ( cls.isAssignableFrom( MetadataDownload.class ) )
            {
                obj = new MetadataDownload( metadata, context, file, checksumPolicy );
            }

            ret.add( cls.cast( obj ) );
        }

        return ret;
    }
}

package org.sonatype.aether.connector.file;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactNotFoundException;
import org.sonatype.aether.ArtifactTransferException;
import org.sonatype.aether.ChecksumFailureException;
import org.sonatype.aether.Metadata;
import org.sonatype.aether.MetadataNotFoundException;
import org.sonatype.aether.MetadataTransferException;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryPolicy;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.TransferEvent.RequestType;
import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactTransfer;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataTransfer;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.Transfer;
import org.sonatype.aether.spi.connector.Transfer.State;
import org.sonatype.aether.util.ChecksumUtils;
import org.sonatype.aether.util.listener.DefaultTransferEvent;
import org.sonatype.aether.util.listener.DefaultTransferResource;

/**
 * The actual class doing all the work. Handles artifact and metadata up- and downloads.
 * 
 * @author Benjamin Hanzelmann
 */
class FileRepositoryWorker
    implements Runnable
{

    enum Direction
    {
        UPLOAD( TransferEvent.RequestType.PUT ), DOWNLOAD( TransferEvent.RequestType.GET );

        TransferEvent.RequestType type;

        private Direction( TransferEvent.RequestType type )
        {
            this.type = type;
        }

        public RequestType getType()
        {
            return type;
        }
    }

    private static LinkedHashMap<String, String> checksumAlgos;

    private TransferWrapper transfer;

    private RemoteRepository repository;

    private DefaultLayout layout;

    private CountDownLatch latch = null;

    private TransferEventCatapult catapult;

    private Direction direction;

    /**
     * Set the latch to count down after all work is done.
     */
    public void setLatch( CountDownLatch latch )
    {
        this.latch = latch;
    }

    static
    {
        checksumAlgos = new LinkedHashMap<String, String>( 4 );
        checksumAlgos.put( "SHA-1", ".sha1" );
        checksumAlgos.put( "MD5", ".md5" );
    }

    private FileRepositoryWorker( ArtifactTransfer transfer, RemoteRepository repository, Direction direction,
                                  RepositorySystemSession session )
    {
        this( session, repository, direction );

        if ( transfer == null )
        {
            throw new IllegalArgumentException( "Transfer may not be null." );
        }
        this.transfer = new TransferWrapper( transfer );
    }

    private FileRepositoryWorker( MetadataTransfer transfer, RemoteRepository repository, Direction direction,
                                  RepositorySystemSession session )
    {
        this( session, repository, direction );

        if ( transfer == null )
        {
            throw new IllegalArgumentException( "Transfer may not be null." );
        }
        this.transfer = new TransferWrapper( transfer );
    }

    /**
     * Initialize the worker for an artifact upload.
     * 
     * @param transfer The actual {@link Transfer}-object. May not be <code>null</code>.
     * @param repository The repository definition. May not be <code>null</code>.
     * @param session The current repository system session. May not be <code>null</code>.
     */
    public FileRepositoryWorker( ArtifactUpload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.UPLOAD, session );
    }

    /**
     * Initialize the worker for an artifact download.
     * 
     * @param transfer The actual {@link Transfer}-object. May not be <code>null</code>.
     * @param repository The repository definition. May not be <code>null</code>.
     * @param session The current repository system session. May not be <code>null</code>.
     */
    public FileRepositoryWorker( ArtifactDownload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.DOWNLOAD, session );
    }

    /**
     * Initialize the worker for an metadata download.
     * 
     * @param transfer The actual {@link Transfer}-object. May not be <code>null</code>.
     * @param repository The repository definition. May not be <code>null</code>.
     * @param session The current repository system session. May not be <code>null</code>.
     */
    public FileRepositoryWorker( MetadataDownload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.DOWNLOAD, session );
    }

    /**
     * Initialize the worker for an metadata upload.
     * 
     * @param transfer The actual {@link Transfer}-object. May not be <code>null</code>.
     * @param repository The repository definition. May not be <code>null</code>.
     * @param session The current repository system session. May not be <code>null</code>.
     */
    public FileRepositoryWorker( MetadataUpload transfer, RemoteRepository repository, RepositorySystemSession session )
    {
        this( transfer, repository, Direction.UPLOAD, session );
    }

    private FileRepositoryWorker( RepositorySystemSession session, RemoteRepository repository, Direction direction )
    {
        if ( repository == null )
        {
            throw new IllegalArgumentException( "RemoteRepository may not be null." );
        }
        if ( session == null )
        {
            throw new IllegalArgumentException( "RepositorySystemSession may not be null." );
        }

        this.catapult = new TransferEventCatapult( session.getTransferListener() );

        this.direction = direction;
        this.repository = repository;
        this.layout = new DefaultLayout();
    }

    /**
     * Do transfer according to {@link RepositoryConnector} specifications.
     * 
     * @see FileRepositoryConnector
     */
    public void run()
    {
        // TODO check event types for mandatory values
        
        File target = null;
        long totalTransferred = -1;
        try
        {
            transfer.setState( State.NEW );
            DefaultTransferEvent event = newEvent( transfer, repository );
            catapult.fireInitiated( event );

            File baseDir = new File( new URI( repository.getUrl() ) );
            File localFile = transfer.getFile();
            File repoFile = null;
            switch ( transfer.getType() )
            {
                case ARTIFACT:
                    repoFile = new File( baseDir, layout.getPath( transfer.getArtifact() ) );
                    break;
                case METADATA:
                    repoFile = new File( baseDir, layout.getPath( transfer.getMetadata() ) );
                    break;
            }

            File src = null;

            switch ( direction )
            {
                case UPLOAD:
                    src = localFile;
                    target = repoFile;
                    break;
                case DOWNLOAD:
                    src = repoFile;
                    target = localFile;
                    break;
            }

            transfer.setState( State.ACTIVE );
            event = newEvent( transfer, repository );
            catapult.fireStarted( event );
            event = null;

            target.getParentFile().mkdirs();
            totalTransferred = copy( src, target );

            Map<String, Object> crcs = ChecksumUtils.calc( src, checksumAlgos.keySet() );
            switch ( direction )
            {
                case UPLOAD:
                    // write checksum files
                    for ( Entry<String, Object> crc : crcs.entrySet() )
                    {
                        String name = crc.getKey();
                        Object sum = crc.getValue();

                        if ( sum instanceof Throwable )
                        {
                            throw (Throwable) sum;
                        }

                        File crcTarget = new File( target.getPath() + checksumAlgos.get( name ) );
                        FileWriter crcWriter = new FileWriter( crcTarget );
                        crcWriter.write( sum.toString() );
                        crcWriter.close();
                    }
                    break;
                case DOWNLOAD:
                    // verify checksum
                    if ( RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( transfer.getChecksumPolicy() ) )
                    {
                        break;
                    }
                    boolean verified = false;
                    try
                    {
                        for ( Entry<String, String> entry : checksumAlgos.entrySet() )
                        {
                            try
                            {
                                String sum = ChecksumUtils.read( new File( src.getPath() + entry.getValue() ) );
                                verified = sum.equalsIgnoreCase( crcs.get( entry.getKey() ).toString() );
                                if ( !verified )
                                {
                                    throw new ChecksumFailureException( sum, crcs.get( entry.getKey() ).toString() );
                                }
                                break;
                            }
                            catch ( IOException e )
                            {
                                // skip verify - try next algorithm
                                continue;
                            }
                        }

                        // all algorithms checked
                        if ( !verified )
                        {
                            throw new ChecksumFailureException( "no supported algorithms found" );
                        }
                    }
                    catch ( ChecksumFailureException e )
                    {
                        if ( RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( transfer.getChecksumPolicy() ) )
                        {
                            throw e;
                        }
                        else if ( RepositoryPolicy.CHECKSUM_POLICY_WARN.equals( transfer.getChecksumPolicy() ) )
                        {
                            event = newEvent( transfer, repository );
                            event.setException( e );
                            catapult.fireCorrupted( event );
                        }

                    }

                    break;
            }
        }
        catch ( FileNotFoundException e) {
            switch ( transfer.getType() )
            {
                case ARTIFACT:
                    transfer.setException( new ArtifactNotFoundException( transfer.getArtifact(), repository ) );
                    break;
                case METADATA:
                    transfer.setException( new MetadataNotFoundException( transfer.getMetadata(), repository ) );
                    break;
            }
            
        }
        catch ( Throwable t )
        {
            switch ( transfer.getType() )
            {
                case ARTIFACT:
                    transfer.setException( new ArtifactTransferException( transfer.getArtifact(), repository, t ) );
                    break;
                case METADATA:
                    transfer.setException( new MetadataTransferException( transfer.getMetadata(), repository, t ) );
                    break;
            }
        }
        finally
        {
            transfer.setState( State.DONE );
            try
            {
                if ( transfer.getException() == null )
                {
                    DefaultTransferEvent event = newEvent( transfer, repository );
                    event.setTransferredBytes( (int) totalTransferred );
                    catapult.fireSucceeded( event );
                }
                else
                {
                    // cleanup
                    if ( target != null )
                        target.delete();
                    if ( direction.equals( Direction.UPLOAD ) )
                    {
                        for ( String ext : checksumAlgos.values() )
                        {
                            new File( target.getPath() + ext ).delete();
                        }
                    }
                    catapult.fireFailed( newEvent( transfer, repository ) );
                }
            }
            finally
            {
                if ( latch != null )
                    latch.countDown();
            }
        }

    }

    private long copy( File src, File target )
        throws FileNotFoundException, IOException, TransferCancelledException
    {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        long total = 0;
        try
        {
            inStream = new FileInputStream( src );
            in = inStream.getChannel();
            outStream = new FileOutputStream( target );
            out = outStream.getChannel();
            long count = 200000L;
            ByteBuffer buf = ByteBuffer.allocate( (int) count );

            buf.clear();
            int transferred;
            while ( ( transferred = in.read( buf ) ) >= 0 || buf.position() != 0 )
            {
                total += transferred;

                DefaultTransferEvent event = newEvent( transfer, repository );
                event.setDataBuffer( buf.array() );
                event.setDataLength( buf.position() );
                event.setDataOffset( 0 );
                event.setTransferredBytes( transferred );
                catapult.fireProgressed( event );

                buf.flip();
                out.write( buf );
                buf.compact();
            }
            buf.flip();
            while ( buf.hasRemaining() )
            {
                out.write( buf );
            }
        }
        finally
        {
            if ( inStream != null )
                inStream.close();
            if ( in != null )
                in.close();
            if ( outStream != null )
                outStream.close();
            if ( out != null )
                out.close();
        }

        return total;
    }

    private DefaultTransferEvent newEvent( TransferWrapper transfer, RemoteRepository repository )
    {
        DefaultTransferEvent event = new DefaultTransferEvent();
        String resourceName = null;
        switch ( transfer.getType() )
        {
            case ARTIFACT:
                Artifact artifact = transfer.getArtifact();
                resourceName =
                    String.format( "%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
                break;
            case METADATA:
                Metadata metadata = transfer.getMetadata();
                resourceName =
                    String.format( "%s:%s:%s", metadata.getGroupId(), metadata.getArtifactId(), metadata.getVersion() );
        }
        event.setResource( new DefaultTransferResource( repository.getUrl(), resourceName, transfer.getFile() ) );
        event.setRequestType( direction.getType() );
        return event;
    }

}
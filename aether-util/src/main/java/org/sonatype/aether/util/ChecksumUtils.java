package org.sonatype.aether.util;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility class to assist in the verification and generation of checksums.
 * 
 * @author Benjamin Bentmann
 */
public class ChecksumUtils
{

    /**
     * Extracts the checksum from the specified file.
     * 
     * @param checksumFile The path to the checksum file, must not be {@code null}.
     * @return The checksum stored in the file, never {@code null}.
     * @throws IOException If the checksum does not exist or could not be read for other reasons.
     */
    public static String read( File checksumFile )
        throws IOException
    {
        String checksum = "";

        FileInputStream fis = new FileInputStream( checksumFile );
        try
        {
            BufferedReader br = new BufferedReader( new InputStreamReader( fis, "UTF-8" ) );
            while ( true )
            {
                String line = br.readLine();
                if ( line == null )
                {
                    break;
                }
                line = line.trim();
                if ( line.length() > 0 )
                {
                    checksum = line;
                    break;
                }
            }
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch ( IOException e )
            {
                // ignored
            }
        }

        if ( checksum.matches( ".+= [0-9A-Fa-f]+" ) )
        {
            int lastSpacePos = checksum.lastIndexOf( ' ' );
            checksum = checksum.substring( lastSpacePos + 1 );
        }
        else
        {
            int spacePos = checksum.indexOf( ' ' );

            if ( spacePos != -1 )
            {
                checksum = checksum.substring( 0, spacePos );
            }
        }

        return checksum;
    }

    /**
     * Calculates checksums for the specified file.
     * 
     * @param dataFile The file for which to calculate checksums, must not be {@code null}.
     * @param algos The names of checksum algorithms (cf. {@link MessageDigest#getInstance(String)} to use, must not be
     *            {@code null}.
     * @return The calculated checksums, indexed by algorithm name, or the exception that occured while trying to
     *         calculate it, never {@code null}.
     * @throws IOException If the data file could not be read.
     */
    public static Map<String, Object> calc( File dataFile, Collection<String> algos )
        throws IOException
    {
        Map<String, Object> results = new LinkedHashMap<String, Object>();

        Map<String, MessageDigest> digests = new LinkedHashMap<String, MessageDigest>();
        for ( String algo : algos )
        {
            try
            {
                digests.put( algo, MessageDigest.getInstance( algo ) );
            }
            catch ( NoSuchAlgorithmException e )
            {
                results.put( algo, e );
            }
        }

        FileInputStream fis = new FileInputStream( dataFile );
        FileChannel in = fis.getChannel();
        try
        {

            ByteBuffer bytebuffer = ByteBuffer.allocate( 16 * 1024 );
            while ( in.read( bytebuffer ) >= 0 || bytebuffer.position() != 0 )
            {
                for ( MessageDigest digest : digests.values() )
                {
                    bytebuffer.flip();
                    digest.update( bytebuffer );
                }
                bytebuffer.compact();
            }
        }
        finally
        {
            fis.close();
            in.close();
        }

        for ( Map.Entry<String, MessageDigest> entry : digests.entrySet() )
        {
            byte[] bytes = entry.getValue().digest();

            StringBuilder buffer = new StringBuilder( 64 );

            for ( int i = 0; i < bytes.length; i++ )
            {
                int b = bytes[i] & 0xFF;
                if ( b < 0x10 )
                {
                    buffer.append( '0' );
                }
                buffer.append( Integer.toHexString( b ) );
            }

            results.put( entry.getKey(), buffer.toString() );
        }

        return results;
    }

}

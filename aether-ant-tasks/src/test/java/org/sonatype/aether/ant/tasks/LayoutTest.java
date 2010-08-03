package org.sonatype.aether.ant.tasks;

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

import org.apache.tools.ant.BuildException;
import org.junit.Test;
import org.sonatype.aether.DefaultArtifact;

/**
 * @author Benjamin Bentmann
 */
public class LayoutTest
{

    @Test( expected = BuildException.class )
    public void testUnknownVariable()
    {
        new Layout( "{unknown}" );
    }

    @Test
    public void testGetPath()
    {
        Layout layout;

        layout =
            new Layout( "{groupIdDirs}/{artifactId}/{baseVersion}/{artifactId}-{version}-{classifier}.{extension}" );
        assertEquals( "org/apache/maven/maven-model/3.0-SNAPSHOT/maven-model-3.0-20100720.132618-1.jar",
                      layout.getPath( new DefaultArtifact( "org.apache.maven:maven-model:3.0-20100720.132618-1" ) ) );

        layout = new Layout( "{groupId}/{artifactId}-{version}-{classifier}.{extension}" );
        assertEquals( "org.apache.maven/maven-model-3.0-sources.jar",
                      layout.getPath( new DefaultArtifact( "org.apache.maven:maven-model:3.0:jar:sources" ) ) );
    }

}

package org.sonatype.aether.util.graph;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.aether.Artifact;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

/**
 * Generates a sequence of dependency nodes from a dependeny graph by traversing the graph in preorder.
 * 
 * @author Benjamin Bentmann
 */
public class PreorderNodeListGenerator
    implements DependencyVisitor
{

    private List<DependencyNode> nodes;

    /**
     * Creates a new list generator.
     */
    public PreorderNodeListGenerator()
    {
        this.nodes = new ArrayList<DependencyNode>( 128 );
    }

    /**
     * Gets the list of dependency nodes that was generated during the graph traversal.
     * 
     * @return The list of dependency nodes in preorder, never {@code null}.
     */
    public List<DependencyNode> getNodes()
    {
        return nodes;
    }

    /**
     * Gets the artifacts associated with the list of dependency nodes generated during the graph traversal.
     * 
     * @param includeUnresolved Whether unresolved artifacts shall be included in the result or not.
     * @return The list of artifacts in preorder, never {@code null}.
     */
    public List<Artifact> getArtifacts( boolean includeUnresolved )
    {
        List<Artifact> artifacts = new ArrayList<Artifact>( getNodes().size() );

        for ( DependencyNode node : getNodes() )
        {
            if ( node.getDependency() != null )
            {
                Artifact artifact = node.getDependency().getArtifact();
                if ( includeUnresolved || artifact.getFile() != null )
                {
                    artifacts.add( artifact );
                }
            }
        }

        return artifacts;
    }

    /**
     * Gets a class path by concatenating the artifact files of the visited dependency nodes. Nodes with unresolved
     * artifacts are automatically skipped.
     * 
     * @return The class path, using the platform-specific path separator, never {@code null}.
     */
    public String getClassPath()
    {
        StringBuilder buffer = new StringBuilder( 1024 );

        for ( Iterator<DependencyNode> it = getNodes().iterator(); it.hasNext(); )
        {
            DependencyNode node = it.next();
            if ( node.getDependency() != null )
            {
                Artifact artifact = node.getDependency().getArtifact();
                if ( artifact.getFile() != null )
                {
                    buffer.append( artifact.getFile().getAbsolutePath() );
                    if ( it.hasNext() )
                    {
                        buffer.append( File.pathSeparatorChar );
                    }
                }
            }
        }

        return buffer.toString();
    }

    public boolean visitEnter( DependencyNode node )
    {
        if ( node.getDependency() != null )
        {
            nodes.add( node );
        }

        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        return true;
    }

}

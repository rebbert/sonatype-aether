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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.DependencyVisitor;

/**
 * @author Benjamin Bentmann
 */
class DependencyGraphLogger
    implements DependencyVisitor
{

    private Task task;

    private String indent = "";

    public DependencyGraphLogger( Task task )
    {
        this.task = task;
    }

    public boolean visitEnter( DependencyNode node )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( indent );
        org.sonatype.aether.Dependency dep = node.getDependency();
        if ( dep != null )
        {
            org.sonatype.aether.Artifact art = dep.getArtifact();

            buffer.append( art );
            buffer.append( ':' ).append( dep.getScope() );

            if ( node.getPremanagedScope() != null && !node.getPremanagedScope().equals( dep.getScope() ) )
            {
                buffer.append( " (scope managed from " ).append( node.getPremanagedScope() ).append( ")" );
            }

            if ( node.getPremanagedVersion() != null && !node.getPremanagedVersion().equals( art.getVersion() ) )
            {
                buffer.append( " (version managed from " ).append( node.getPremanagedVersion() ).append( ")" );
            }
        }
        else
        {
            buffer.append( "Resolved Dependency Graph:" );
        }

        task.log( buffer.toString(), Project.MSG_VERBOSE );
        indent += "   ";
        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        indent = indent.substring( 0, indent.length() - 3 );
        return true;
    }

}

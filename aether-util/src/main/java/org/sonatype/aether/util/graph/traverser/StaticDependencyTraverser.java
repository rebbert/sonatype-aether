package org.sonatype.aether.util.graph.traverser;

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

import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyCollectionContext;
import org.sonatype.aether.DependencyTraverser;

/**
 * A dependency traverser with always or never traverses children.
 * 
 * @author Benjamin Bentmann
 */
public class StaticDependencyTraverser
    implements DependencyTraverser
{

    private final boolean traverse;

    /**
     * Creates a new traverser with the specified traversal behavior.
     * 
     * @param traverse {@code true} to traverse all dependencies, {@code false} to never traverse.
     */
    public StaticDependencyTraverser( boolean traverse )
    {
        this.traverse = traverse;
    }

    public boolean traverseDependency( Dependency dependency )
    {
        return traverse;
    }

    public DependencyTraverser deriveChildTraverser( DependencyCollectionContext context )
    {
        return this;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        else if ( null == obj || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        StaticDependencyTraverser that = (StaticDependencyTraverser) obj;
        return traverse == that.traverse;
    }

    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + ( traverse ? 1 : 0 );
        return hash;
    }

}

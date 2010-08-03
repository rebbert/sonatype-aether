package org.sonatype.aether.ant.types;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * @author Benjamin Bentmann
 */
public class Artifacts
    extends DataType
    implements ArtifactContainer
{

    private List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>();

    protected Artifacts getRef()
    {
        return (Artifacts) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            for ( ArtifactContainer container : containers )
            {
                container.validate( task );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( !containers.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void addArtifact( Artifact artifact )
    {
        checkChildrenAllowed();
        containers.add( artifact );
    }

    public void addArtifacts( Artifacts artifacts )
    {
        checkChildrenAllowed();
        if ( artifacts == this )
        {
            throw circularReference();
        }
        containers.add( artifacts );
    }

    public List<Artifact> getArtifacts()
    {
        if ( isReference() )
        {
            return getRef().getArtifacts();
        }
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( ArtifactContainer container : containers )
        {
            artifacts.addAll( container.getArtifacts() );
        }
        return artifacts;
    }

}

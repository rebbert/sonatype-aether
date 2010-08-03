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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * @author Benjamin Bentmann
 */
public class Artifact
    extends DataType
    implements ArtifactContainer
{

    private File file;

    private String type;

    private String classifier;

    protected Artifact getRef()
    {
        return (Artifact) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( file == null )
            {
                throw new BuildException( "You must specify the 'file' for the artifact" );
            }
            else if ( !file.isFile() )
            {
                throw new BuildException( "The artifact file " + file + " does not exist" );
            }
            if ( type == null || type.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'type' for the artifact" );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( file != null || type != null || classifier != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public File getFile()
    {
        if ( isReference() )
        {
            return getRef().getFile();
        }
        return file;
    }

    public void setFile( File file )
    {
        checkAttributesAllowed();
        this.file = file;

        if ( file != null && type == null )
        {
            String name = file.getName();
            int period = name.lastIndexOf( '.' );
            if ( period >= 0 )
            {
                type = name.substring( period + 1 );
            }
        }
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return ( type != null ) ? type : "";
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        this.type = type;
    }

    public String getClassifier()
    {
        if ( isReference() )
        {
            return getRef().getClassifier();
        }
        return ( classifier != null ) ? classifier : "";
    }

    public void setClassifier( String classifier )
    {
        checkAttributesAllowed();
        this.classifier = classifier;
    }

    public List<Artifact> getArtifacts()
    {
        return Collections.singletonList( this );
    }

}

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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.ant.AntRepoSys;

/**
 * @author Benjamin Bentmann
 */
public class LocalRepository
    extends DataType
{

    private final Task task;

    private File dir;

    public LocalRepository()
    {
        this( null );
    }

    public LocalRepository( Task task )
    {
        this.task = task;
    }

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        if ( task == null )
        {
            AntRepoSys.getInstance( project ).setLocalRepository( this );
        }
    }

    protected LocalRepository getRef()
    {
        return (LocalRepository) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( dir != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public File getDir()
    {
        if ( isReference() )
        {
            return getRef().getDir();
        }
        return dir;
    }

    public void setDir( File dir )
    {
        checkAttributesAllowed();
        this.dir = dir;
    }

}

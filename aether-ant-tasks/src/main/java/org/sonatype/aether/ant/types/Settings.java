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

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.ant.AntRepoSys;

/**
 * @author Benjamin Bentmann
 */
public class Settings
    extends DataType
{

    private File file;

    private File globalFile;

    protected Settings getRef()
    {
        return (Settings) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( file != null || globalFile != null )
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

        AntRepoSys.getInstance( getProject() ).setUserSettings( globalFile );
    }

    public File getGlobalFile()
    {
        if ( isReference() )
        {
            return getRef().getFile();
        }
        return globalFile;
    }

    public void setGlobalFile( File globalFile )
    {
        checkAttributesAllowed();
        this.globalFile = globalFile;

        AntRepoSys.getInstance( getProject() ).setGlobalSettings( globalFile );
    }

}

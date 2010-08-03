package org.sonatype.aether.ant;

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
import org.sonatype.aether.spi.log.Logger;

/**
 * @author Benjamin Bentmann
 */
class AntLogger
    implements Logger
{

    private Project project;

    public AntLogger( Project project )
    {
        this.project = project;
    }

    public void debug( String msg )
    {
        project.log( msg, Project.MSG_DEBUG );
    }

    public void debug( String msg, Throwable error )
    {
        project.log( msg, error, Project.MSG_DEBUG );
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

}

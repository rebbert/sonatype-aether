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
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;

/**
 * @author Benjamin Bentmann
 */
class AntServiceLocator
    extends DefaultServiceLocator
{

    private Project project;

    public AntServiceLocator( Project project )
    {
        this.project = project;
    }

    @Override
    protected void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
    {
        String msg = "Could not initialize repository system";
        if ( !RepositorySystem.class.equals( type ) )
        {
            msg += ", service " + type.getName() + " (" + impl.getName() + ") failed to initialize";
        }
        msg += ": " + exception.getMessage();
        project.log( msg, exception, Project.MSG_ERR );
    }

}

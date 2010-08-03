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
import org.apache.tools.ant.Task;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.util.listener.AbstractRepositoryListener;

/**
 * @author Benjamin Bentmann
 */
class AntRepositoryListener
    extends AbstractRepositoryListener
{

    private Task task;

    public AntRepositoryListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        task.log( "Installing " + event.getFile() );
    }

    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is invalid"
            + ", transitive dependencies (if any) will not be available: " + event.getException().getMessage(),
                  event.getException(), Project.MSG_WARN );
    };

    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is missing, no dependency information available",
                  Project.MSG_WARN );
    };

}

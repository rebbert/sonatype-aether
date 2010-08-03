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
import org.sonatype.aether.TransferCancelledException;
import org.sonatype.aether.TransferEvent;
import org.sonatype.aether.util.listener.AbstractTransferListener;

/**
 * @author Benjamin Bentmann
 */
class AntTransferListener
    extends AbstractTransferListener
{

    private Task task;

    public AntTransferListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void transferInitiated( TransferEvent event )
        throws TransferCancelledException
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        task.log( msg );
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        task.log( msg );
    }

    @Override
    public void transferFailed( TransferEvent event )
    {
        String msg = "Failed to ";
        msg += event.getRequestType() == TransferEvent.RequestType.PUT ? "upload" : "download";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        msg += ": " + event.getException().getMessage();
        task.log( msg, event.getException(), Project.MSG_ERR );
    }

}

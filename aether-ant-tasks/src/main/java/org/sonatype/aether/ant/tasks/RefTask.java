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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * @author Benjamin Bentmann
 */
public abstract class RefTask
    extends Task
{

    private Reference ref;

    public boolean isReference()
    {
        return ref != null;
    }

    public void setRefid( final Reference ref )
    {
        this.ref = ref;
    }

    protected void checkAttributesAllowed()
    {
        if ( isReference() )
        {
            throw tooManyAttributes();
        }
    }

    protected void checkChildrenAllowed()
    {
        if ( isReference() )
        {
            throw noChildrenAllowed();
        }
    }

    protected BuildException tooManyAttributes()
    {
        return new BuildException( "You must not specify more than one " + "attribute when using refid" );
    }

    protected BuildException noChildrenAllowed()
    {
        return new BuildException( "You must not specify nested elements " + "when using refid" );
    }

    protected String getDataTypeName()
    {
        return ComponentHelper.getElementName( getProject(), this, true );
    }

    protected Object getCheckedRef()
    {
        return getCheckedRef( getClass(), getDataTypeName(), getProject() );
    }

    protected Object getCheckedRef( final Class<?> requiredClass, final String dataTypeName, final Project project )
    {
        if ( project == null )
        {
            throw new BuildException( "No Project specified" );
        }
        Object o = ref.getReferencedObject( project );
        if ( !( requiredClass.isAssignableFrom( o.getClass() ) ) )
        {
            log( "Class " + o.getClass() + " is not a subclass of " + requiredClass, Project.MSG_VERBOSE );
            String msg = ref.getRefId() + " doesn\'t denote a " + dataTypeName;
            throw new BuildException( msg );
        }
        return o;
    }

}

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

import org.apache.tools.ant.PropertyHelper;

/**
 * @author Benjamin Bentmann
 */
@SuppressWarnings( "deprecation" )
class PomPropertyHelper
    extends PropertyHelper
{

    private final ModelValueExtractor extractor;

    public static void register( ModelValueExtractor extractor, PropertyHelper propertyHelper )
    {
        PomPropertyHelper helper = new PomPropertyHelper( extractor );
        helper.setNext( propertyHelper.getNext() );
        propertyHelper.setNext( helper );
    }

    public PomPropertyHelper( ModelValueExtractor extractor )
    {
        if ( extractor == null )
        {
            throw new IllegalArgumentException( "no model value exractor specified" );
        }
        this.extractor = extractor;
        setProject( extractor.getProject() );
    }

    @Override
    public Object getPropertyHook( String ns, String name, boolean user )
    {
        Object value = extractor.getValue( name );
        if ( value != null )
        {
            return value;
        }
        else if ( extractor.isApplicable( name ) )
        {
            return null;
        }
        return super.getPropertyHook( ns, name, user );
    }

}

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

import org.apache.maven.model.Model;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.interpolation.reflection.ReflectionValueExtractor;

/**
 * @author Benjamin Bentmann
 */
class ModelValueExtractor
{

    private static final String PREFIX_PROPERTIES = "properties.";

    private final String prefix;

    private final Project project;

    private final Model model;

    public ModelValueExtractor( String prefix, Model model, Project project )
    {
        if ( model == null )
        {
            throw new IllegalArgumentException( "reference to Maven POM has not been specified" );
        }
        if ( project == null )
        {
            throw new IllegalArgumentException( "reference to Ant project has not been specified" );
        }
        if ( prefix == null || prefix.length() <= 0 )
        {
            prefix = "pom.";
        }
        else if ( !prefix.endsWith( "." ) )
        {
            prefix += '.';
        }
        this.prefix = prefix;
        this.model = model;
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isApplicable( String expression )
    {
        return expression.startsWith( prefix );
    }

    public Object getValue( String expression )
    {
        if ( expression.startsWith( prefix ) )
        {
            String expr = expression.substring( prefix.length() );
            try
            {
                if ( expr.startsWith( PREFIX_PROPERTIES ) )
                {
                    String key = expr.substring( PREFIX_PROPERTIES.length() );
                    return model.getProperties().getProperty( key );
                }

                return ReflectionValueExtractor.evaluate( expr, model, false );
            }
            catch ( Exception e )
            {
                project.log( "Could not retrieve '" + expression + "' from POM: " + e.getMessage(), e, Project.MSG_WARN );
                return null;
            }
        }
        else
        {
            return null;
        }
    }

}

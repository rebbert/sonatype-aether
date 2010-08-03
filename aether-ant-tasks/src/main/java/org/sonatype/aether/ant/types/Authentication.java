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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.ant.AntRepoSys;

/**
 * @author Benjamin Bentmann
 */
public class Authentication
    extends DataType
{

    private String username;

    private String password;

    private String privateKeyFile;

    private String passphrase;

    private List<String> servers = new ArrayList<String>();

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        AntRepoSys.getInstance( project ).addAuthentication( this );
    }

    protected Authentication getRef()
    {
        return (Authentication) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( username != null || password != null || privateKeyFile != null || passphrase != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public String getUsername()
    {
        if ( isReference() )
        {
            return getRef().getUsername();
        }
        return username;
    }

    public void setUsername( String username )
    {
        checkAttributesAllowed();
        this.username = username;
    }

    public String getPassword()
    {
        if ( isReference() )
        {
            return getRef().getPassword();
        }
        return password;
    }

    public void setPassword( String password )
    {
        checkAttributesAllowed();
        this.password = password;
    }

    public String getPrivateKeyFile()
    {
        if ( isReference() )
        {
            return getRef().getPrivateKeyFile();
        }
        return privateKeyFile;
    }

    public void setPrivateKeyFile( String privateKeyFile )
    {
        checkAttributesAllowed();
        this.privateKeyFile = privateKeyFile;
    }

    public String getPassphrase()
    {
        if ( isReference() )
        {
            return getRef().getPassphrase();
        }
        return passphrase;
    }

    public void setPassphrase( String passphrase )
    {
        checkAttributesAllowed();
        this.passphrase = passphrase;
    }

    public List<String> getServers()
    {
        if ( isReference() )
        {
            return getRef().getServers();
        }
        return servers;
    }

    public void setServers( String servers )
    {
        checkAttributesAllowed();
        this.servers.clear();
        String[] split = servers.split( "[;:]" );
        for ( String server : split )
        {
            server = server.trim();
            if ( server.length() > 0 )
            {
                this.servers.add( server );
            }
        }
    }

}

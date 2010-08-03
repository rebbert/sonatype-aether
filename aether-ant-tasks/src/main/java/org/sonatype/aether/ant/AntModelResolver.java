package org.sonatype.aether.ant;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.ArtifactRequest;
import org.sonatype.aether.ArtifactResolutionException;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositoryPolicy;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;

/**
 * A model resolver to assist building of dependency POMs. This resolver gives priority to those repositories that have
 * been initially specified and repositories discovered in dependency POMs are recessively merged into the search chain.
 * 
 * @author Benjamin Bentmann
 */
class AntModelResolver
    implements ModelResolver
{

    private final RepositorySystemSession session;

    private final String context;

    private List<RemoteRepository> repositories;

    private final RepositorySystem repoSys;

    private final RemoteRepositoryManager remoteRepositoryManager;

    private final Set<String> repositoryIds;

    public AntModelResolver( RepositorySystemSession session, String context, RepositorySystem repoSys,
                             RemoteRepositoryManager remoteRepositoryManager, List<RemoteRepository> repositories )
    {
        this.session = session;
        this.context = context;
        this.repoSys = repoSys;
        this.remoteRepositoryManager = remoteRepositoryManager;
        this.repositories = repositories;
        this.repositoryIds = new HashSet<String>();
    }

    private AntModelResolver( AntModelResolver original )
    {
        this.session = original.session;
        this.context = original.context;
        this.repoSys = original.repoSys;
        this.remoteRepositoryManager = original.remoteRepositoryManager;
        this.repositories = original.repositories;
        this.repositoryIds = new HashSet<String>( original.repositoryIds );
    }

    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        if ( !repositoryIds.add( repository.getId() ) )
        {
            return;
        }

        List<RemoteRepository> newRepositories = Collections.singletonList( convert( repository ) );

        this.repositories =
            remoteRepositoryManager.aggregateRepositories( session, repositories, newRepositories, true );
    }

    static RemoteRepository convert( Repository repository )
    {
        RemoteRepository result =
            new RemoteRepository( repository.getId(), repository.getLayout(), repository.getUrl() );
        result.setPolicy( true, convert( repository.getSnapshots() ) );
        result.setPolicy( false, convert( repository.getReleases() ) );
        return result;
    }

    private static RepositoryPolicy convert( org.apache.maven.model.RepositoryPolicy policy )
    {
        boolean enabled = true;
        String checksums = RepositoryPolicy.CHECKSUM_POLICY_WARN;
        String updates = RepositoryPolicy.UPDATE_POLICY_DAILY;

        if ( policy != null )
        {
            enabled = policy.isEnabled();
            if ( policy.getUpdatePolicy() != null )
            {
                updates = policy.getUpdatePolicy();
            }
            if ( policy.getChecksumPolicy() != null )
            {
                checksums = policy.getChecksumPolicy();
            }
        }

        return new RepositoryPolicy( enabled, updates, checksums );
    }

    public ModelResolver newCopy()
    {
        return new AntModelResolver( this );
    }

    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "", "pom", version );

        try
        {
            ArtifactRequest request = new ArtifactRequest( pomArtifact, repositories, context );
            pomArtifact = repoSys.resolveArtifact( session, request ).getArtifact();
        }
        catch ( ArtifactResolutionException e )
        {
            throw new UnresolvableModelException( "Failed to resolve POM for " + groupId + ":" + artifactId + ":"
                + version + " due to " + e.getMessage(), groupId, artifactId, version, e );
        }

        File pomFile = pomArtifact.getFile();

        return new FileModelSource( pomFile );
    }

}

/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package com.redhat.depassembler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import com.redhat.depassemblerutils.Booter;

/**
 * Resolves the transitive (compile) dependencies of an artifact.
 * 
 * 
 */
public class DependencyAssembler
{
	/* Logging */
	private static final Logger log = Logger.getLogger(DependencyAssembler.class.getName());
	
    public static void main( String[] args )throws Exception
    {
    	/*
			-p "C:/Users/sgeevarg/Desktop/Final/dependency-assembler.properties"
			-p "C:/Users/sgeevarg/Desktop/Final/dependency-assembler.properties"
		*/

    	Options options = new Options();
    	
    	cliSetup(options);
    	String propertiesFile = cliParse(options, args);

    	Properties properties = readConfig(propertiesFile);
    	Map<String, String> remoteRepo= new HashMap<String, String>();
    	String localRepo = null;
    	String artifact= null;
    	
    	if (properties.size() > 0) {
    		remoteRepo.put("RepoId", validateProperty(properties, "RepoId"));
    		remoteRepo.put("RepoType", validateProperty(properties, "RepoType"));
    		remoteRepo.put("RepoUrl", validateProperty(properties, "RepoUrl"));

    		localRepo = validateProperty(properties, "LocalRepoFolder");

    		artifact = validateProperty(properties, "Artifact");
    	}
     	
    	DependencyAssembler.execute(remoteRepo, localRepo, artifact);
    	
    }
    
    public static void execute(Map<String, String> remoteRepo, String localRepoFolder, String artifactRequest) throws Exception{
    	
        System.out.println( "------------------------------------------------------------" );
        System.out.println( DependencyAssembler.class.getSimpleName() );

        RepositorySystem system = Booter.newRepositorySystem();

        RepositorySystemSession session = Booter.newRepositorySystemSession( system, localRepoFolder );

        // Artifact artifact = new DefaultArtifact( "org.eclipse.aether:aether-impl:1.0.0.v20140518" );
        Artifact artifact = new DefaultArtifact( artifactRequest );

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
        collectRequest.setRepositories( Booter.newRepositories( system, session, remoteRepo ) );

        DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, classpathFlter );

        List<ArtifactResult> artifactResults =
            system.resolveDependencies( session, dependencyRequest ).getArtifactResults();

        for ( ArtifactResult artifactResult : artifactResults )
        {
            System.out.println( artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile() );
        }
    }
    
    
    //
    // Read of Properties
    //
    private static Properties readConfig(String fileName) {
		Properties properties = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(fileName);
			//load properties file
			properties.load(input);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Unable to access file: " + fileName + ", Error: " + e);
		} finally {
			if (input != null){
				try {
					input.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Unable to close file: " + fileName + ", Error: " + e);
				}
			}
		}
		
		return properties;
	}

    //
    // Validate Properties
    //
    private static String validateProperty(final Properties properties, final String propertyName) throws Exception {

    	String propertyValue = properties.getProperty(propertyName);
	
    	if (isNotNullOrEmptyString(propertyValue)) {
    		return propertyValue;
    	} else {
    		throw new Exception ("Required Property name is not provided:" + propertyName);
    	}
    	
	}
    
    public static boolean isNotNullOrEmptyString(final String string)
	{
	   return string != null && !string.isEmpty() && !string.trim().isEmpty() && (string.length() > 0);
	}    


    private static void cliSetup(Options options) {
		options.addOption("p", "properties", true, "Properties File with Full Directory Path");
	}

    private static String cliParse(Options options, String[] args) {
		
    	String propertiesfile = null;
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
			
			if (cmd.hasOption("h")) {
				cliHelp(options);
			}
			
			if (cmd.hasOption("p")){
				propertiesfile=cmd.getOptionValue("p");
			} else {
				log.log (Level.SEVERE, "You will need to provide the properties file");
				cliHelp(options);
			}
			
		} catch (ParseException e){
			log.log(Level.INFO, "Unable to parse command line arguments", e);
		}
		
		return propertiesfile;
	}

    private static void cliHelp(Options options) {
		HelpFormatter helpFormatter = new HelpFormatter();
		
		String footer = "The properties file will also need to contain the following information:";
		
		footer += "RepoId=\"central\"\nRepoType=\"default\"\nRepoUrl=\"http://central.maven.org/maven2/\"\n\n";
		footer += "LocalRepoFolder=\"/root/target/local-repo\"\n\n";
		footer += "Artifact=\"com.amazonaws:aws-java-sdk-osgi:1.10.0\"";
		
		// helpFormatter.printHelp("DependencyAssembler", options);
		helpFormatter.printHelp("DependencyAssembler", "", options, footer);
		
		System.exit(0);
		
	}
	

    
}
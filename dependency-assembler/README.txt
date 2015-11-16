>> If using the Application Container Properties (standalone.xml):

-DRepoId="central" -DRepoType="default" -DRepoUrl="http://central.maven.org/maven2/"
Code: return new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build();

id		-	Idenitity of the Repository, may be null	:	central 
type		-	Type of the Repository, may be null		:	default
	  		layout - legacy (Maven 1) or default (Maven 2)
url		-	The base URL of the Repository			: central

-DLocalRepo="/root/target/local-repo"
Code: LocalRepository localRepo = new LocalRepository( "target/local-repo" );

-DArtifact="com.amazonaws:aws-java-sdk-osgi:1.10.0"
Code: Artifact artifact = new DefaultArtifact( "com.amazonaws:aws-java-sdk-osgi:1.10.0" );

>> If using Command line include the properties file:

java -jar DependencyAssembler.jar -p "/tmp/domain/dependency-assembler.properties"
and the properties file will contain the properties

RepoId=central
RepoType=default
RepoUrl=http://central.maven.org/maven2/

LocalRepoFolder=/root/target/local-repo
LocalRepoFolderWin=C:/root/target/local-repo

Artifact=com.amazonaws:aws-java-sdk-osgi:1.10.0

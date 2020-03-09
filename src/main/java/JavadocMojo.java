import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.databind.ObjectMapper;

import entity.ApiEntity;
import parser.ControllerParser;

/**
 * mvn cimc:javadoc && mvn package -Dmaven.test.skip=true || mvn package -Dmaven.test.skip=true
 */

@Mojo(name = "javadoc", requiresProject = true, threadSafe = true, defaultPhase = LifecyclePhase.NONE)
public class JavadocMojo extends AbstractMojo {
	
	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(property = "fileName", defaultValue = "api.json")
	private String fileName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = this.getLog();

		log.info( String.format("Found project  : %s", project.getBasedir().getAbsolutePath()));
		
		List<String> sources = project.getCompileSourceRoots();
		List<Resource> resources = project.getResources();

		if (null == sources || sources.isEmpty())
			return;
		if (null == resources || resources.isEmpty())
			return;

		String source = sources.get(0);
		String resource = resources.get(0).getDirectory();

		log.info( String.format("Found source   : %s",source));
		log.info( String.format("Found resource : %s",resource));
		
		ControllerParser parser = new ControllerParser();
		List<ApiEntity> apis = parser.parse(source);
		
		if( null== apis)
			return;

		log.info( String.format("Total javadoc  : %d", apis.size()));

		try {

			String text = new ObjectMapper().writeValueAsString(apis);
			Files.write(
					Paths.get(resource, this.fileName), 
					text.getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE)
			;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

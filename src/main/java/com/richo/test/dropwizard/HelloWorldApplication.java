package com.richo.test.dropwizard;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.poorknight.persistence.RecipeRepository;
import com.richo.test.dropwizard.api.HelloWorldApi;
import com.richo.test.dropwizard.api.HelloWorldResource;
import com.richo.test.dropwizard.api.RecipeEndpoint;
import com.richo.test.dropwizard.api.RecipeSearchStringParser;
import com.richo.test.dropwizard.api.RecipeTranslator;
import com.richo.test.dropwizard.filter.MyFilter;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(final String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	@Override
	public String getName() {
		return "hello-world";
	}

	@Override
	public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
		final AssetsBundle assetsBundle = new AssetsBundle("/assets/", "/", "index.html", "static");
		bootstrap.addBundle(assetsBundle);
	}

	@Override
	public void run(final HelloWorldConfiguration configuration, final Environment environment) {
		enableWadl(environment);

		String mongoLocation = System.getenv("MONGO_LOCATION");
		if (mongoLocation == null) {
			mongoLocation = "mongodb";
		}
		logger.info("Connecting to MongoDB found at: " + mongoLocation);

		final MongoClient mongoClient = new MongoClient(mongoLocation);
		MongoSetup.setupDatabaseCollections(mongoClient);

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);

		environment.getApplicationContext().addFilter(MyFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

		environment.admin().addTask(new MyTestTask());

		final HelloWorldApi resource = new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName(), mongoClient);
		environment.jersey().register(resource);

		final RecipeRepository recipeRepository = new RecipeRepository(mongoClient);
		final RecipeSearchStringParser recipeSearchStringParser = new RecipeSearchStringParser();
		final RecipeTranslator recipeTranslator = new RecipeTranslator();
		final RecipeEndpoint recipeEndpoint = new RecipeEndpoint(recipeRepository, recipeTranslator, recipeSearchStringParser);
		environment.jersey().register(recipeEndpoint);
	}

	private void enableWadl(final Environment environment) {
		final Map<String, Object> props = new HashMap<>();
		props.put("jersey.config.server.wadl.disableWadl", "false");
		environment.jersey().getResourceConfig().addProperties(props);
	}
}
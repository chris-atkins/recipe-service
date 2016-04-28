package com.richo.test.dropwizard;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.poorknight.persistence.RecipeRepository;
import com.richo.test.dropwizard.api.HelloWorldApi;
import com.richo.test.dropwizard.api.HelloWorldResource;
import com.richo.test.dropwizard.api.RecipeEndpoint;
import com.richo.test.dropwizard.api.RecipeSearchStringParser;
import com.richo.test.dropwizard.api.RecipeTranslator;
import com.richo.test.dropwizard.api.TextToHtmlTranformer;
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
		return "recipe-connection";
	}

	@Override
	public void initialize(final Bootstrap<HelloWorldConfiguration> bootstrap) {
		final AssetsBundle assetsBundle = new AssetsBundle("/assets/", "/", "index.html", "static");
		bootstrap.addBundle(assetsBundle);
	}

	@Override
	public void run(final HelloWorldConfiguration configuration, final Environment environment) {
		enableWadl(environment);

		final MongoClient mongoClient = connectToDatabase();

		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		environment.healthChecks().register("template", healthCheck);

		environment.getApplicationContext().addFilter(MyFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
		environment.admin().addTask(new MyTestTask());

		final HelloWorldApi resource = new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName(), mongoClient);
		environment.jersey().register(resource);

		final RecipeRepository recipeRepository = new RecipeRepository(mongoClient);
		final RecipeSearchStringParser recipeSearchStringParser = new RecipeSearchStringParser();
		final RecipeTranslator recipeTranslator = new RecipeTranslator();
		final TextToHtmlTranformer textToHtmlTransformer = new TextToHtmlTranformer();
		final RecipeEndpoint recipeEndpoint = new RecipeEndpoint(recipeRepository, recipeTranslator, recipeSearchStringParser, textToHtmlTransformer);
		environment.jersey().register(recipeEndpoint);
	}

	private MongoClient connectToDatabase() {
		String mongoLocation = System.getenv("MONGO_LOCATION");
		if (mongoLocation == null) {
			mongoLocation = "mongodb";
		}

		final MongoClient mongoClient = connectToDatabase(mongoLocation);
		MongoSetup.setupDatabaseCollections(mongoClient);
		return mongoClient;
	}

	private MongoClient connectToDatabase(final String mongoLocation) {
		final String user = System.getenv("MONGO_USER");
		final String password = System.getenv("MONGO_PASSWORD");

		if (user == null && password == null) {
			return setupNoAuthDatabaseConnection(mongoLocation);
		}
		return setupAuthenticatedDatabaseConnection(mongoLocation, user, password);
	}

	private MongoClient setupNoAuthDatabaseConnection(final String mongoLocation) {
		logger.info("Connecting with no auth to MongoDB found at: " + mongoLocation);
		return new MongoClient(mongoLocation);
	}

	private MongoClient setupAuthenticatedDatabaseConnection(final String mongoLocation, final String user, final String password) {
		logger.info("Connecting securely to MongoDB found at: " + mongoLocation);
		final char[] passwordChars = password == null ? null : password.toCharArray();
		final MongoCredential credential = MongoCredential.createCredential(user, "admin", passwordChars);
		return new MongoClient(new ServerAddress(mongoLocation), Collections.singletonList(credential));
	}

	private void enableWadl(final Environment environment) {
		final Map<String, Object> props = new HashMap<>();
		props.put("jersey.config.server.wadl.disableWadl", "false");
		environment.jersey().getResourceConfig().addProperties(props);
	}
}
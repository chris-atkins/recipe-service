package com.poorknight.app;

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
import com.poorknight.api.RecipeEndpoint;
import com.poorknight.api.UserEndpoint;
import com.poorknight.app.filters.MyFilter;
import com.poorknight.app.init.MetricsInitializer;
import com.poorknight.app.init.MongoSetup;
import com.poorknight.persistence.RecipeRepository;
import com.poorknight.persistence.UserRepository;
import com.poorknight.recipe.save.TextToHtmlTranformer;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.transform.api.domain.RecipeTranslator;
import com.poorknight.transform.api.domain.UserTranslator;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class RecipeServiceApplication extends Application<RecipeServiceConfiguration> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(final String[] args) throws Exception {
		new RecipeServiceApplication().run(args);
	}

	@Override
	public String getName() {
		return "recipe-connection";
	}

	@Override
	public void initialize(final Bootstrap<RecipeServiceConfiguration> bootstrap) {
		final AssetsBundle assetsBundle = new AssetsBundle("/assets/", "/", "index.html", "static");
		bootstrap.addBundle(assetsBundle);
		startMetricCollection(bootstrap);
	}

	@Override
	public void run(final RecipeServiceConfiguration configuration, final Environment environment) {
		enableWadl(environment);
		final MongoClient mongoClient = connectToDatabase();
		environment.getApplicationContext().addFilter(MyFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

		final RecipeEndpoint recipeEndpoint = initializeRecipeEndpoint(mongoClient);
		final UserEndpoint userEndpoint = initializeUserEndpoint(mongoClient);

		environment.jersey().register(recipeEndpoint);
		environment.jersey().register(userEndpoint);
	}

	private RecipeEndpoint initializeRecipeEndpoint(final MongoClient mongoClient) {
		final RecipeRepository recipeRepository = new RecipeRepository(mongoClient);
		final RecipeSearchStringParser recipeSearchStringParser = new RecipeSearchStringParser();
		final RecipeTranslator recipeTranslator = new RecipeTranslator();
		final TextToHtmlTranformer textToHtmlTransformer = new TextToHtmlTranformer();
		final RecipeEndpoint recipeEndpoint = new RecipeEndpoint(recipeRepository, recipeTranslator, recipeSearchStringParser, textToHtmlTransformer);
		return recipeEndpoint;
	}

	private UserEndpoint initializeUserEndpoint(final MongoClient mongoClient) {
		final UserTranslator userTranslator = new UserTranslator();
		final UserRepository userRepository = new UserRepository(mongoClient, userTranslator);
		final UserEndpoint userEndpoint = new UserEndpoint(userRepository, userTranslator);
		return userEndpoint;
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

	private void startMetricCollection(final Bootstrap<RecipeServiceConfiguration> bootstrap) {
		final String metricsUrl = System.getenv("METRICS_REPOSITORY_LOCATION");
		final String metricsPort = System.getenv("METRICS_REPOSITORY_PORT");

		MetricsInitializer.initializeApplicationMetrics(metricsUrl, metricsPort, bootstrap);
	}
}
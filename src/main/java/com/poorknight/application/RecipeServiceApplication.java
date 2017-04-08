package com.poorknight.application;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.poorknight.api.RecipeBookEndpoint;
import com.poorknight.api.RecipeEndpoint;
import com.poorknight.api.RecipeImageEndpoint;
import com.poorknight.api.UserEndpoint;
import com.poorknight.application.init.MetricsInitializer;
import com.poorknight.application.init.MongoSetup;
import com.poorknight.recipe.RecipeBookToRecipeTranslator;
import com.poorknight.recipe.RecipeRepository;
import com.poorknight.recipe.RecipeTranslator;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipebook.RecipeBookRepository;
import com.poorknight.recipebook.RecipeBookTranslator;
import com.poorknight.user.UserRepository;
import com.poorknight.user.UserTranslator;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
//		environment.getApplicationContext().addFilter(MyFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

		final UserEndpoint userEndpoint = initializeUserEndpoint(mongoClient);
		final RecipeBookEndpoint recipeBookEndpoint = initializeRecipeBookEndpoint(mongoClient);
		final RecipeEndpoint recipeEndpoint = initializeRecipeEndpoint(mongoClient, recipeBookEndpoint);

		environment.jersey().register(recipeEndpoint);
		environment.jersey().register(userEndpoint);
		environment.jersey().register(recipeBookEndpoint);
		environment.jersey().register(MultiPartFeature.class);
		environment.jersey().register(new RecipeImageEndpoint());

		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");  //Allows CORS headers to be returned
	}

	private RecipeEndpoint initializeRecipeEndpoint(final MongoClient mongoClient, final RecipeBookEndpoint recipeBookEndpoint) {
		final RecipeRepository recipeRepository = new RecipeRepository(mongoClient);
		final RecipeSearchStringParser recipeSearchStringParser = new RecipeSearchStringParser();
		final RecipeTranslator recipeTranslator = new RecipeTranslator();
		final RecipeBookToRecipeTranslator recipeBookToRecipeTranslator = new RecipeBookToRecipeTranslator();
		return new RecipeEndpoint(recipeRepository, recipeTranslator, recipeSearchStringParser, recipeBookEndpoint, recipeBookToRecipeTranslator);
	}

	private UserEndpoint initializeUserEndpoint(final MongoClient mongoClient) {
		final UserTranslator userTranslator = new UserTranslator();
		final UserRepository userRepository = new UserRepository(mongoClient, userTranslator);
		return new UserEndpoint(userRepository, userTranslator);
	}

	private RecipeBookEndpoint initializeRecipeBookEndpoint(final MongoClient mongoClient) {
		final RecipeBookRepository recipeBookRepository = new RecipeBookRepository(mongoClient);
		final RecipeBookTranslator recipeBookTranslator = new RecipeBookTranslator();
		return new RecipeBookEndpoint(recipeBookRepository, recipeBookTranslator);
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
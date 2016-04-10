package com.poorknight.mongo.setup;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoSetupHelper {

	private static final String MONGO_VERSION = "3.2.4";

	private static MongodExecutable mongodExecutable = null;
	private static MongoClient mongo;
	private static MongodProcess mongod;

	public static MongoClient startMongoInstance() throws Exception {
		final MongodStarter starter = MongodStarter.getDefaultInstance();
		final int port = Network.getFreeServerPort();
		final IMongodConfig mongodConfig = new MongodConfigBuilder() //
				.version(Versions.withFeatures(new GenericVersion(MONGO_VERSION), Feature.SYNC_DELAY)) //
				.net(new Net(port, Network.localhostIsIPv6())) //
				.build();

		mongodExecutable = starter.prepare(mongodConfig);
		mongod = mongodExecutable.start();
		mongo = new MongoClient("localhost", port);
		return mongo;
	}

	public static void cleanupMongo() {
		try {
			mongo.close();
		} finally {
			mongod.stop();
			mongodExecutable.stop();
		}
	}
}

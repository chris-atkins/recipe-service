package com.poorknight.application.init.historical.run20160905;

import com.mongodb.MongoClient;
import com.poorknight.mongo.setup.MongoSetupHelper;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.MongoRecipeRepository;
import com.poorknight.recipe.RecipeRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OneTimeRecipeHtmlifierTest {

	private static MongoClient mongo;
	private RecipeRepository recipeRepository;
	private OneTimeRecipeHtmlifier htmlifier;

	@BeforeAll
	public static void setupMongo() throws Exception {
		mongo = MongoSetupHelper.startMongoInstance();
	}

	@AfterAll
	public static void teardown() {
		MongoSetupHelper.cleanupMongo();
	}

	@BeforeEach
	public void setup() {
		recipeRepository = new MongoRecipeRepository(mongo);
		htmlifier = new OneTimeRecipeHtmlifier(recipeRepository);
	}

	@AfterEach
	public void tearDown() {
		MongoSetupHelper.deleteAllRecipes();
	}

	@Test
	public void transformWorks() throws Exception {
		final String initialRecipe1Content = "Ingredients\n\n2 cups uncooked elbow macaroni (7 ounces)\n1/4 cup butter or margarine\n1/4 cup Gold Medal™ all-purpose flour\n1/2 teaspoon salt\n1/4 teaspoon pepper\n1/4 teaspoon ground mustard\n1/4 teaspoon Worcestershire sauce\n2 cups milk\n2 cups shredded or cubed Cheddar cheese (8 ounces)\n\n\nDirections\n\n1. Heat oven to 350ºF.\n2. Cook macaroni as directed on package.\n3. While macaroni is cooking, melt butter in 3-quart saucepan over low heat. Stir in flour, salt, pepper, mustard and Worcestershire sauce. Cook over medium low heat, stirring constantly, until mixture is smooth and bubbly; remove from heat. Stir in milk. Heat to boiling, stirring constanly. Boil and stir 1 minute. Stir in cheese. Cook, stirring occasionally, until cheese is melted.\n4. Drain macaroni. Gently stir macaroni into cheese sauce. Pour into ungreased 2-quart casserole. Bake uncovered 20 to 25 minutes or until bubbly.";
		final String expectedAlteredRecipe1Content = "<div><!--block-->Ingredients<br><br>2 cups uncooked elbow macaroni (7 ounces)<br>1/4 cup butter or margarine<br>1/4 cup Gold Medal™ all-purpose flour<br>1/2 teaspoon salt<br>1/4 teaspoon pepper<br>1/4 teaspoon ground mustard<br>1/4 teaspoon Worcestershire sauce<br>2 cups milk<br>2 cups shredded or cubed Cheddar cheese (8 ounces)<br><br><br>Directions<br><br>1. Heat oven to 350ºF.<br>2. Cook macaroni as directed on package.<br>3. While macaroni is cooking, melt butter in 3-quart saucepan over low heat. Stir in flour, salt, pepper, mustard and Worcestershire sauce. Cook over medium low heat, stirring constantly, until mixture is smooth and bubbly; remove from heat. Stir in milk. Heat to boiling, stirring constanly. Boil and stir 1 minute. Stir in cheese. Cook, stirring occasionally, until cheese is melted.<br>4. Drain macaroni. Gently stir macaroni into cheese sauce. Pour into ungreased 2-quart casserole. Bake uncovered 20 to 25 minutes or until bubbly.</div>";

		final String initialRecipe2Content = "This was originally supposed to be this: https://www.youtube.com/watch?v=Fxk28ViDeeQ (Chocolate Pretzel Poke Cake) with the whipped cream recipe coming from this: http://addapinch.com/perfect-whipped-cream-recipe/. However....some things went horribly wrong. The end result was delicious, but did not look anything like the video. So I am going to record what I actually did, disasters and all, then follow it up with what I would change the next time I try this.\n\nThis is sized to make a 13 x 9 pan. I recommend having a spoon on hand to serve it as it will be quite mushy (like a lava cake).\n\n\nIngredients: \n\nGhiradelli dark chocolate cake mix: 2 boxes\nExtra cake mix ingredients (go by the box, below is my recollection)\n     - Eggs: 4\n     - Vegetable Oil: 2/3 cup\n     - Water: 1 1/3 cup\nSweetened condensed milk: 1 10 oz can\nCaramel sauce: 2 10oz jars (I used Trader Joe's brand)\nHeavy whipping cream: 2 cups\nSugar: 4 tablespoons\nVanilla: 1 - 2 teaspoons based on taste\n\n\nEquipment:\n\n13 x 9 cake pan with cover\nLarge mixing bowl\nMedium mixing bowl\n2 Whisks\nMedium microwave safe bowl\nSpatula\nHole poker (I used a clean table knife, but something rounder like a chopstick would probably work better)\n\n\nSteps I actually followed:\n\n1. Put the medium mixing bowl and a whisk in the freezer. Put the whipping cream in the refrigerator. Leave them there for at least 20 minutes.\n\n2. Preheat the oven per the instructions for a 9x9 pan.\n\n3. Prepare the chocolate cake mix according to the instructions on the box. Double the ingredient sizes since you're making two boxes.\n\n4. Once the oven is preheated, bake the cake per the instructions for a 9x9 pan.\n\n5. While the cake is baking, pull out the mixing bowl, whisk and whipping cream from the freezer/refrigerator. Now comes the *isn't it totally awesome the internet lied to me* part.\n\n6. We're going to make whipped cream! Pour the whipping cream, sugar and vanilla into the medium mixing bowl. Use the chilled whisk to try and whip the cream. THIS WILL NOT WORK NO MATTER HOW HARD OR LONG YOU TRY.\n\n7. Cake is done! Pull it out and let it cool for 10 minutes. Meanwhile, keep trying to whip the cream. Despair.\n\n8. Poke holes in the top of the cake about every two inches vertically and horizontally. Make sure they're deep enough to puncture the top and expose the delicious nummy cake body underneath.\n\n9. Open the can of condensed milk and pour it evenly over the cake. Make sure it's pouring into the holes you've poked.\n\n10. Open the two jars of caramel sauce and spatula them into the microwave safe bowl. Zap it for about 30 seconds at a time until the caramel is nicely warm and thin. Adjust the microwave time based on your microwave's eccentricities, and fear of burning yourself and having to clean up exploded caramel.\n\n11. Pour the caramel over the cake, making sure it runs into the poked holes. It should look like WAY TOO MUCH CARAMEL. It will run over the sides of the cake and pool in the bottom of the pan. It will cover the cake top. It will seep into the depths of the chocolate. IT WILL BE EVERYWHERE.\n\n12. Swallow nervously and look at the bowl of not-ever-going-to-be-whipped-cream. Take your (rinsed off) spatula and try to take the parts that maybe-kind-of-whipped-a-little and spread them on top of the cake. It won't be much.\n\n13. Take a deep breath. Pick up the mixing bowl of cream. Pour as much as will fit into the cake pan. If you did it like me it will make a huge mess. DO NOT DO IT LIKE ME.\n\n14. Stare in horror at what you've done and then quickly (totally panicking) cover the cake, creating more mess, and try to carefully stuff it in the freezer so it doesn't spill.\n\n15. Let chill for 30 minutes. Try try try try to not think about it.\n\n16. Tentatively open your freezer, gazing in terror at the cake. It will sit quietly, hiding it's monstrosity except for an occasional ominous drip from one side.\n\n17. Pull the cake out (you will try to be careful, it will make still more mess) and put it in the refrigerator to chill for at least four hours.\n\n18. Pull it out, let it warm to room temperature, and serve. Do not even try to add pretzels like in the video, there's no way they will stay on the cake.\n\n19. Apparently it's actually good?!\n\n20. Think of ways to fix the recipe for next time.\n\n\nWhat I would change:\n\n1. I would start by completely ditching the whipped cream topping. I think that the cake absorbed a bunch of the whipping cream concoction and that added to it being complete mush, but I'm not sure if it adds much to the flavor.\n\n2. I'm also not 100% sure what the condensed milk does in this recipe. If it's not your thing I'd experiment with removing it.\n\n3. If you decide to keep the whipped cream, USE A ******* ELECTRIC HAND MIXER. DO NOT BELIEVE THE INTERNET'S LIES.\n\n4. Experiment with adding pretzels. But I'm not sure they'd actually do much for this recipe.";
		final String expectedAlteredRecipe2Content = "<div><!--block-->This was originally supposed to be this: https://www.youtube.com/watch?v=Fxk28ViDeeQ (Chocolate Pretzel Poke Cake) with the whipped cream recipe coming from this: http://addapinch.com/perfect-whipped-cream-recipe/. However....some things went horribly wrong. The end result was delicious, but did not look anything like the video. So I am going to record what I actually did, disasters and all, then follow it up with what I would change the next time I try this.<br><br>This is sized to make a 13 x 9 pan. I recommend having a spoon on hand to serve it as it will be quite mushy (like a lava cake).<br><br><br>Ingredients:&nbsp;<br><br>Ghiradelli dark chocolate cake mix: 2 boxes<br>Extra cake mix ingredients (go by the box, below is my recollection)<br>&nbsp; &nbsp; &nbsp;- Eggs: 4<br>&nbsp; &nbsp; &nbsp;- Vegetable Oil: 2/3 cup<br>&nbsp; &nbsp; &nbsp;- Water: 1 1/3 cup<br>Sweetened condensed milk: 1 10 oz can<br>Caramel sauce: 2 10oz jars (I used Trader Joe's brand)<br>Heavy whipping cream: 2 cups<br>Sugar: 4 tablespoons<br>Vanilla: 1 - 2 teaspoons based on taste<br><br><br>Equipment:<br><br>13 x 9 cake pan with cover<br>Large mixing bowl<br>Medium mixing bowl<br>2 Whisks<br>Medium microwave safe bowl<br>Spatula<br>Hole poker (I used a clean table knife, but something rounder like a chopstick would probably work better)<br><br><br>Steps I actually followed:<br><br>1. Put the medium mixing bowl and a whisk in the freezer. Put the whipping cream in the refrigerator. Leave them there for at least 20 minutes.<br><br>2. Preheat the oven per the instructions for a 9x9 pan.<br><br>3. Prepare the chocolate cake mix according to the instructions on the box. Double the ingredient sizes since you're making two boxes.<br><br>4. Once the oven is preheated, bake the cake per the instructions for a 9x9 pan.<br><br>5. While the cake is baking, pull out the mixing bowl, whisk and whipping cream from the freezer/refrigerator. Now comes the *isn't it totally awesome the internet lied to me* part.<br><br>6. We're going to make whipped cream! Pour the whipping cream, sugar and vanilla into the medium mixing bowl. Use the chilled whisk to try and whip the cream. THIS WILL NOT WORK NO MATTER HOW HARD OR LONG YOU TRY.<br><br>7. Cake is done! Pull it out and let it cool for 10 minutes. Meanwhile, keep trying to whip the cream. Despair.<br><br>8. Poke holes in the top of the cake about every two inches vertically and horizontally. Make sure they're deep enough to puncture the top and expose the delicious nummy cake body underneath.<br><br>9. Open the can of condensed milk and pour it evenly over the cake. Make sure it's pouring into the holes you've poked.<br><br>10. Open the two jars of caramel sauce and spatula them into the microwave safe bowl. Zap it for about 30 seconds at a time until the caramel is nicely warm and thin. Adjust the microwave time based on your microwave's eccentricities, and fear of burning yourself and having to clean up exploded caramel.<br><br>11. Pour the caramel over the cake, making sure it runs into the poked holes. It should look like WAY TOO MUCH CARAMEL. It will run over the sides of the cake and pool in the bottom of the pan. It will cover the cake top. It will seep into the depths of the chocolate. IT WILL BE EVERYWHERE.<br><br>12. Swallow nervously and look at the bowl of not-ever-going-to-be-whipped-cream. Take your (rinsed off) spatula and try to take the parts that maybe-kind-of-whipped-a-little and spread them on top of the cake. It won't be much.<br><br>13. Take a deep breath. Pick up the mixing bowl of cream. Pour as much as will fit into the cake pan. If you did it like me it will make a huge mess. DO NOT DO IT LIKE ME.<br><br>14. Stare in horror at what you've done and then quickly (totally panicking) cover the cake, creating more mess, and try to carefully stuff it in the freezer so it doesn't spill.<br><br>15. Let chill for 30 minutes. Try try try try to not think about it.<br><br>16. Tentatively open your freezer, gazing in terror at the cake. It will sit quietly, hiding it's monstrosity except for an occasional ominous drip from one side.<br><br>17. Pull the cake out (you will try to be careful, it will make still more mess) and put it in the refrigerator to chill for at least four hours.<br><br>18. Pull it out, let it warm to room temperature, and serve. Do not even try to add pretzels like in the video, there's no way they will stay on the cake.<br><br>19. Apparently it's actually good?!<br><br>20. Think of ways to fix the recipe for next time.<br><br><br>What I would change:<br><br>1. I would start by completely ditching the whipped cream topping. I think that the cake absorbed a bunch of the whipping cream concoction and that added to it being complete mush, but I'm not sure if it adds much to the flavor.<br><br>2. I'm also not 100% sure what the condensed milk does in this recipe. If it's not your thing I'd experiment with removing it.<br><br>3. If you decide to keep the whipped cream, USE A ******* ELECTRIC HAND MIXER. DO NOT BELIEVE THE INTERNET'S LIES.<br><br>4. Experiment with adding pretzels. But I'm not sure they'd actually do much for this recipe.</div>";


		final Recipe recipe1 = new Recipe("queryAllRecipesWorksAsExpected_name1", initialRecipe1Content, new Recipe.UserId("userId1"));
		final Recipe recipe2 = new Recipe("queryAllRecipesWorksAsExpected_name2", initialRecipe2Content, new Recipe.UserId("userId2"));
		saveRecipes(recipe1, recipe2);

		final List<Recipe> recipeList = recipeRepository.findAllRecipes();

		final Recipe foundRecipe1 = findRecipeByName("queryAllRecipesWorksAsExpected_name1", recipeList);
		assertThat(foundRecipe1.getContent(), equalTo(initialRecipe1Content));
		assertThat(foundRecipe1.getOwningUserId().getValue(), equalTo("userId1"));

		final Recipe foundRecipe2 = findRecipeByName("queryAllRecipesWorksAsExpected_name2", recipeList);
		assertThat(foundRecipe2.getContent(), equalTo(initialRecipe2Content));
		assertThat(foundRecipe2.getOwningUserId().getValue(), equalTo("userId2"));


		htmlifier.transformAllRecipes();


		final List<Recipe> alteredRecipeList = recipeRepository.findAllRecipes();

		final Recipe alteredRecipe1 = findRecipeByName("queryAllRecipesWorksAsExpected_name1", alteredRecipeList);
		assertThat(alteredRecipe1.getContent(), equalTo(expectedAlteredRecipe1Content));
		assertThat(alteredRecipe1.getOwningUserId().getValue(), equalTo("userId1"));

		final Recipe alteredRecipe2 = findRecipeByName("queryAllRecipesWorksAsExpected_name2", alteredRecipeList);
		assertThat(alteredRecipe2.getContent(), equalTo(expectedAlteredRecipe2Content));
		assertThat(alteredRecipe2.getOwningUserId().getValue(), equalTo("userId2"));
	}

	private void saveRecipes(final Recipe... recipes) {
		for (final Recipe recipe : recipes) {
			recipeRepository.saveNewRecipe(recipe);
		}
	}

	private Recipe findRecipeByName(final String nameToFind, final List<Recipe> recipeList) {
		for (final Recipe recipe : recipeList) {
			if (recipe.getName().equals(nameToFind)) {
				return recipe;
			}
		}
		return null;
	}
}
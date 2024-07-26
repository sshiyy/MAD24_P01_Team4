## MAD24_P01_Team4 - FOOD 
# Members:

```
Tam Shi Ying - S10257952D
Raeann Tai Yu Xuan - S10262832J
Ng Kai Huat Jason - S10262552
law Ming Qi - S10257808
Kaylea Luk - S10258039
```


# Stage 1
**Introduction:**

```
Welcome to Enchante, where each meal is an enjoyable experience! Our revolutionary app is intended to enhance your eating
experience by bringing convenience and flavor to your fingertips. Whether you want a delicious pasta dish or a
refreshing beverage, Enchante has you covered.

With our user-friendly interface, you can easily take orders and explore our extensive selection.The days of waiting
in line are over! Just simply log in to your unique account and explore a world of gastronomic choices.

Track your orders in real time to ensure that your meals arrive on time and hot. When it comes time to pay the bill,
our secure payment system makes things simple, giving you more time to savor each scrumptious bite.

But that's not all, our loyalty program earns you points for every purchase and provides thrilling bonuses and
unique offers with each visit. Furthermore, our advanced filtering page allows you to adapt your dining experience
to your own preferences, ensuring that each item is precisely tailored to your taste.

Join us on a culinary journey unlike any other with Enchante. Download our app now and experience a world of flavor
at your fingertips. Bon appétit!
```


**Objective:**

```
The Enchante application aims to facilitate a smooth and delightful eating experience for our customers while
concurrently promoting business expansion. By giving users a simple way to browse our menu, place orders, and
boost sales. Additionally, we want to encourage customers to choose
Enchante for their dining standards by introducing a point system that rewards repeat business and fosters customer loyalty.
In order to differentiate Enchante from rivals and maintain our position as a leader in the culinary sector, we constantly
work to improve our service and menu options by optimizing restaurant operations and obtaining insightful knowledge about
customer needs and feedback. 
```

**Category of application:**

```
Food & Drink
```
# Stage 1

**Task and features - one person one or more feature:**

```
1. Login/Register/Profile Page - Jason

Our app will allow users to sign up to order our food. We will have a signup, login, profile page.
Users require a Username, name, password and a unique email.

- Implemented Firebase Firestore and Authentication to handle User Accounts
- Implemented User Persistance 
- Implemented Validation to check for unique Email and a 6 or more character password
- Implemented Forgot Password Feature ( sends a email to the respective email address to reset password )
- Implemented Email Change ( Requires legitimate email to function ) 
```
```
2. Cart/Product page - Shi Ying

There will be a product page with all the items displayed and a + - button for users to add the quantity
they want into the cart. The items added into the cart will then appear in the cart page with the
quantities and prices as well as the total price for all the items in the cart. Users can also
increase or decrease the quanitity of the items in the cart page. If users wants to add on items they
can do so by pressing the 'X' button which leads them back into the product page.
```
```
3. Price/Make payment - Ming Qi

After user adds product to cart, there will be a summary of the total price at the bottom of the page.
Also, prices will be updated immediately once user increase or decrease an item.
To pay for the items, user can click on the confirm button and there will be multiple payment methods for user to choose from.
User must select one payment method to enable the "pay' button or can press "cancel" to cancel payment and continue surfing the app.
This is a stimulated payment so no real money will be involved in the process. Payment will be always be successful once user click pay.
After successfully paying, there will be a pop up message saying that payment is successful and reset the cart to empty.
```
```
4. Point system - Kaylea (only did UI) 

There will be a total of 3 members: bronze, sliver and gold. User will automatically become bronze
when they create an account.User will need to accumulate 100 points to become sliver and 300 points 
to become gold. Implemented a redeem voucher so that when accumulates 100points, can get $5 off , 200points, can
get $10 off, 350points can get $20 dollar off. Also updated the cart page, to have a discount so that user know
how many discount they are getting, so when user redeem it will automatically add the voucher in the cart so when user
checkout it will be discounted already. 

Raeann:
- Implement database for the points and tier to be in the account firestore
- Fetch and match the logged in user by email so that we can update the points to the correct
user
- Updated points - making it when redeem voucher it will deduct the points and when payment is done
it will also add the points into the firestore.

```
```
5. Filtering page - Raeann

This will be at the top of the productpage where users can click on the button and a popup filter
will appear for users to choose their category and price range. The cateogry will include Mains,Pizza,
Appetizer,Sidedish,Dessert,Beverages. For the price, it is seperate into $, $$, $$$ where $: $0 to $10
$$: $10 to $20 $$$: $20 and above. Users will then be able to filter according to what they prefer and if
they want to remove they can click on the 'X' button.

- Implemented firebase for productpage
- Fetching the product from firestore for filtering and displaying products
- used Category and Price for filtering

```

# Stage 2 

**Planned task and feature - one person one or more feature**
```
Jason 
Stage 1 Improvements
1. Allow user to upload profile image 
-------------------------------------- 
Stage 2 Features
1. Add gps tracker for locations if Echante decides to franchise to allow Users to locate our cafes
```
Jason's References
1. [Image Uploader](https://www.youtube.com/watch?v=mHR9Lof_VHQ&t=61s)
--------------------------------------------
```
2. Widget and searchbar  - MingQi

        - Widget : 1. Allows user to know their order status (confirmed, preparing, ready, picked up)
                   2. A notification will be prompt after payment asking user whether they want
                      to have a widget on their home screen so that they know when their order is ready       

        - Search Bar : 1. Allows user to search for items without having to key in the full spelling 
```
```
3. Advance cart UI feature - Shiying 

Users can now swipe left on the item to add it to favorites/unfavorite, view more
and also finding other relevant items in the same category.
allow user to ammend order

popular recommendations
order again function
ongoing order
order history
payment


```
```
4. Navigation Bar - Raeann 

1. Hamburger Menu for Navigation Using Fragments
Concepts: Fragment Navigation, HashMap for Fragment Mapping
Implementation:
Fragment Navigation: Implementing a hamburger menu in an Android app can help in efficiently organizing different sections of the app. Fragments are used to display different UI components within the same activity. This approach allows for a modular and flexible design where each section of the app is represented by a fragment.
HashMap for Fragment Mapping: To simplify the fragment navigation logic, a HashMap can be used to map menu item IDs to their respective fragments. This makes the code more maintainable and scalable. For example, when a menu item is selected, the corresponding fragment can be retrieved from the HashMap and displayed, streamlining the process of fragment transactions.

2. Voice Commands and Assistant Integration
Concepts: Speech Recognition, Voice Assistant Integration
Implementation:
Speech Recognition: Using the SpeechRecognizer class in Android, the app can capture voice input from the user. This involves initializing the SpeechRecognizer, setting up the necessary permissions, and handling the speech input events.
Voice Assistant Integration: Integrating with Google Assistant can enhance the app’s functionality by allowing users to perform actions through voice commands. This can be achieved by creating intents that the Google Assistant can trigger based on specific voice commands. By integrating voice commands, users can interact with the app more naturally and perform tasks hands-free, improving the overall user experience.

3. Chatbot for Customer Support
Concepts: Natural Language Processing (NLP), Firestore
Implementation:
Natural Language Processing (NLP): Creating a chatbot involves using NLP techniques to understand and process user queries. NLP allows the chatbot to interpret the intent behind user messages and respond appropriately.
Firestore Integration: Firestore, a flexible and scalable NoSQL cloud database from Firebase, can be used to store and manage the data required for the chatbot. This includes storing predefined responses, user queries, and other relevant information. The chatbot can fetch responses from Firestore based on the user's input, providing real-time, automated support. This implementation enhances customer support by offering instant responses and handling common queries efficiently.



```

```
5.  External notification - Kaylea



```


References
```
Icon Images : https://thenounproject.com/
Logo: https://www.canva.com/

Aglio olio: https://theplantbasedschool.com/spaghetti-aglio-e-olio/
Apple juice: https://www.indianhealthyrecipes.com/apple-juice-recipe/
Avocado milkshake: https://www.oliviascuisine.com/indonesian-avocado-milkshake/
Avocado toast: https://yejiskitchenstories.com/smoked-salmon-avocado-toast/
Baked rice: https://easygourmet.com.sg/nacho-cheese-chicken-ham-baked-rice
Banana split: https://www.twopeasandtheirpod.com/banana-split/
BBQ sausage: https://www.epicurious.com/recipes/food/views/beer-simmered-grilled-sausages-105455
Broccoli garlic toast: https://www.epicurious.com/recipes/food/views/broccoli-and-garlic-ricotta-toasts-with-hot-honey
Buffalo wings: https://easychickenrecipes.com/buffalo-wings-recipe-the-best/
Calamari: https://apronandwhisk.com/easy-fried-calamari/
Cheese Pizza: https://www.foodandwine.com/recipes/classic-cheese-pizza
Cheesy bread sticks: https://www.jocooks.com/recipes/cheesy-breadsticks/
Cheesy cauliflower soup: https://www.kitchensanctuary.com/creamy-cauliflower-soup/
Chicken chop: https://www.sidechef.com/recipes/6106/chicken_chop_with_black_pepper_sauce/
Chocolate cake: https://sugargeekshow.com/recipe/easy-chocolate-cake/
Clam chowder: https://handletheheat.com/new-england-clam-chowder/
Coffee: https://boston.eater.com/maps/best-cafes-boston
Curly fries: https://www.bataviarestaurantsupply.com/product/french-fries-curly-q-65/
Fish n chips: https://www.thespruceeats.com/best-fish-and-chips-recipe-434856
Fries: https://www.bbcgoodfood.com/recipes/french-fries
Grilled fish: https://www.lanascooking.com/simple-grilled-fish/
Honey chicken wing: https://twoplaidaprons.com/honey-garlic-chicken-wings-air-fryer/
Hot chocolate: https://jessicainthekitchen.com/vegan-hot-chocolate-simple-creamy/
Ice cream croissant: https://www.sainsburysmagazine.co.uk/recipes/desserts/peanut-butter-jelly-ice-cream-croissant-sandwich
Ice cream waffle: https://hillstreetgrocer.com/recipes/dessert/homemade-waffles-valhalla-ice-cream
Lasagna: https://newmansown.com/recipes/homestyle-lasagna/
Lemonade: https://lmld.org/simple-lemonade/
Mac n cheese: https://www.allrecipes.com/recipe/238691/simple-macaroni-and-cheese/
Margherita pizza: https://uk.ooni.com/blogs/recipes/margherita-pizza
Meatballs with mozzarella: https://www.bellandevans.com/recipe/skillet-meatballs-with-marinara-and-mozzarella/
Mocha: https://www.olivemagazine.com/recipes/cocktails-and-drinks/mocha/
Mushroom pizza: https://portandfin.com/mushroom-pizza-bianco-with-truffle-oil-fresh-herbs/
Mushroom soup with garlic bread: https://www.nommygod.com/recipes/cream-of-mushroom
Onion rings: https://kristineskitchenblog.com/air-fryer-onion-rings/
Orange juice: https://en.wikipedia.org/wiki/Orange_juice
Bolognese: https://www.bonappetit.com/recipe/bas-best-bolognese
Carbonara: https://www.bonappetit.com/recipe/simple-carbonara
Pepperoni pizza: https://www.thecountrycook.net/smoked-pepperoni-pizza/
Poached salmon: https://downshiftology.com/recipes/poached-salmon/
Popcorn chicken: https://thecozycook.com/popcorn-chicken/
Rainbow crepe cake: https://suncorefoods.com/blogs/recipes/rainbow-crepes-cake
Risotto: https://cooking.nytimes.com/recipes/1017022-mushroom-risotto-with-peas
Root beer: https://shop.scentbridge.com/products/root-beer-float-120-ml-for-scentfit
Salad: https://www.eatingwell.com/recipe/7917784/chopped-power-salad-with-chicken/
Smoked salmon rosti: https://www.recipetineats.com/smoked-salmon-potato-rosti-stack/
Spicy shrimp: https://anaffairfromtheheart.com/spicy-caribbean-shrimp-appetizer/
Steak with rice: https://www.hellofresh.co.uk/recipes/sirloin-steak-and-black-garlic-butter-5f101be30f472c292d5da9f6
Steak with potato: https://fantabulosity.com/easy-steak-recipe-pan-seared-in-the-oven/
Strawberry shortcake: https://www.foodelicacy.com/japanese-strawberry-shortcake/
Strawberry smoothie: https://www.acouplecooks.com/perfect-strawberry-smoothie/
Tiramisu: https://www.foodnetwork.com/recipes/food-network-kitchen/tiramisu-recipe-2131631
Tiramisu crepe cake: https://momsdish.com/tiramisu-crepe-cake

```


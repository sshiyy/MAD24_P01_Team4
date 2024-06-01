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
follow delivery in real time, our app seeks to boost sales. Additionally, we want to encourage customers to choose
Enchante for their dining standards by introducing a point system that rewards repeat business andfosters customer loyalty.
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
3. Make payment - Ming Qi

There will be multiple payment method like QR code and credit card. For QR, we can make it static 
where we will leave it there for 5 seconds and it will pay automatically and for credit card we can 
just let them key in the details and will display the last 4 digit and proceed to payment. 
```
```
4. Point system - Kaylea

There will be a total of 3 members: bronze, sliver and gold. User will automatically become bronze
when they create an account.User will need to accumulate 100 points to become sliver and 300 points 
to become gold. Can have a reedem points page? so users can redeem voucher. 
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
1. User authentication - Jason
        - A new owner role will be added which will allow them to view the current orders and allow them to mark as completed or remove them.
        - When a new user sign up, there will be verification to confirm the email is valid 

1.1 Image Upload 
        - Users can now upload a custom profile image to their profile

```
```
2. Widget and searchbar  - MingQi

notification for users to add widget. (orders preparing)
Search Bar 

```
```
3. Advance cart UI feature - Shiying 

Users can now swipe left on the item to add it to favorites/unfavorite, view more
and also finding other relevant items in the same category.

```
```
4. Navigation Bar - Raeann 

Hamburger , or bottom buttom popup? Fragment, or smth

```

```
5.  External notification - Kaylea



```




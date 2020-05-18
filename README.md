# Read Me 

Simple app to show case simple recommending "system" for an e-shop

There 4 rest enpoints

  * `GET` `http://localhost:8080/customer/recommend/{customer-id}[/{limit}]`
  	 >Returns list of recommended items for a given customer id.
  	 By default limited to 10 items, but optionally path parameter {limit} could by supplied (number of items is also limited by setting of recommanders in DB) 
  
  * `GET` `http://localhost:8080/customer/fav/{customer-id}`
     >Returns list of items that customer has selected as favourites
  
  * `POST` `http://localhost:8080/customer/fav/{customer-id}/{item-id}`
     >Adds given item to the given customer's list of favourites
  
  * `DELETE` `http://localhost:8080/customer/fav/{customer-id}/{item-id}`
     >Removes given item from favourites list of given customer 
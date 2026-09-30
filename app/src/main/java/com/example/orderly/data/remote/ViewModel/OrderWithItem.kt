package com.example.orderly.data.remote.ViewModel

import androidx.room.Embedded
import androidx.room.Relation
import com.example.orderly.data.local.entity.MenuItemEntity
import com.example.orderly.data.local.entity.OrderEntity
import com.example.orderly.data.local.entity.OrderItemEntity
import java.util.stream.DoubleStream.DoubleMapMultiConsumer

/* @Embedded flattens all columns of a data class directly into the parent.
* So instead of orderItem.orderId, orderItem.itemId ect, you can access them
* directly as properties of OrderItemWithMenuItem
*
* @Relation tells Room to automatically fetch related rows from another table.
* It needs 2 things parentColumn - the column in the current table to match on
* entityColumn - the column in the related table to match against */


data class OrderItemWithMenuItem(
    @Embedded val orderItem: OrderItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "id"
    )
    val menuItem: MenuItemEntity /* this basically tells room " for this order item,
     give me the single MenuItemEntity that matches its itemId"*/
)

/* The above can be represented like this:
* SELECT order_items.*, menu_items.*
FROM order_items
JOIN menu_items ON menu_items.id = order_items.itemId */

/*
* so the parent column refers to the itemId because we did
* @Embedded val orderItem:OrderEntity and the entityColumn refers to
* the id in the menuItemEntity because we did val menuItem:MenuItemEntity
* and together they return all the columns from both tables
*  */


data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId",
        entity = OrderItemEntity::class
    )
    val items:List<OrderItemWithMenuItem>/* this tells Room "for each order, give me a list of
     OrderItemWithMenuItem objects" Each object in that list represents one item in the order, bundled together
     with its menuItem details*/
)

/* the above can be represented like this:
* SELECT orders.*, order_items.*, menu_items.*
FROM orders
JOIN order_items ON order_items.orderId = orders.id
JOIN menu_items ON menu_items.id = order_items.itemId */

/* Without entity, Room would look at the type of items to which table to fetch
* from. But here the type is List<OrderItemWithMenuITem> which is not a room entity
* - its just a plan data class. So room wouldnt know which table to query
* entity = ORderItemEntity::class tells Room:
* Which table to query - "go fetch from order_items",
* Which columns to match on - use orderId from order_items to match against id from orders
*
* Then after fetching the OrderItemEntity rows, Room sees that OrderItemWithMenuItem has
* its own relation and automatically fetches the matching MenuItemEntity for each
* item */

/*OrderWithItems
├── order: OrderEntity          → one order
└── items: List<OrderItemWithMenuItem>  → many items in that order
    ├── orderItem: OrderItemEntity  → the quantity, price etc.
    └── menuItem: MenuItemEntity    → the name, image etc.


    orders          order_items          menu_items
------          -----------          ----------
id=1     ←───  orderId=1, itemId="abc"  ───→  id="abc" Fried Chicken
              orderId=1, itemId="xyz"  ───→  id="xyz" Pasta
 */